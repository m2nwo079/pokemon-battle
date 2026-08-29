package com.example.pokemonbattle;

import com.example.pokemonbattle.card.Card;
import com.example.pokemonbattle.card.Move;
import com.example.pokemonbattle.game.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BattleEngineTest {

    private final Fighter squirtle = new Fighter(
            "꼬부기", List.of("water"), 44, 48, 65, 50, 64, 43);

    private final Fighter charmander = new Fighter(
            "파이리", List.of("fire"), 39, 52, 43, 60, 50, 65);

    private final Move waterGun = new Move(
            55, "물대포", "water", 40, 100, false, 25, 0, "none", 0);

    private final Move ember = new Move(
            52, "불꽃세례", "fire", 40, 100, false, 25, 0, "none", 0);


    @Test
    void 물기술은_불포켓몬에게_두배로_들어간다() {
        int water = BattleEngine.damage(squirtle, charmander, waterGun, 1.0, false);
        int fire  = BattleEngine.damage(charmander, squirtle, ember, 1.0, false);

        assertTrue(water > fire);
    }

    @Test
    void 급소면_데미지가_더_크다() {
        int normal = BattleEngine.damage(squirtle, charmander, waterGun, 1.0, false);
        int crit   = BattleEngine.damage(squirtle, charmander, waterGun, 1.0, true);
        assertTrue(crit > normal);
    }

    @Test
    void 난수롤이_작으면_데미지도_작다() {
        int low  = BattleEngine.damage(squirtle, charmander, waterGun, 0.85, false);
        int high = BattleEngine.damage(squirtle, charmander, waterGun, 1.0, false);
        assertTrue(low <= high);
    }


    @Test
    void 스피드가_빠른_쪽이_먼저_때린다() {
        Card cs = new Card(7, "squirtle", List.of(waterGun));
        Card cc = new Card(4, "charmander", List.of(ember));

        Battle b = new Battle(squirtle, cs, charmander, cc, new ScriptedRandom());
        TurnResult r = b.playTurn(waterGun.getId(), ember.getId());

        Map<String, Object> first = (Map<String, Object>) r.events.get(0);
        assertEquals("p2", first.get("who"));
    }

    @Test
    void 스피드_동점이면_동전던지기로_선공이_갈린다() {
        // 같은 종족값의 두 포켓몬을 만들어 스피드를 동점으로 만든다
        Fighter a = new Fighter("A", List.of("normal"), 50, 50, 50, 50, 50, 50);
        Fighter b = new Fighter("B", List.of("normal"), 50, 50, 50, 50, 50, 50);
        Move tackle = new Move(1, "몸통박치기", "normal", 40, 100, true, 35, 0, "none", 0);

        Card ca1 = new Card(1, "A", List.of(tackle));
        Card cb1 = new Card(2, "B", List.of(tackle));
        Battle p1First = new Battle(a, ca1, b, cb1, new ScriptedRandom().coin(true));
        TurnResult r1 = p1First.playTurn(tackle.getId(), tackle.getId());
        assertEquals("p1", ((Map<String, Object>) r1.events.get(0)).get("who"));

        // coinFlip=false → p2 선공
        Fighter a2 = new Fighter("A", List.of("normal"), 50, 50, 50, 50, 50, 50);
        Fighter b2 = new Fighter("B", List.of("normal"), 50, 50, 50, 50, 50, 50);
        Card ca2 = new Card(1, "A", List.of(tackle));
        Card cb2 = new Card(2, "B", List.of(tackle));
        Battle p2First = new Battle(a2, ca2, b2, cb2, new ScriptedRandom().coin(false));
        TurnResult r2 = p2First.playTurn(tackle.getId(), tackle.getId());
        assertEquals("p2", ((Map<String, Object>) r2.events.get(0)).get("who"));
    }

    @Test
    void 명중률_판정에서_빗나가면_miss_이벤트가_나온다() {
        Move shaky = new Move(3, "흔들기", "normal", 40, 80, true, 20, 0, "none", 0);
        Card ca = new Card(1, "A", List.of(shaky));
        Card cb = new Card(2, "B", List.of(waterGun));

        Fighter a = new Fighter("A", List.of("normal"), 50, 50, 50, 50, 50, 99);
        Fighter b = new Fighter("B", List.of("water"), 50, 50, 50, 50, 50, 1);

        ScriptedRandom rng = new ScriptedRandom().ints(90);
        Battle battle = new Battle(a, ca, b, cb, rng);
        TurnResult r = battle.playTurn(shaky.getId(), waterGun.getId());

        Map<String, Object> first = (Map<String, Object>) r.events.get(0);
        assertEquals("miss", first.get("type"));
        assertEquals("p1", first.get("who"));
    }

    @Test
    void 언젠가는_승부가_난다() {
        Card cs = new Card(7, "squirtle", List.of(waterGun));
        Card cc = new Card(4, "charmander", List.of(ember));
        Battle b = new Battle(squirtle, cs, charmander, cc, new ScriptedRandom());

        TurnResult r = null;
        for (int i = 0; i < 30 && (r == null || r.winner == null); i++) {
            r = b.playTurn(waterGun.getId(), ember.getId());
        }
        assertNotNull(r.winner);
    }

    @Test
    void 발버둥은_시전자에게_반동_데미지를_준다() {
        Move onePp = new Move(1, "몸통박치기", "normal", 40, 100, true, 1, 0, "none", 0);
        Move noDamage = new Move(2, "울음소리", "normal", 0, 100, false, 30, 0, "none", 0);

        Fighter attacker = new Fighter("A", List.of("normal"), 200, 80, 50, 50, 50, 99);
        Fighter dummy    = new Fighter("B", List.of("normal"), 200, 50, 10, 50, 50, 1);

        Card ca = new Card(1, "A", List.of(onePp));
        Card cb = new Card(2, "B", List.of(noDamage));

        Battle b = new Battle(attacker, ca, dummy, cb,
                new ScriptedRandom().ints(0, 1, 1, 0, 1, 1).doubles(0.5, 0.5));

        b.playTurn(onePp.getId(), noDamage.getId());
        int hpBeforeStruggle = attacker.getCurrentHp();

        b.playTurn(onePp.getId(), noDamage.getId());
        int hpAfterStruggle = attacker.getCurrentHp();

        int expectedRecoil = attacker.getMaxHp() / 4;
        assertEquals(expectedRecoil, hpBeforeStruggle - hpAfterStruggle,
                "발버둥 반동은 시전자 최대 HP의 1/4이어야 한다");
    }

    static class ScriptedRandom implements RandomSource {
        private final Deque<Integer> intQueue = new ArrayDeque<>();
        private final Deque<Double> doubleQueue = new ArrayDeque<>();
        private boolean coin = true;

        ScriptedRandom ints(int... values) {
            for (int v : values) intQueue.add(v);
            return this;
        }

        ScriptedRandom doubles(double... values) {
            for (double v : values) doubleQueue.add(v);
            return this;
        }

        ScriptedRandom coin(boolean value) {
            this.coin = value;
            return this;
        }

        @Override
        public int nextInt(int bound) {
            Integer v = intQueue.poll();
            if (v == null) return 0;
            return v % bound;
        }

        @Override
        public double nextDouble() {
            Double v = doubleQueue.poll();
            return v == null ? 0.5 : v;
        }

        @Override
        public boolean coinFlip() {
            return coin;
        }
    }
}