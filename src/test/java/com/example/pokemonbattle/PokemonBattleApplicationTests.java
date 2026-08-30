package com.example.pokemonbattle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DATABASE_URL", matches = ".+")
class PokemonBattleApplicationTests {

    @Test
    void contextLoads() {
    }

}