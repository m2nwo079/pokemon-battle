package com.example.pokemonbattle.web;

import java.util.List;

public record GameEnd(String type, String winner, List<Integer> wins) {
    public GameEnd(String winner, List<Integer> wins) {
        this("game_end", winner, wins);
    }
}
