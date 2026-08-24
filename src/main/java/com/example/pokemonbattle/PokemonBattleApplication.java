package com.example.pokemonbattle;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PokemonBattleApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemonBattleApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedRunner(PokemonSeeder seeder) {
        return args -> {
            for (String a : args) {
                if ("--seed".equals(a)) {
                    seeder.run();
                    System.exit(0);   // 시딩만 하고 서버는 안 띄운다
                }
            }
        };
    }

    @Bean
    public org.springframework.web.client.RestClient restClient() {
        return org.springframework.web.client.RestClient.create();
    }
}