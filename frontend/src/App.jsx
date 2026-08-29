import { useEffect, useState } from "react";
import { useGameSocket } from "./useGameSocket";
import { wakeServer } from "./api";
import Lobby from "./screens/Lobby";
import Battle from "./screens/Battle";
import "./styles.css";

export default function App() {
  const { state, createRoom, joinRoom, playCard, chooseMove, rematch, leave } = useGameSocket();
  const [waking, setWaking] = useState(true);

  useEffect(() => {
    wakeServer().finally(() => setWaking(false));
  }, []);

  if (waking) {
    return (
        <div className="app waking">
          <p>서버를 깨우는 중입니다 (최대 1분)</p>
          <div className="waking-dots" aria-hidden="true">
            <span></span><span></span><span></span>
          </div>
        </div>
    );
  }

  const inBattle = ["picking", "battle", "roundEnd"].includes(state.phase);

  if (state.connectionLost) {
    return (
        <div className="conn-lost" role="alert">
          <h2>연결이 끊겼습니다</h2>
          <p>서버와의 연결이 종료됐습니다. 새로고침 후 다시 시도해 주세요.</p>
          <button className="primary" onClick={() => window.location.reload()}>
            새로고침
          </button>
        </div>
    );
  }

  return (
      <div className={inBattle ? "app app-full" : "app"}>
        {!inBattle && <h1>포켓몬 카드 배틀</h1>}
        {state.error && <p className="error">{state.error}</p>}

        {(state.phase === "lobby" || state.phase === "waiting") && (
            <Lobby state={state} createRoom={createRoom} joinRoom={joinRoom} />
        )}

        {inBattle && (
            <>
              <Battle state={state} playCard={playCard} chooseMove={chooseMove} />
              {state.phase === "roundEnd" && (() => {
                const outcome =
                    state.lastRoundWinner === "draw" ? "draw"
                        : state.lastRoundWinner === state.me ? "win"
                            : "lose";
                const label = { win: "WIN", lose: "LOSE", draw: "DRAW" }[outcome];
                return (
                    <div className="round-result-overlay" role="status" aria-live="assertive">
                      <div className={`round-result ${outcome}`}>
                        {label}
                      </div>
                    </div>
                );
              })()}
            </>
        )}

        {state.phase === "gameEnd" && (
            <div className="center">
              {state.opponentLeft ? (
                  <h2>상대가 나갔습니다</h2>
              ) : (
                  <h2>
                    {state.winner === "draw"
                        ? "무승부"
                        : state.winner === state.me ? "승리" : "패배"}
                  </h2>
              )}
              <p>{state.wins[0]} 대 {state.wins[1]}</p>

              {state.opponentLeft ? (
                  <button className="primary" onClick={() => window.location.reload()}>
                    로비로
                  </button>
              ) : (
                  <div className="end-actions">
                    <button
                        className="primary"
                        onClick={rematch}
                        disabled={state.rematchPending}
                    >
                      {state.rematchPending ? "상대를 기다리는 중" : "다시 하기"}
                    </button>
                    <button className="ghost" onClick={leave}>
                      나가기
                    </button>
                    {state.opponentWantsRematch && !state.rematchPending && (
                        <p className="rematch-hint">상대가 다시하기를 원합니다</p>
                    )}
                  </div>
              )}
            </div>
        )}
      </div>
  );
}