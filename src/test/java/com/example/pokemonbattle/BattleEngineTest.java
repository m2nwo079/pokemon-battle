package com.example.pokemonbattle;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BattleEngineTest {

    // 준비 — 실제 종족값을 넣어 둔다
    private final Fighter squirtle = new Fighter(
            "꼬부기", List.of("water"), 44, 48, 65, 50, 64, 43);

    private final Fighter charmander = new Fighter(
            "파이리", List.of("fire"), 39, 52, 43, 60, 50, 65);

    private final Move waterGun = new Move(
            55, "물대포", "water", 40, 100, false, 25);

    private final Move ember = new Move(
            52, "불꽃세례", "fire", 40, 100, false, 25);

    @Test
    void 물기술은_불포켓몬에게_두배로_들어간다() {
        int water = BattleEngine.damage(squirtle, charmander, waterGun, 1.0);
        int fire  = BattleEngine.damage(charmander, squirtle, ember, 1.0);

        // 같은 위력인데 상성 덕에 물 쪽이 더 아프다
        assertTrue(water > fire);
    }

    @Test
    void 난수가_작으면_데미지도_작다() {
        int max = BattleEngine.damage(squirtle, charmander, waterGun, 1.0);
        int min = BattleEngine.damage(squirtle, charmander, waterGun, 0.85);

        assertTrue(min < max);
    }
}