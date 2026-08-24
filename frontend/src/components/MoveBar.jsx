import TypeChip from "./TypeChip";

export default function MoveBar({ moves, onChoose, disabled }) {
    return (
        <div className="move-bar">
            {moves.map((m) => (
                <button
                    key={m.moveId}
                    className="move"
                    onClick={() => onChoose(m.moveId)}
                    disabled={disabled || m.currentPp === 0}
                >
                    <span className="move-name">{m.name}</span>
                    <span className="move-meta">
            위력 {m.power}
                        {m.currentPp !== undefined && ` · PP ${m.currentPp}`}
          </span>
                </button>
            ))}
        </div>
    );
}