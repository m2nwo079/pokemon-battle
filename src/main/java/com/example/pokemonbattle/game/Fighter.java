package com.example.pokemonbattle.game;

import com.example.pokemonbattle.card.Move;

import java.util.List;

public class Fighter {
    private final String name;
    private final List<String> types;
    private final int maxHp;
    private int currentHp;
    private final int attack, defense, spAttack, spDefense, speed;

    private String status = "none";

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

    public String getStatus() { return status; }

    public boolean hasStatus() { return !"none".equals(status); }

    public boolean applyStatus(String newStatus) {
        if (hasStatus()) return false;
        if (isImmuneTo(newStatus)) return false;
        this.status = newStatus;
        return true;
    }

    private boolean isImmuneTo(String s) {
        return switch (s) {
            case "burn" -> types.contains("fire");
            case "paralysis" -> types.contains("electric");
            case "poison" -> types.contains("poison") || types.contains("steel");
            default -> true;
        };
    }

    public int tickStatusDamage() {
        int dmg = switch (status) {
            case "poison" -> Math.max(1, maxHp / 8);
            case "burn" -> Math.max(1, maxHp / 16);
            default -> 0;
        };
        if (dmg > 0) takeDamage(dmg);
        return dmg;
    }

}
