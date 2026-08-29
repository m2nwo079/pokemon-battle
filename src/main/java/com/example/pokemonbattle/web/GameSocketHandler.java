package com.example.pokemonbattle.web;

import com.example.pokemonbattle.card.Card;
import com.example.pokemonbattle.card.CardFactory;
import com.example.pokemonbattle.game.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GameSocketHandler.class);

    private final RoomRegistry rooms;
    private final CardFactory cards;
    private final ObjectMapper json = new ObjectMapper();

    private final Map<String, GameRoom> roomOf = new ConcurrentHashMap<>();
    private final Map<String, Player> playerOf = new ConcurrentHashMap<>();

    public GameSocketHandler(RoomRegistry rooms, CardFactory cards) {
        this.rooms = rooms;
        this.cards = cards;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Map<String, Object> msg = json.readValue(message.getPayload(), Map.class);
            String type = String.valueOf(msg.get("type"));

            switch (type) {
                case "create_room" -> createRoom(session);
                case "join_room"   -> joinRoom(session, asString(msg.get("roomCode")));
                case "play_card"   -> playCard(session, asInt(msg.get("cardIndex")));
                case "choose_move" -> chooseMove(session, asInt(msg.get("moveId")));
                case "rematch"     -> rematch(session);
                case "leave"       -> leave(session);
                default -> send(session, Map.of("type", "error",
                        "code", "unknown_type", "message", "모르는 요청입니다"));
            }
        } catch (Exception e) {
            log.warn("메시지 처리 실패: {}", message.getPayload(), e);
            send(session, Map.of("type", "error",
                    "code", "bad_request", "message", "요청을 처리하지 못했습니다"));
        }
    }

    private static String asString(Object v) {
        if (v == null) throw new IllegalArgumentException("값이 없습니다");
        return v.toString();
    }

    private static int asInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        throw new IllegalArgumentException("숫자가 아닙니다: " + v);
    }


    private void createRoom(WebSocketSession session) {
        GameRoom room = rooms.create();
        Player me = new Player(session);

        synchronized (room) {
            room.players.add(me);
        }

        roomOf.put(session.getId(), room);
        playerOf.put(session.getId(), me);

        send(session, Map.of("type", "room_created",
                "roomCode", room.code));
    }

    private void joinRoom(WebSocketSession session, String code) {
        GameRoom room = rooms.find(code);

        if (room == null) {
            send(session, Map.of("type", "error",
                    "code", "no_room", "message", "그런 방이 없습니다"));
            return;
        }

        Player me = new Player(session);

        synchronized (room) {
            if (room.finished) {
                send(session, Map.of("type", "error",
                        "code", "no_room", "message", "이미 끝난 방입니다"));
                return;
            }
            if (room.players.size() >= 2) {
                send(session, Map.of("type", "error",
                        "code", "room_full", "message", "이미 두 명입니다"));
                return;
            }

            room.players.add(me);
            roomOf.put(session.getId(), room);
            playerOf.put(session.getId(), me);

            send(session, Map.of("type", "room_joined",
                    "roomCode", room.code));

            startGame(room);
        }
    }

    private void startGame(GameRoom room) {
        room.hands[0] = cards.deal(6);
        room.hands[1] = cards.deal(6);
        room.round = 1;

        for (int i = 0; i < 2; i++) {
            List<Map<String, Object>> hand = new ArrayList<>();
            for (Card c : room.hands[i]) hand.add(c.toPayload(true));

            send(room.players.get(i).session,
                    Map.of("type", "game_start", "round", 1, "myHand", hand));
        }
    }

    private void rematch(WebSocketSession session) {
        GameRoom room = roomOf.get(session.getId());
        Player me = playerOf.get(session.getId());
        if (room == null || me == null) return;

        synchronized (room) {
            if (!room.finished) return;
            if (room.players.size() < 2) return;

            int i = room.indexOf(me);
            if (i == -1) return;

            room.rematchReady[i] = true;

            Player opponent = room.opponentOf(i);
            if (opponent != null) {
                send(opponent.session, Map.of("type", "opponent_rematch"));
            }

            if (room.rematchReady[0] && room.rematchReady[1]) {
                room.resetForRematch();
                startGame(room);
            }
        }
    }

    private void leave(WebSocketSession session) {
        try {
            session.close();
        } catch (Exception ignored) {}
    }

    private void playCard(WebSocketSession session, int index) {
        GameRoom room = roomOf.get(session.getId());
        Player me = playerOf.get(session.getId());
        if (room == null || me == null) return;

        synchronized (room) {
            if (room.finished) return;
            if (room.players.size() < 2) return;

            int i = room.indexOf(me);
            if (i == -1) return;
            if (room.hands[i] == null) return;
            if (room.played[i] != null) return;
            if (index < 0 || index >= room.hands[i].size()) return;

            room.played[i] = room.hands[i].remove(index);

            Player opponent = room.opponentOf(i);
            if (opponent != null) {
                send(opponent.session, Map.of("type", "opponent_played"));
            }

            if (room.bothPlayed()) startRound(room);
        }
    }

    private void startRound(GameRoom room) {
        Fighter f0 = fighterOf(room.played[0]);
        Fighter f1 = fighterOf(room.played[1]);
        room.battle = new Battle(f0, room.played[0], f1, room.played[1]);

        Fighter[] fs = { f0, f1 };

        for (int i = 0; i < 2; i++) {
            Fighter mine = fs[i], theirs = fs[1 - i];
            send(room.players.get(i).session, new RoundStart(
                    room.round,
                    i == 0 ? "p1" : "p2",
                    room.played[i].toPayload(true),
                    room.played[1 - i].toPayload(false),
                    HpState.of(mine),
                    HpState.of(theirs),
                    mine.getTypes(),
                    theirs.getTypes()));
        }
    }

    private void chooseMove(WebSocketSession session, int moveId) {
        GameRoom room = roomOf.get(session.getId());
        Player me = playerOf.get(session.getId());
        if (room == null || me == null) return;

        synchronized (room) {
            if (room.finished) return;
            if (room.battle == null) return;
            if (room.players.size() < 2) return;

            int i = room.indexOf(me);
            if (i == -1) return;
            if (room.chosenMove[i] != null) return;

            room.chosenMove[i] = moveId;

            Player opponent = room.opponentOf(i);
            if (opponent != null) {
                send(opponent.session, Map.of("type", "opponent_chose"));
            }

            if (room.bothChose()) resolveTurn(room);
        }
    }

    private void resolveTurn(GameRoom room) {
        TurnResult r = room.battle.playTurn(room.chosenMove[0], room.chosenMove[1]);
        room.clearTurn();

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "turn_result");
        payload.put("events", r.events);
        payload.put("winner", r.winner);
        broadcast(room, payload);

        if (r.winner != null) endRound(room, r.winner);
    }

    private void endRound(GameRoom room, String roundWinner) {
        if ("p1".equals(roundWinner)) room.wins[0]++;
        else if ("p2".equals(roundWinner)) room.wins[1]++;

        broadcast(room, new RoundEnd(roundWinner,
                List.of(room.wins[0], room.wins[1])));

        room.clearRound();
        room.round++;

        if (room.round > 6) {
            String winner = room.wins[0] > room.wins[1] ? "p1"
                    : room.wins[1] > room.wins[0] ? "p2" : "draw";

            broadcast(room, new GameEnd(winner,
                    List.of(room.wins[0], room.wins[1])));

            room.finished = true;
            room.rematchReady[0] = false;
            room.rematchReady[1] = false;
        }
    }

    private Fighter fighterOf(Card card) {
        return cards.fighterOf(card.getPokemonId());
    }

    private void broadcast(GameRoom room, Object payload) {
        for (Player p : room.players) send(p.session, payload);
    }

    private void send(WebSocketSession session, Object payload) {
        try {
            String text = json.writeValueAsString(payload);
            synchronized (session) {
                if (session.isOpen()) session.sendMessage(new TextMessage(text));
            }
        } catch (Exception e) {
            log.debug("전송 실패", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        GameRoom room = roomOf.remove(session.getId());
        Player me = playerOf.remove(session.getId());
        if (room == null || me == null) return;

        synchronized (room) {
            int myIndex = room.players.indexOf(me);
            if (myIndex != -1) {
                room.players.remove(myIndex);

                for (Player p : room.players) {
                    send(p.session, Map.of(
                            "type", "opponent_left",
                            "message", "상대가 나갔습니다"));
                }
            }
            room.finished = true;
        }

        rooms.remove(room.code);
    }
}