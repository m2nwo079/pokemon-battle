package com.example.pokemonbattle;

import java.util.concurrent.ThreadLocalRandom;

public class Battle {

    private static final int MAX_TURNS = 30;

    private final Fighter f1, f2;
    private final Card c1, c2;
    private int turn = 0;

    public Battle(Fighter f1, Card c1, Fighter f2, Card c2) {
        this.f1 = f1; this.c1 = c1;
        this.f2 = f2; this.c2 = c2;
    }

    public TurnResult playTurn(int move1Id, int move2Id) {
        turn++;
        TurnResult r = new TurnResult();

        Move m1 = resolveMove(c1, move1Id);
        Move m2 = resolveMove(c2, move2Id);

        boolean p1First = f1.getSpeed() >= f2.getSpeed();

        if (p1First) {
            attack("p1", f1, c1, m1, f2, r);
            if (!f2.isFainted()) attack("p2", f2, c2, m2, f1, r);
        } else {
            attack("p2", f2, c2, m2, f1, r);
            if (!f1.isFainted()) attack("p1", f1, c1, m1, f2, r);
        }

        if (f2.isFainted()) { r.faint("p2"); r.winner = "p1"; }
        else if (f1.isFainted()) { r.faint("p1"); r.winner = "p2"; }
        else if (turn >= MAX_TURNS) r.winner = byHpRatio();

        return r;
    }

    private void attack(String who, Fighter atk, Card card, Move move,
                        Fighter def, TurnResult r) {
        card.usePp(move.getId());

        if (ThreadLocalRandom.current().nextInt(100) >= move.getAccuracy()) {
            r.miss(who, move);
            return;
        }

        double roll = 0.85 + ThreadLocalRandom.current().nextDouble() * 0.15;
        int damage = BattleEngine.damage(atk, def, move, roll);
        def.takeDamage(damage);

        double eff = TypeChart.multiplier(move.getType(), def.getTypes());
        r.hit(who, move, damage, eff, def.getCurrentHp());
    }

    private Move resolveMove(Card card, int moveId) {
        boolean anyLeft = card.getMoves().stream()
                .anyMatch(m -> card.ppLeft(m.getId()) > 0);

        if (!anyLeft) return STRUGGLE;

        Move chosen = card.getMoves().stream()
                .filter(m -> m.getId() == moveId)
                .findFirst()
                .orElse(card.getMoves().get(0));

        return card.ppLeft(chosen.getId()) > 0 ? chosen : STRUGGLE;
    }

    private static final Move STRUGGLE =
            new Move(-1, "struggle", "none",
                    50, 100, true, 999);

    private String byHpRatio() {
        double r1 = (double) f1.getCurrentHp() / f1.getMaxHp();
        double r2 = (double) f2.getCurrentHp() / f2.getMaxHp();
        if (r1 > r2) return "p1";
        if (r2 > r1) return "p2";
        return "draw";
    }

    public Fighter getF1() {
        return f1;
    }

    public Fighter getF2() {
        return f2;
    }

    public int getTurn() {
        return turn;
    }
}