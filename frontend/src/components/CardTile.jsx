import { spriteUrl } from "../api";

export default function CardTile({ card, label, onClick, disabled }) {
    return (
        <button
            className="card-tile"
            onClick={onClick}
            disabled={disabled || !onClick}
        >
            {label && <small className="card-label">{label}</small>}
            <img src={spriteUrl(card.pokemonId)} alt={card.name} width={96} height={96} />
            <div className="card-name">{card.name}</div>
        </button>
    );
}