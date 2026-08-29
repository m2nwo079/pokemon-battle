package com.example.pokemonbattle.card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Card {
    private final int pokemonId;
    private final String name;
    private final List<Move> moves;
    private final Map<Integer, Integer> pp;   // 기술별 남은 사용 횟수

    public Card(int pokemonId, String name, List<Move> moves) {
        this.pokemonId = pokemonId;
        this.name = name;
        this.moves = moves;
        this.pp = new HashMap<>();
        for (Move m : moves) {
            this.pp.put(m.getId(), m.getMaxPp());   // 만들어지는 순간 최대치로 채운다
        }
    }

    public int getPokemonId() {
        return pokemonId;
    }
    public String getName() {
        return name;
    }
    public List<Move> getMoves() {
        return moves;
    }

    public int ppLeft(int moveId) { return pp.getOrDefault(moveId, 0); }

    public void usePp(int moveId) {
        pp.computeIfPresent(moveId, (k, v) -> Math.max(0, v - 1));
    }

    public Map<String, Object> toPayload(boolean isMine) {
        Map<String, Object> out = new HashMap<>();
        out.put("pokemonId", pokemonId);
        out.put("name", name);

        List<Map<String, Object>> moveList = new ArrayList<>();
        for (Move m : moves) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("moveId", m.getId());
            entry.put("name", m.getName());
            entry.put("type", m.getType());
            entry.put("power", m.getPower());
            if (isMine) {
                entry.put("currentPp", pp.get(m.getId()));
            }
            moveList.add(entry);
        }
        out.put("moves", moveList);
        return out;
    }
}
