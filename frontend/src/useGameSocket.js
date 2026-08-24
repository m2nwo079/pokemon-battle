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
};

function reducer(state, msg) {
    switch (msg.type) {
        case "room_created":
            return { ...state, phase: "waiting", roomCode: msg.roomCode, error: null };

        case "game_start":
            return { ...state, phase: "picking", hand: msg.myHand, round: msg.round, log: [] };

        case "opponent_played":
        case "opponent_chose":
            return { ...state, opponentReady: true };

        case "round_start":
            return { ...state, phase: "battle", round: msg.round,
                myCard: msg.myCard, opponentCard: msg.opponentCard,
                opponentReady: false, log: [] };

        case "turn_result":
            return { ...state, opponentReady: false, log: [...state.log, ...msg.events] };

        case "round_end":
            return { ...state, phase: "picking", wins: msg.wins,
                myCard: null, opponentCard: null,
                round: state.round + 1, opponentReady: false, log: [] };

        case "game_end":
            return { ...state, phase: "gameEnd", winner: msg.winner, wins: msg.wins };

        case "error":
            return { ...state, error: msg.message };

        case "local_card_played":
            return { ...state,
                hand: state.hand.filter((_, i) => i !== msg.cardIndex) };

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
            send({ type: "play_card", cardIndex });
            dispatch({ type: "local_card_played", cardIndex });
        },
        chooseMove: (moveId) => send({ type: "choose_move", moveId }),
    };
}