import { spriteUrl } from "../api";

export default function CardTile({ card, onClick, disabled }) {
    return (
        <button
            className="card-tile"
            onClick={onClick}
            disabled={disabled || !onClick}
        >
            <img src={spriteUrl(card.pokemonId)} alt={card.name} width={96} height={96} />
            <div className="card-name">{card.name}</div>
        </button>
    );
}