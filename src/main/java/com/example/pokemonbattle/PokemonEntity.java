package com.example.pokemonbattle;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon")
public class PokemonEntity {

    @Id
    private int id;
    private String name;
    private String type1;
    private String type2;
    private int baseHp;
    private int baseAttack;
    private int baseDefense;
    private int baseSpAttack;
    private int baseSpDefense;
    private int baseSpeed;

    protected PokemonEntity() {}

    public PokemonEntity(int id, String name, String type1, String type2,
                         int baseHp, int baseAttack, int baseDefense,
                         int baseSpAttack, int baseSpDefense, int baseSpeed) {
        this.id = id;
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseSpAttack = baseSpAttack;
        this.baseSpDefense = baseSpDefense;
        this.baseSpeed = baseSpeed;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType1() {
        return type1;
    }

    public String getType2() {
        return type2;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public int getBaseSpAttack() {
        return baseSpAttack;
    }

    public int getBaseSpDefense() {
        return baseSpDefense;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public int total() {
        return baseHp + baseAttack + baseDefense
                + baseSpAttack + baseSpDefense + baseSpeed;
    }
}
