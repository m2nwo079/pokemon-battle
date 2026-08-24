package com.example.pokemonbattle;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Component
public class GameSocketHandler extends TextWebSocketHandler {

    private final RoomRegistry rooms;
    private final CardFactory cards;
    private final ObjectMapper json = new ObjectMapper();

    private final Map<String, GameRoom> roomOf = new HashMap<>();
    private final Map<String, Player> playerOf = new HashMap<>();

    public GameSocketHandler(RoomRegistry rooms, CardFactory cards) {
        this.rooms = rooms;
        this.cards = cards;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> msg = json.readValue(message.getPayload(), Map.class);
        String type = (String) msg.get("type");

        switch (type) {
            case "create_room" -> createRoom(session);
            case "join_room"   -> joinRoom(session, (String) msg.get("roomCode"));
            case "play_card"   -> playCard(session, (int) msg.get("cardIndex"));
            case "choose_move" -> chooseMove(session, (int) msg.get("moveId"));
            default -> send(session, Map.of("type", "error",
                    "code", "unknown_type", "message", "모르는 요청입니다"));
        }
    }


    private void createRoom(WebSocketSession session) throws Exception {
        GameRoom room = rooms.create();
        Player me = new Player(session, UUID.randomUUID().toString());
        room.players.add(me);

        roomOf.put(session.getId(), room);
        playerOf.put(session.getId(), me);

        send(session, Map.of("type", "room_created",
                "roomCode", room.code, "playerToken", me.token));
    }

    private void joinRoom(WebSocketSession session, String code) throws Exception {
        GameRoom room = rooms.find(code);

        if (room == null) {
            send(session, Map.of("type", "error",
                    "code", "no_room", "message", "그런 방이 없습니다"));
            return;
        }
        if (room.players.size() >= 2) {
            send(session, Map.of("type", "error",
                    "code", "room_full", "message", "이미 두 명입니다"));
            return;
        }

        Player me = new Player(session, UUID.randomUUID().toString());
        room.players.add(me);
        roomOf.put(session.getId(), room);
        playerOf.put(session.getId(), me);

        send(session, Map.of("type", "room_created",
                "roomCode", room.code, "playerToken", me.token));

        startGame(room);
    }

    private void startGame(GameRoom room) throws Exception {
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

    private void playCard(WebSocketSession session, int index) throws Exception {
        GameRoom room = roomOf.get(session.getId());
        if (room == null) return;

        int me = room.indexOf(playerOf.get(session.getId()));
        if (room.played[me] != null) return;
        if (index < 0 || index >= room.hands[me].size()) return;

        room.played[me] = room.hands[me].remove(index);

        send(room.opponentOf(me).session, Map.of("type", "opponent_played"));

        if (room.bothPlayed()) startRound(room);
    }

    private void startRound(GameRoom room) throws Exception {
        Fighter f0 = fighterOf(room.played[0]);
        Fighter f1 = fighterOf(room.played[1]);
        room.battle = new Battle(f0, room.played[0], f1, room.played[1]);

        for (int i = 0; i < 2; i++) {
            send(room.players.get(i).session, Map.of(
                    "type", "round_start",
                    "round", room.round,
                    "myCard", room.played[i].toPayload(true),
                    "opponentCard", room.played[1 - i].toPayload(false)));
        }
    }

    private void chooseMove(WebSocketSession session, int moveId) throws Exception {
        GameRoom room = roomOf.get(session.getId());
        if (room == null || room.battle == null) return;

        int me = room.indexOf(playerOf.get(session.getId()));
        if (room.chosenMove[me] != null) return;

        room.chosenMove[me] = moveId;
        send(room.opponentOf(me).session, Map.of("type", "opponent_chose"));

        if (room.bothChose()) resolveTurn(room);
    }

    private void resolveTurn(GameRoom room) throws Exception {
        TurnResult r = room.battle.playTurn(room.chosenMove[0], room.chosenMove[1]);
        room.clearTurn();

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "turn_result");
        payload.put("events", r.events);
        payload.put("winner", r.winner);
        broadcast(room, payload);

        if (r.winner != null) endRound(room, r.winner);
    }

    private void endRound(GameRoom room, String roundWinner) throws Exception {
        if ("p1".equals(roundWinner)) room.wins[0]++;
        else if ("p2".equals(roundWinner)) room.wins[1]++;

        broadcast(room, Map.of("type", "round_end",
                "roundWinner", roundWinner,
                "wins", List.of(room.wins[0], room.wins[1])));

        room.clearRound();
        room.round++;

        if (room.round > 6) {
            String winner = room.wins[0] > room.wins[1] ? "p1"
                    : room.wins[1] > room.wins[0] ? "p2" : "draw";
            broadcast(room, Map.of("type", "game_end",
                    "winner", winner,
                    "wins", List.of(room.wins[0], room.wins[1])));
        }
    }

    private Fighter fighterOf(Card card) {
        return cards.fighterOf(card.getPokemonId());
    }

    private void broadcast(GameRoom room, Map<String, Object> payload) throws Exception {
        for (Player p : room.players) send(p.session, payload);
    }

    private void send(WebSocketSession session, Map<String, Object> payload) throws Exception {
        String text = json.writeValueAsString(payload);
        synchronized (session) {
            if (session.isOpen()) session.sendMessage(new TextMessage(text));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        roomOf.remove(session.getId());
        playerOf.remove(session.getId());
    }
}