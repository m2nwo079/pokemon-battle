package com.example.pokemonbattle;

public class Move {
    private final int id;
    private final String name;
    private final String type;      // "fire", "water" 같은 타입
    private final int power;
    private final int accuracy;      // 명중률 0\~100
    private final boolean physical;  // true면 물리, false면 특수
    private final int maxPp;

    public Move(int id, String name, String type, int power, int accuracy, boolean physical, int maxPp) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.physical = physical;
        this.maxPp = maxPp;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getPower() {
        return power;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public boolean isPhysical() {
        return physical;
    }

    public int getMaxPp() {
        return maxPp;
    }
}