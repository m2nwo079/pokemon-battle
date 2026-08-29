package com.example.pokemonbattle.game;

public interface RandomSource {

    int nextInt(int bound);

    double nextDouble();

    boolean coinFlip();
}
