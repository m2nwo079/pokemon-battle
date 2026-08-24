package com.example.pokemonbattle;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MoveRepository extends JpaRepository<MoveEntity, Integer> {
    Optional<MoveEntity> findByName(String name);
}
