import TypeChip from "./TypeChip";

export default function MoveBar({ moves, onChoose, disabled }) {
    const allOut = moves.length === 0 || moves.every((m) => m.currentPp === 0);

    if (allOut) {
        return (
            <div className="move-bar">
                <button
                    className="move struggle"
                    style={{ gridColumn: "1 / -1" }}
                    onClick={() => onChoose(-1)}
                    disabled={disabled}
                >
                    <span className="move-name">발버둥</span>
                    <span className="move-meta">쓸 수 있는 기술이 없습니다</span>
                </button>
            </div>
        );
    }

    return (
        <div className="move-bar">
            {moves.map((m) => (
                <button
                    key={m.moveId}
                    className="move"
                    onClick={() => onChoose(m.moveId)}
                    disabled={disabled || m.currentPp === 0}
                >
                    <span className="move-name">
                        {m.name}
                        {m.type && <TypeChip type={m.type} />}
                    </span>
                    <span className="move-meta">
                        위력 {m.power}
                        {m.currentPp !== undefined && ` · PP ${m.currentPp}`}
                    </span>
                </button>
            ))}
        </div>
    );
}