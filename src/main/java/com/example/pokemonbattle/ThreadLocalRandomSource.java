package com.example.pokemonbattle;

import java.util.concurrent.ThreadLocalRandom;

public class ThreadLocalRandomSource implements RandomSource {

    @Override
    public int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    @Override
    public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    @Override
    public boolean coinFlip() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
