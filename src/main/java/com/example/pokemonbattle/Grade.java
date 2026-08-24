package com.example.pokemonbattle;

public enum Grade {
    S(12), A(33), B(33), C(22);   // 뽑힐 확률(%)

    public final int weight;

    Grade(int weight) { this.weight = weight; }

    public static Grade of(int total) {
        if (total >= 520) return S;
        if (total >= 470) return A;
        if (total >= 420) return B;
        return C;
    }
}