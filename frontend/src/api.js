const WS_BASE = import.meta.env.VITE_WS_BASE;
const API_BASE = import.meta.env.VITE_API_BASE;

export const wsUrl = () => `${WS_BASE}/ws`;

export const wakeServer = () => fetch(`${API_BASE}/health`);

export const spriteUrl = (pokemonId) =>
    `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemonId}.png`;