package com.example.pokemonbattle;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BattleEngineTest {

    // 준비 — 실제 종족값을 넣어 둔다
    private final Fighter squirtle = new Fighter(
            "꼬부기", List.of("water"), 44, 48, 65, 50, 64, 43);

    private final Fighter charmander = new Fighter(
            "파이리", List.of("fire"), 39, 52, 43, 60, 50, 65);

    private final Move waterGun = new Move(
            55, "물대포", "water", 40, 100, false, 25, 0);

    private final Move ember = new Move(
            52, "불꽃세례", "fire", 40, 100, false, 25, 0);

    @Test
    void 물기술은_불포켓몬에게_두배로_들어간다() {
        int water = BattleEngine.damage(squirtle, charmander, waterGun, 1.0, false);
        int fire  = BattleEngine.damage(charmander, squirtle, ember, 1.0, false);

        // 같은 위력인데 상성 덕에 물 쪽이 더 아프다
        assertTrue(water > fire);
    }

    @Test
    void 난수가_작으면_데미지도_작다() {
        int water = BattleEngine.damage(squirtle, charmander, waterGun, 1.0, false);
        int fire  = BattleEngine.damage(charmander, squirtle, ember, 1.0, false);
    }

    @Test
    void 스피드가_빠른_쪽이_먼저_때린다() {
        Card cs = new Card(7, "squirtle", List.of(waterGun));
        Card cc = new Card(4, "charmander", List.of(ember));

        Battle b = new Battle(squirtle, cs, charmander, cc);
        TurnResult r = b.playTurn(waterGun.getId(), ember.getId());

        // 파이리 스피드 65+20, 꼬부기 43+20 → 파이리가 먼저
        Map<String, Object> first = (Map<String, Object>) r.events.get(0);
        assertEquals("p2", first.get("who"));
    }

    @Test
    void 언젠가는_승부가_난다() {
        Card cs = new Card(7, "squirtle", List.of(waterGun));
        Card cc = new Card(4, "charmander", List.of(ember));
        Battle b = new Battle(squirtle, cs, charmander, cc);

        TurnResult r = null;
        for (int i = 0; i < 30 && (r == null || r.winner == null); i++) {
            r = b.playTurn(waterGun.getId(), ember.getId());
        }
        assertNotNull(r.winner);
    }
}