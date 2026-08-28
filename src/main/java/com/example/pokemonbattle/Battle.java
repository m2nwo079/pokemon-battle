package com.example.pokemonbattle;

public class Battle {

    private static final int MAX_TURNS = 30;

    private final Fighter f1, f2;
    private final Card c1, c2;
    private final RandomSource rng;
    private int turn = 0;

    public Battle(Fighter f1, Card c1, Fighter f2, Card c2) {
        this(f1, c1, f2, c2, new ThreadLocalRandomSource());
    }

    public Battle(Fighter f1, Card c1, Fighter f2, Card c2, RandomSource rng) {
        this.f1 = f1; this.c1 = c1;
        this.f2 = f2; this.c2 = c2;
        this.rng = rng;
    }

    public TurnResult playTurn(int move1Id, int move2Id) {
        turn++;
        TurnResult r = new TurnResult();

        Move m1 = resolveMove(c1, move1Id);
        Move m2 = resolveMove(c2, move2Id);

        int spd1 = f1.getStatus().equals("paralysis") ? f1.getSpeed() / 4 : f1.getSpeed();
        int spd2 = f2.getStatus().equals("paralysis") ? f2.getSpeed() / 4 : f2.getSpeed();
        boolean p1First = spd1 != spd2 ? spd1 > spd2 : rng.coinFlip();

        if (p1First) {
            attack("p1", f1, c1, m1, f2, r);
            if (!f2.isFainted()) attack("p2", f2, c2, m2, f1, r);
        } else {
            attack("p2", f2, c2, m2, f1, r);
            if (!f1.isFainted()) attack("p1", f1, c1, m1, f2, r);
        }

        if (!f1.isFainted() && !f2.isFainted()) {
            tickStatus("p1", f1, r);
            if (!f1.isFainted()) tickStatus("p2", f2, r);
        }

        if (f2.isFainted()) { r.faint("p2"); r.winner = "p1"; }
        else if (f1.isFainted()) { r.faint("p1"); r.winner = "p2"; }
        else if (turn >= MAX_TURNS) r.winner = byHpRatio();

        return r;
    }

    private void tickStatus(String who, Fighter f, TurnResult r) {
        int dmg = f.tickStatusDamage();
        if (dmg > 0) {
            r.statusDamage(who, f.getStatus(), dmg, f.getCurrentHp());
        }
    }

    private void attack(String who, Fighter atk, Card card, Move move,
                        Fighter def, TurnResult r) {
        if (atk.getStatus().equals("paralysis")
                && rng.nextInt(100) < 25) {
            r.paralyzed(who);
            return;
        }

        card.usePp(move.getId());
        int ppLeft = card.ppLeft(move.getId());

        if (rng.nextInt(100) >= move.getAccuracy()) {
            r.miss(who, move, ppLeft);
            return;
        }

        int damage = 0;
        int healed = 0;
        double eff = 1.0;
        boolean crit = false;
        boolean stab = false;

        if (move.getPower() > 0) {
            crit = rng.nextInt(16) == 0;
            double roll = 0.85 + rng.nextDouble() * 0.15;
            damage = BattleEngine.damage(atk, def, move, roll, crit);

            if (atk.getStatus().equals("burn") && move.isPhysical()) {
                damage = Math.max(1, damage / 2);
            }

            def.takeDamage(damage);

            if (move.getDrain() > 0) {
                healed = Math.max(1, damage * move.getDrain() / 100);
                atk.heal(healed);
            }

            eff = TypeChart.multiplier(move.getType(), def.getTypes());
            stab = atk.hasType(move.getType());
            r.hit(who, move, damage, eff, stab, def.getCurrentHp(), ppLeft, healed, crit, atk.getCurrentHp());
        } else {
            r.hit(who, move, 0, 1.0, false, def.getCurrentHp(), ppLeft, 0, false, atk.getCurrentHp());
        }

        if (!def.isFainted() && !move.getAilment().equals("none")) {
            int chance = move.getAilmentChance() > 0 ? move.getAilmentChance() : 100;
            if (rng.nextInt(100) < chance) {
                boolean applied = def.applyStatus(move.getAilment());
                if (applied) {
                    String defWho = who.equals("p1") ? "p2" : "p1";
                    r.statusInflicted(defWho, move.getAilment());
                }
            }
        }
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
                    50, 100, true, 999, 0, "none", 0);

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