package com.example.pokemonbattle;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class PokemonSeeder {

    private static final String API = "https://pokeapi.co/api/v2";

    private static final List<Integer> TARGETS = List.of(
            1, 4, 7, 12, 25, 26, 31, 34, 38, 45,
            51, 59, 62, 65, 68, 71, 76, 78, 82, 89,
            91, 94, 97, 103, 106, 107, 110, 112, 115, 121,
            125, 126, 130, 131, 134, 135, 136, 143, 149, 150
    );

    private final PokemonRepository pokemonRepository;
    private final MoveRepository moveRepository;
    private final RestClient http;

    public PokemonSeeder(PokemonRepository pokemonRepository,
                         MoveRepository moveRepository,
                         RestClient http) {
        this.pokemonRepository = pokemonRepository;
        this.moveRepository = moveRepository;
        this.http = http;
    }

    public void run() {
        System.out.println("=== 시딩 시작 ===");

        Set<String> moveNames = new HashSet<>();

        for (int id : TARGETS) {
            Map<String, Object> data = get(API + "/pokemon/" + id);
            if (data == null) continue;

            pokemonRepository.save(toPokemon(id, data));
            moveNames.addAll(moveNamesOf(data));

            System.out.println("포켓몬 " + id + " 저장");
            sleep(100);
        }

        System.out.println("중복 제거 후 기술 " + moveNames.size() + "개");

        int saved = 0;
        for (String name : moveNames) {
            Map<String, Object> data = get(API + "/move/" + name);
            if (data == null) continue;

            MoveEntity move = toMove(data);
            if (move == null) continue;

            moveRepository.save(move);
            saved++;
            if (saved % 50 == 0) System.out.println("기술 " + saved + "개 저장");
            sleep(100);
        }

        System.out.println("=== 끝. 포켓몬 " + pokemonRepository.count()
                + "마리, 기술 " + moveRepository.count() + "개 ===");
    }

    @SuppressWarnings("unchecked")
    private PokemonEntity toPokemon(int id, Map<String, Object> data) {
        String name = (String) data.get("name");

        List<Map<String, Object>> types = (List<Map<String, Object>>) data.get("types");
        String type1 = typeNameAt(types, 0);
        String type2 = types.size() > 1 ? typeNameAt(types, 1) : null;

        Map<String, Integer> s = statsOf(data);

        return new PokemonEntity(id, name, type1, type2,
                s.get("hp"), s.get("attack"), s.get("defense"),
                s.get("special-attack"), s.get("special-defense"), s.get("speed"));
    }

    @SuppressWarnings("unchecked")
    private MoveEntity toMove(Map<String, Object> data) {
        Object power = data.get("power");
        if (power == null) return null;

        Object accuracy = data.get("accuracy");
        Object pp = data.get("pp");
        if (accuracy == null || pp == null) return null;

        int id = (int) data.get("id");
        String name = (String) data.get("name");
        String type = (String) ((Map<String, Object>) data.get("type")).get("name");
        String klass = (String) ((Map<String, Object>) data.get("damage_class")).get("name");

        return new MoveEntity(id, name, type,
                (int) power, (int) accuracy,
                "physical".equals(klass), (int) pp);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String url) {
        try {
            return http.get().uri(url).retrieve().body(Map.class);
        } catch (Exception e) {
            System.out.println("실패: " + url + " (" + e.getMessage() + ")");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String typeNameAt(List<Map<String, Object>> types, int i) {
        return (String) ((Map<String, Object>) types.get(i).get("type")).get("name");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> statsOf(Map<String, Object> data) {
        Map<String, Integer> out = new HashMap<>();
        for (Map<String, Object> stat : (List<Map<String, Object>>) data.get("stats")) {
            String key = (String) ((Map<String, Object>) stat.get("stat")).get("name");
            out.put(key, (int) stat.get("base_stat"));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<String> moveNamesOf(Map<String, Object> data) {
        List<String> out = new ArrayList<>();
        for (Map<String, Object> m : (List<Map<String, Object>>) data.get("moves")) {
            out.add((String) ((Map<String, Object>) m.get("move")).get("name"));
        }
        return out;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
