package com.example.pokemonbattle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TurnResult {
    public final List<Map<String, Object>> events = new ArrayList<>();
    public String winner;

    public void hit(String who, Move move, int damage, double effect,
                    boolean stab, int hpLeft) {
        events.add(Map.of(
                "type", "hit", "who", who,
                "move", move.getName(),
                "moveType", move.getType(),
                "moveId", move.getId(),
                "damage", damage,
                "effectiveness", effect,
                "stab", stab,
                "hpLeft", hpLeft));
    }

    public void miss(String who, Move move) {
        events.add(Map.of(
                "type", "miss",
                "who", who,
                "move", move.getName(),
                "moveType", move.getType(),
                "moveId", move.getId()));
    }

    public void faint(String who) {
        events.add(Map.of("type", "faint", "who", who));
    }
}