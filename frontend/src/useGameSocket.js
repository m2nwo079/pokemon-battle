import { useEffect, useRef, useReducer } from "react";
import { wsUrl } from "./api";

const initial = {
    phase: "lobby",      // lobby | waiting | picking | battle | roundEnd | gameEnd
    roomCode: null,
    hand: [],
    myCard: null,
    opponentCard: null,
    round: 0,
    wins: [0, 0],
    log: [],
    winner: null,
    opponentReady: false,
    error: null,
    me: "p1",
    myHp: null,
    opponentHp: null,
    myTypes: [],
    opponentTypes: [],
    myStatus: "none",
    opponentStatus: "none",
    cardLocked: false,
    moveLocked: false,
    opponentLeft: false,
    opponentWantsRematch: false,
    rematchPending: false,
    lastRoundWinner: null,
};

function reducer(state, msg) {
    switch (msg.type) {
        case "room_created":
            return { ...state, phase: "waiting", roomCode: msg.roomCode, error: null };

        case "game_start":
            return { ...state, phase: "picking", hand: msg.myHand, round: msg.round, log: [],
                winner: null, wins: [0, 0], opponentLeft: false,
                opponentWantsRematch: false, rematchPending: false };

        case "opponent_played":
        case "opponent_chose":
            return { ...state, opponentReady: true };

        case "round_start":
            return { ...state, phase: "battle", round: msg.round,
                me: msg.me,
                myCard: msg.myCard, opponentCard: msg.opponentCard,
                myHp: msg.myHp, opponentHp: msg.opponentHp,
                myTypes: msg.myTypes ?? [], opponentTypes: msg.opponentTypes ?? [],
                myStatus: "none", opponentStatus: "none",
                cardLocked: false, opponentReady: false, log: [] };

        case "turn_result": {
            let myHp = state.myHp;
            let oppHp = state.opponentHp;
            let myCard = state.myCard;
            let myStatus = state.myStatus;
            let oppStatus = state.opponentStatus;

            for (const e of msg.events) {
                if (e.type === "hit") {
                    if (e.who === state.me) {
                        oppHp = { ...oppHp, current: e.hpLeft };
                        if (e.healed > 0 && myHp) {
                            myHp = { ...myHp, current: Math.min(myHp.max, myHp.current + e.healed) };
                        }
                    } else {
                        myHp = { ...myHp, current: e.hpLeft };
                        if (e.healed > 0 && oppHp) {
                            oppHp = { ...oppHp, current: Math.min(oppHp.max, oppHp.current + e.healed) };
                        }
                    }
                }

                if (e.type === "status_inflicted") {
                    if (e.who === state.me) myStatus = e.status;
                    else oppStatus = e.status;
                }

                if (e.type === "status_damage") {
                    if (e.who === state.me) myHp = { ...myHp, current: e.hpLeft };
                    else oppHp = { ...oppHp, current: e.hpLeft };
                }

                if (e.who === state.me && e.moveId !== undefined && e.ppLeft !== undefined && myCard) {
                    myCard = {
                        ...myCard,
                        moves: myCard.moves.map((m) =>
                            m.moveId === e.moveId ? { ...m, currentPp: e.ppLeft } : m
                        ),
                    };
                }
            }

            return { ...state, opponentReady: false, moveLocked: false,
                myHp, opponentHp: oppHp, myCard,
                myStatus, opponentStatus: oppStatus,
                log: [...state.log, ...msg.events] };
        }

        case "round_end":
            return { ...state, phase: "roundEnd", wins: msg.wins,
                lastRoundWinner: msg.roundWinner,
                myCard: null, opponentCard: null,
                myHp: null, opponentHp: null,
                round: state.round + 1,
                cardLocked: false, opponentReady: false, log: [] };

        case "advance_to_picking":
            return { ...state, phase: "picking" };

        case "game_end":
            return { ...state, phase: "gameEnd", winner: msg.winner, wins: msg.wins };

        case "error":
            return { ...state, error: msg.message };

        case "local_card_played":
            return { ...state, cardLocked: true,
                hand: state.hand.filter((_, i) => i !== msg.cardIndex) };

        case "local_move_chosen":
            return { ...state, moveLocked: true };

        case "local_rematch":
            return { ...state, rematchPending: true };
        case "opponent_rematch":
            return { ...state, opponentWantsRematch: true };

        case "opponent_left":
            return { ...state, phase: "gameEnd",
                winner: null, wins: state.wins,
                opponentLeft: true, error: null };

        default:
            return state;
    }
}

export function useGameSocket() {
    const [state, dispatch] = useReducer(reducer, initial);
    const ws = useRef(null);

    useEffect(() => {
        const socket = new WebSocket(wsUrl());
        socket.onmessage = (e) => dispatch(JSON.parse(e.data));
        ws.current = socket;
        return () => socket.close();
    }, []);

    useEffect(() => {
        if (state.phase !== "roundEnd") return;
        const t = setTimeout(() => dispatch({ type: "advance_to_picking" }), 2000);
        return () => clearTimeout(t);
    }, [state.phase]);

    const send = (payload) => {
        if (ws.current?.readyState === WebSocket.OPEN) {
            ws.current.send(JSON.stringify(payload));
        }
    };

    return {
        state,
        createRoom: () => send({ type: "create_room" }),
        joinRoom: (roomCode) => send({ type: "join_room", roomCode }),
        playCard: (cardIndex) => {
            if (state.cardLocked) return;
            send({ type: "play_card", cardIndex });
            dispatch({ type: "local_card_played", cardIndex });
        },
        chooseMove: (moveId) => {
            if (state.moveLocked) return;
            send({ type: "choose_move", moveId });
            dispatch({ type: "local_move_chosen" });
        },
        rematch: () => {
            send({ type: "rematch" });
            dispatch({ type: "local_rematch" });
        },
        leave: () => {
            send({ type: "leave" });
            window.location.reload();
        },
    };
}