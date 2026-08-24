package com.example.pokemonbattle;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class CardFactory {

    private final PokemonRepository pokemonRepository;
    private final MoveRepository moveRepository;
    private final PokemonMoveRepository pokemonMoveRepository;

    private List<PokemonEntity> allPokemon;
    private Map<Grade, List<PokemonEntity>> byGrade;
    private Map<Integer, MoveEntity> moveById;
    private Map<Integer, List<Integer>> learnable;

    public CardFactory(PokemonRepository pokemonRepository,
                       MoveRepository moveRepository,
                       PokemonMoveRepository pokemonMoveRepository) {
        this.pokemonRepository = pokemonRepository;
        this.moveRepository = moveRepository;
        this.pokemonMoveRepository = pokemonMoveRepository;
    }

    private synchronized void loadIfNeeded() {
        if (allPokemon != null) return;

        allPokemon = pokemonRepository.findAll();

        moveById = moveRepository.findAll().stream()
                .collect(Collectors.toMap(MoveEntity::getId, m -> m));

        byGrade = allPokemon.stream()
                .collect(Collectors.groupingBy(p -> Grade.of(p.total())));

        learnable = pokemonMoveRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        PokemonMoveEntity::getPokemonId,
                        Collectors.mapping(PokemonMoveEntity::getMoveId, Collectors.toList())));

        System.out.println("카드 풀 준비 완료: 포켓몬 " + allPokemon.size()
                + ", 기술 " + moveById.size()
                + ", 관계 " + learnable.values().stream().mapToInt(List::size).sum());
    }

    public List<Card> deal(int count) {
        loadIfNeeded();

        Set<Integer> used = new HashSet<>();
        List<Card> hand = new ArrayList<>();

        while (hand.size() < count) {
            PokemonEntity p = pickByGrade();
            if (p == null || !used.add(p.getId())) continue;
            hand.add(new Card(p.getId(), p.getName(), pickMoves(p)));
        }
        return hand;
    }

    private PokemonEntity pickByGrade() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        int acc = 0;

        for (Grade g : Grade.values()) {
            acc += g.weight;
            if (roll < acc) {
                List<PokemonEntity> pool = byGrade.get(g);
                if (pool == null || pool.isEmpty()) break;
                return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
            }
        }
        return allPokemon.get(ThreadLocalRandom.current().nextInt(allPokemon.size()));
    }

    private List<Move> pickMoves(PokemonEntity p) {
        List<Integer> ids = learnable.getOrDefault(p.getId(), List.of());

        List<MoveEntity> usable = ids.stream()
                .map(moveById::get)
                .filter(Objects::nonNull)
                .filter(m -> m.getPower() <= 110)
                .sorted(Comparator.comparingInt(
                        (MoveEntity m) -> m.getPower() * m.getAccuracy()).reversed())
                .toList();

        List<Move> picked = new ArrayList<>();
        Set<String> usedTypes = new HashSet<>();

        for (MoveEntity m : usable) {
            if (m.getType().equals(p.getType1())) {
                picked.add(m.toMove());
                usedTypes.add(m.getType());
                break;
            }
        }

        for (MoveEntity m : usable) {
            if (picked.size() >= 4) break;
            if (usedTypes.add(m.getType())) picked.add(m.toMove());
        }

        for (MoveEntity m : usable) {
            if (picked.size() >= 4) break;
            boolean already = picked.stream().anyMatch(x -> x.getId() == m.getId());
            if (!already) picked.add(m.toMove());
        }

        return picked;
    }

    public Fighter fighterOf(int pokemonId) {
        loadIfNeeded();

        PokemonEntity p = allPokemon.stream()
                .filter(x -> x.getId() == pokemonId)
                .findFirst()
                .orElseThrow();

        List<String> types = new ArrayList<>();
        types.add(p.getType1());
        if (p.getType2() != null) types.add(p.getType2());

        return new Fighter(p.getName(), types,
                p.getBaseHp(), p.getBaseAttack(), p.getBaseDefense(),
                p.getBaseSpAttack(), p.getBaseSpDefense(), p.getBaseSpeed());
    }
}