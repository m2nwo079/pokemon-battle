package com.example.pokemonbattle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TurnResult {
    public final List<Map<String, Object>> events = new ArrayList<>();
    public String winner;

    public void hit(String who, Move move, int damage, double effect,
                    boolean stab, int hpLeft, int ppLeft, int healed) {
        events.add(Map.ofEntries(
                Map.entry("type", "hit"),
                Map.entry("who", who),
                Map.entry("move", move.getName()),
                Map.entry("moveType", move.getType()),
                Map.entry("moveId", move.getId()),
                Map.entry("damage", damage),
                Map.entry("effectiveness", effect),
                Map.entry("stab", stab),
                Map.entry("hpLeft", hpLeft),
                Map.entry("ppLeft", ppLeft),
                Map.entry("healed", healed)));
    }

    public void miss(String who, Move move, int ppLeft) {
        events.add(Map.of(
                "type", "miss", "who", who,
                "move", move.getName(),
                "moveType", move.getType(),
                "moveId", move.getId(),
                "ppLeft", ppLeft));
    }

    public void faint(String who) {
        events.add(Map.of("type", "faint", "who", who));
    }
}