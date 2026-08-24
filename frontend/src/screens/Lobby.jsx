import { useState } from "react";

export default function Lobby({ state, createRoom, joinRoom }) {
    const [code, setCode] = useState("");

    console.log("Lobby 렌더됨, phase =", state.phase);

    if (state.phase === "waiting") {
        return (
            <div className="lobby">
                <p>방 코드</p>
                <strong className="room-code">{state.roomCode}</strong>
                <p className="hint">상대에게 알려 주고 기다리세요</p>
            </div>
        );
    }

    return (
        <div className="lobby">
            <button className="primary" onClick={createRoom}>방 만들기</button>

            <div className="divider">또는</div>

            <div className="join">
                <input
                    value={code}
                    onChange={(e) => setCode(e.target.value.toUpperCase())}
                    placeholder="ABCD"
                    maxLength={4}
                />
                <button onClick={() => joinRoom(code)} disabled={code.length !== 4}>
                    입장
                </button>
            </div>
        </div>
    );
}