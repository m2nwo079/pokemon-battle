package com.example.pokemonbattle.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon_moves")
@IdClass(PokemonMoveId.class)
public class PokemonMoveEntity {

    @Id
    private int pokemonId;

    @Id
    private int moveId;

    protected PokemonMoveEntity() {}

    public PokemonMoveEntity(int pokemonId, int moveId) {
        this.pokemonId = pokemonId;
        this.moveId = moveId;
    }

    public int getPokemonId() { return pokemonId; }
    public int getMoveId() { return moveId; }
}
