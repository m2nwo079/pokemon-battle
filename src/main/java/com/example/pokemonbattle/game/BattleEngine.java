package com.example.pokemonbattle.game;

import com.example.pokemonbattle.card.Move;

public class BattleEngine {

    public static int damage(Fighter attacker, Fighter defender,
                             Move move, double roll, boolean crit) {
        double base = ((2 * 50 / 5.0 + 2) * move.getPower()
                * attacker.attackFor(move) / defender.defenseFor(move)) / 50.0 + 2;
        double stab = attacker.hasType(move.getType()) ? 1.5 : 1.0;
        double eff = TypeChart.multiplier(move.getType(), defender.getTypes());
        double critMul = crit ? 1.5 : 1.0;
        return (int) Math.floor(base * stab * eff * critMul * roll);
    }
}
