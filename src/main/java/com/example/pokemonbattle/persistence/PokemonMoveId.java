package com.example.pokemonbattle.persistence;

import java.io.Serializable;
import java.util.Objects;

public class PokemonMoveId implements Serializable {
    private int pokemonId;
    private int moveId;

    public PokemonMoveId() {}

    public PokemonMoveId(int pokemonId, int moveId) {
        this.pokemonId = pokemonId;
        this.moveId = moveId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PokemonMoveId that)) return false;
        return pokemonId == that.pokemonId && moveId == that.moveId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pokemonId, moveId);
    }
}