package com.example.pokemonbattle;

public class Move {
    private final int id;
    private final String name;
    private final String type;
    private final int power;
    private final int accuracy;
    private final boolean physical;
    private final int maxPp;
    private final int drain;
    private final String ailment;
    private final int ailmentChance;

    public Move(int id, String name, String type, int power,
                int accuracy, boolean physical, int maxPp, int drain,
                String ailment, int ailmentChance) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.physical = physical;
        this.maxPp = maxPp;
        this.drain = drain;
        this.ailment = ailment;
        this.ailmentChance = ailmentChance;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getPower() { return power; }
    public int getAccuracy() { return accuracy; }
    public boolean isPhysical() { return physical; }
    public int getMaxPp() { return maxPp; }
    public int getDrain() { return drain; }
    public String getAilment() { return ailment; }
    public int getAilmentChance() { return ailmentChance; }
}