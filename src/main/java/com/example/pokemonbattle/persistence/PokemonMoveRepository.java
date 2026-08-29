package com.example.pokemonbattle.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PokemonMoveRepository
        extends JpaRepository<PokemonMoveEntity, PokemonMoveId> {

    List<PokemonMoveEntity> findByPokemonId(int pokemonId);
}