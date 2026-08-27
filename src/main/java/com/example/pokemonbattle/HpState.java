package com.example.pokemonbattle;

public record HpState(int current, int max) {
    public static HpState of(Fighter f) {
        return new HpState(f.getCurrentHp(), f.getMaxHp());
    }
}