package com.example.pokemonbattle;

public class BattleEngine {

    public static int damage(Fighter attacker, Fighter defender, Move move, double roll) {
        double base = ((2 * 50 / 5.0 + 2) * move.getPower()
                * attacker.attackFor(move) / defender.defenseFor(move)) / 50.0 + 2;
        double stab = attacker.hasType(move.getType()) ? 1.5 : 1.0;
        double eff = TypeChart.multiplier(move.getType(), defender.getTypes());
        return (int) Math.floor(base * stab * eff * roll);
    }
}
