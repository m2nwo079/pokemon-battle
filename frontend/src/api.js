const WS_BASE = import.meta.env.VITE_WS_BASE;
const API_BASE = import.meta.env.VITE_API_BASE;

export const wsUrl = () => `${WS_BASE}/ws`;

export const wakeServer = () =>
    fetch(`${API_BASE}/health`).catch(() => null);

export const spriteUrl = (pokemonId, back = false) =>
    `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${back ? "back/" : ""}${pokemonId}.png`;