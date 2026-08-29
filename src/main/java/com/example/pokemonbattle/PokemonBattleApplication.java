package com.example.pokemonbattle;

import com.example.pokemonbattle.card.Card;
import com.example.pokemonbattle.card.CardFactory;
import com.example.pokemonbattle.persistence.PokemonSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class PokemonBattleApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemonBattleApplication.class, args);
    }

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    public CommandLineRunner seedRunner(PokemonSeeder seeder, CardFactory cardFactory) {
        return args -> {
            for (String a : args) {
                if ("--seed".equals(a)) {
                    seeder.run();
                    System.exit(0);
                }
                if ("--deal".equals(a)) {
                    for (Card c : cardFactory.deal(6)) {
                        System.out.println(c.toPayload(true));
                    }
                    System.exit(0);
                }
            }
        };
    }
}