import { useRef } from "react";
import BattleStage from "../three/BattleStage";
import CardTile from "../components/CardTile";
import MoveBar from "../components/MoveBar";

export default function Battle({ state, playCard, chooseMove }) {
    const { phase, hand, myCard, opponentCard, round, wins, log, opponentReady } = state;
    const hitKey = useRef(0);
    if (log.length) hitKey.current = log.length;

    return (
        <div className="battle">
            <header className="scoreboard">
                <span>{round} / 6 라운드</span>
                <span>{wins[0]} : {wins[1]}</span>
            </header>

            <BattleStage
                myCard={myCard}
                opponentCard={opponentCard}
                opponentPlayed={opponentReady}
                hitKey={hitKey.current}
                events={log}
            />

            {phase === "picking" && (
                <>
                    <p className="prompt">
                        낼 카드를 고르세요
                        {opponentReady && <em> · 상대는 이미 냈습니다</em>}
                    </p>
                    <div className="hand">
                        {hand.map((c, i) => (
                            <CardTile key={c.pokemonId} card={c} onClick={() => playCard(i)} />
                        ))}
                    </div>
                </>
            )}

            {phase === "battle" && myCard && (
                <>
                    <p className="prompt">
                        기술을 고르세요
                        {opponentReady && <em> · 상대는 이미 골랐습니다</em>}
                    </p>
                    <MoveBar moves={myCard.moves} onChoose={chooseMove} />
                    <BattleLog events={log} />
                </>
            )}
        </div>
    );
}

function BattleLog({ events }) {
    return (
        <ul className="log">
            {events.map((e, i) => (
                <li key={i}>{describe(e)}</li>
            ))}
        </ul>
    );
}

function describe(e) {
    const who = e.who === "p1" ? "1P" : "2P";

    if (e.type === "miss") return `${who} ${e.move} — 빗나갔다`;
    if (e.type === "faint") return `${who} 쓰러졌다`;

    const eff =
        e.effectiveness > 1 ? " (효과가 굉장했다)" :
            e.effectiveness === 0 ? " (효과가 없었다)" :
                e.effectiveness < 1 ? " (효과가 별로였다)" : "";

    return `${who} ${e.move} — ${e.damage} 데미지${eff} · 남은 HP ${e.hpLeft}`;
}