import { useState } from "react";

export default function Lobby({ state, createRoom, joinRoom }) {
    const [code, setCode] = useState("");
    const [nick, setNick] = useState("");

    const ready = nick.trim().length > 0;

    if (state.phase === "waiting") {
        return (
            <div className="lobby">
                <h1 className="lobby-title">포켓몬 카드 배틀</h1>
                <div className="lobby-card waiting">
                    <p className="field-label">방 코드</p>
                    <strong className="room-code">{state.roomCode}</strong>
                    <p className="hint">상대에게 알려 주고 기다리세요</p>
                </div>
            </div>
        );
    }

    return (
        <div className="lobby">
            <h1 className="lobby-title">포켓몬 카드 배틀</h1>
            <p className="lobby-sub">
                랜덤으로 받은 6마리로 6라운드. 타입 상성이 스탯을 뒤집는다.
            </p>

            <div className="lobby-stack">
                <div className="lobby-card identity">
                    <label className="field-label" htmlFor="nick">닉네임</label>
                    <input
                        id="nick"
                        className="field nick"
                        value={nick}
                        onChange={(e) => setNick(e.target.value)}
                        placeholder="닉네임을 입력하세요"
                        maxLength={12}
                    />
                    {!ready && (
                        <p className="hint">방을 만들거나 입장하려면 닉네임이 필요합니다</p>
                    )}
                </div>

                <div className="lobby-actions">
                    <div className="action-card">
                        <p className="action-title">새 방 만들기</p>
                        <p className="action-desc">방을 열고 상대에게 코드를 알려 주세요</p>
                        <button
                            className="primary block push"
                            onClick={() => createRoom(nick)}
                            disabled={!ready}
                        >
                            방 만들기
                        </button>
                    </div>

                    <div className="action-card">
                        <p className="action-title">코드로 입장</p>
                        <input
                            id="code"
                            className="field code"
                            value={code}
                            onChange={(e) => setCode(e.target.value.toUpperCase())}
                            placeholder="ABCD"
                            maxLength={4}
                        />
                        <button
                            className="block push"
                            onClick={() => joinRoom(code, nick)}
                            disabled={!ready || code.length !== 4}
                        >
                            입장하기
                        </button>
                    </div>
                </div>
            </div>

            <p className="lobby-foot">
                같은 브라우저에서 시크릿 창을 하나 더 열면 혼자서도 양쪽을 다 해볼 수 있다.
            </p>
        </div>
    );
}