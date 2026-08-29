package com.example.pokemonbattle.persistence;

import com.example.pokemonbattle.card.Move;
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

    private String ailment;
    private int ailmentChance;

    protected MoveEntity() {}

    public MoveEntity(int id, String name, String type, int power,
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

    public Move toMove() {
        return new Move(id, name, type, power, accuracy, physical, maxPp, drain,
                ailment, ailmentChance);
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