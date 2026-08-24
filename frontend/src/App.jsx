import { useEffect, useState } from "react";
import { useGameSocket } from "./useGameSocket";
import { wakeServer } from "./api";
import Lobby from "./screens/Lobby";
import Battle from "./screens/Battle";
import "./styles.css";

export default function App() {
  const { state, createRoom, joinRoom, playCard, chooseMove } = useGameSocket();
  const [waking, setWaking] = useState(true);

  useEffect(() => {
    wakeServer().finally(() => setWaking(false));
  }, []);

  if (waking) {
    return <div className="app center">서버를 깨우는 중입니다 (최대 1분)</div>;
  }

  console.log("phase =", state.phase);
  return (
      <div className="app">
        <h1>포켓몬 카드 배틀</h1>
        {state.error && <p className="error">{state.error}</p>}

        {(state.phase === "lobby" || state.phase === "waiting") && (
            <Lobby state={state} createRoom={createRoom} joinRoom={joinRoom} />
        )}

        {["picking", "battle", "roundEnd"].includes(state.phase) && (
            <Battle state={state} playCard={playCard} chooseMove={chooseMove} />
        )}

        {state.phase === "gameEnd" && (
            <div className="center">
              <h2>{state.winner === "draw" ? "무승부" : `${state.winner === "p1" ? "1P" : "2P"} 승리`}</h2>
              <p>{state.wins[0]} 대 {state.wins[1]}</p>
              <button className="primary" onClick={() => window.location.reload()}>
                다시 하기
              </button>
            </div>
        )}
      </div>
  );
}