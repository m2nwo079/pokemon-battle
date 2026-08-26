package com.example.pokemonbattle;

import java.util.List;

public class Fighter {
    private final String name;
    private final List<String> types;
    private final int maxHp;
    private int currentHp;
    private final int attack, defense, spAttack, spDefense, speed;

    public Fighter(String name, List<String> types, int baseHp, int baseAtk,
                   int baseDef, int baseSpAtk, int baseSpDef, int baseSpe) {
        this.name = name;
        this.types = types;
        this.maxHp = baseHp + 75;
        this.currentHp = this.maxHp;
        this.attack = baseAtk + 20;
        this.defense = baseDef + 20;
        this.spAttack = baseSpAtk + 20;
        this.spDefense = baseSpDef + 20;
        this.speed = baseSpe + 20;
    }

    public int attackFor(Move move) {
        return move.isPhysical() ? attack : spAttack;
    }

    public int defenseFor(Move move) {
        return move.isPhysical() ? defense : spDefense;
    }

    public boolean hasType(String type) {
        return types.contains(type);
    }

    public List<String> getTypes() {
        return types;
    }

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public boolean isFainted() {
        return currentHp <= 0;
    }

    public void takeDamage(int amount) {
        currentHp = Math.max(0, currentHp - amount);
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }
}
