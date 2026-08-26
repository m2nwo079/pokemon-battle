package com.example.pokemonbattle;

import jakarta.persistence.*;

@Entity
@Table(name = "moves")
public class MoveEntity {

    @Id
    private int id;

    private String name;
    private String type;
    private int power;
    private int accuracy;
    private boolean physical;
    private int maxPp;
    private int drain;

    protected MoveEntity() {}

    public MoveEntity(int id, String name, String type, int power,
                      int accuracy, boolean physical, int maxPp, int drain) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.physical = physical;
        this.maxPp = maxPp;
        this.drain = drain;
    }

    public Move toMove() {
        return new Move(id, name, type, power, accuracy, physical, maxPp, drain);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getPower() { return power; }
    public int getAccuracy() { return accuracy; }
    public boolean isPhysical() { return physical; }
    public int getMaxPp() { return maxPp; }
    public int getDrain() { return drain; }
}