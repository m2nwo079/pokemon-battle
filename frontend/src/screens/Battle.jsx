import BattleStage from "../three/BattleStage";
import CardTile from "../components/CardTile";
import MoveBar from "../components/MoveBar";

export default function Battle({ state, playCard, chooseMove }) {
    const { phase, hand, myCard, opponentCard, round, wins,
        log, opponentReady, me, myHp, opponentHp, cardLocked } = state;

    return (
        <div className="battle">
            <BattleStage
                myCard={myCard}
                opponentCard={opponentCard}
                opponentPlayed={opponentReady}
                hitKey={log.length}
                events={log}
                me={me}
            />

            <div className="hud">
                <div className="hud-top">
                    <div className="round-chip">{round} / 6 라운드</div>
                    {opponentCard && <HpBox card={opponentCard} hp={opponentHp} side="opp" />}
                    <div className="score-chip">{wins[0]} : {wins[1]}</div>
                </div>

                <div className="hud-mid">
                    {myCard && <HpBox card={myCard} hp={myHp} side="me" />}
                </div>

                <div className="hud-bottom">
                    {phase === "picking" && (
                        <>
                            <p className="prompt">
                                {cardLocked ? "상대를 기다리는 중" : "낼 카드를 고르세요"}
                                {opponentReady && !cardLocked && <em> · 상대는 이미 냈습니다</em>}
                            </p>
                            <div className="hand">
                                {hand.map((c, i) => (
                                    <CardTile key={c.pokemonId} card={c}
                                              onClick={() => playCard(i)}
                                              disabled={cardLocked} />
                                ))}
                            </div>
                        </>
                    )}

                    {phase === "battle" && myCard && (
                        <>
                            <BattleLog events={log} me={me} />
                            <p className="prompt">
                                기술을 고르세요
                                {opponentReady && <em> · 상대는 이미 골랐습니다</em>}
                            </p>
                            <MoveBar moves={myCard.moves} onChoose={chooseMove} />
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}

function HpBox({ card, hp, side }) {
    if (!hp) return null;
    const ratio = Math.max(0, hp.current / hp.max);
    const tone = ratio > 0.5 ? "ok" : ratio > 0.2 ? "warn" : "danger";

    return (
        <div className={`hp-box ${side}`}>
            <div className="hp-name">{card.name}</div>
            <div className="hp-track">
                <div className={`hp-fill ${tone}`} style={{ width: `${ratio * 100}%` }} />
            </div>
            <div className="hp-num">{hp.current} / {hp.max}</div>
        </div>
    );
}

function BattleLog({ events, me }) {
    const recent = events.slice(-3);
    return (
        <ul className="log">
            {recent.map((e, i) => <li key={i}>{describe(e, me)}</li>)}
        </ul>
    );
}

function describe(e, me) {
    const who = e.who === me ? "내" : "상대";

    if (e.type === "miss") return `${who} ${e.move} — 빗나갔다`;
    if (e.type === "faint") return `${e.who === me ? "내 포켓몬이" : "상대가"} 쓰러졌다`;

    const eff =
        e.effectiveness > 1 ? " 효과가 굉장했다" :
            e.effectiveness === 0 ? " 효과가 없었다" :
                e.effectiveness < 1 ? " 효과가 별로였다" : "";

    return `${who} ${e.move} — ${e.damage}${eff}`;
}