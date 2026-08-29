package com.example.pokemonbattle.web;

import java.util.List;

public record RoundEnd(String type, String roundWinner, List<Integer> wins) {
    public RoundEnd(String roundWinner, List<Integer> wins) {
        this("round_end", roundWinner, wins);
    }
}
