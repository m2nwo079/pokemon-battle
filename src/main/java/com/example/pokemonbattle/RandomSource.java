package com.example.pokemonbattle;

public interface RandomSource {

    int nextInt(int bound);

    double nextDouble();

    boolean coinFlip();
}
