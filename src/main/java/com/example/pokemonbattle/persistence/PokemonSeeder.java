package com.example.pokemonbattle.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class PokemonSeeder {

    private static final Logger log = LoggerFactory.getLogger(PokemonSeeder.class);

    private static final String API = "https://pokeapi.co/api/v2";

    private static final List<Integer> TARGETS = List.of(
            1, 2, 4, 5, 7, 8, 12, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
            28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
            38, 39, 40, 41, 42, 43, 45, 49, 51, 53,
            55, 57, 59, 62, 65, 68, 71, 73, 76, 78,
            80, 82, 85, 87, 89, 91, 94, 97, 99, 103,
            105, 106, 107, 110, 112, 113, 114, 115, 117, 119,
            121, 122, 124, 125, 126, 130, 131, 134, 135, 136,
            143, 148, 149, 150
    );

    private final PokemonRepository pokemonRepository;
    private final MoveRepository moveRepository;
    private final PokemonMoveRepository pokemonMoveRepository;
    private final RestClient http;

    public PokemonSeeder(PokemonRepository pokemonRepository,
                         MoveRepository moveRepository,
                         PokemonMoveRepository pokemonMoveRepository,
                         RestClient http) {
        this.pokemonRepository = pokemonRepository;
        this.moveRepository = moveRepository;
        this.pokemonMoveRepository = pokemonMoveRepository;
        this.http = http;
    }

    public void run() {
        log.info("시딩 시작");

        pokemonMoveRepository.deleteAllInBatch();
        moveRepository.deleteAllInBatch();
        pokemonRepository.deleteAllInBatch();
        log.info("기존 데이터 삭제 완료");
        Map<Integer, List<String>> learnable = new HashMap<>();
        Set<String> allMoveNames = new HashSet<>();

        for (int id : TARGETS) {
            Map<String, Object> data = get(API + "/pokemon/" + id);
            if (data == null) continue;

            Map<String, Object> species = get(API + "/pokemon-species/" + id);
            sleep(100);

            pokemonRepository.save(toPokemon(id, data, species));

            List<String> names = moveNamesOf(data);
            learnable.put(id, names);
            allMoveNames.addAll(names);

            log.info("포켓몬 {} 저장", id);
            sleep(100);
        }

        log.info("중복 제거 후 기술 {}개", allMoveNames.size());

        Map<String, Integer> savedMoveIds = new HashMap<>();

        int saved = 0;
        for (String name : allMoveNames) {
            Map<String, Object> data = get(API + "/move/" + name);
            if (data == null) continue;

            MoveEntity move = toMove(data);
            if (move == null) continue;

            moveRepository.save(move);
            savedMoveIds.put(name, move.getId());

            saved++;
            if (saved % 50 == 0) log.info("기술 {}개 저장", saved);
            sleep(100);
        }

        List<PokemonMoveEntity> links = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> e : learnable.entrySet()) {
            for (String name : e.getValue()) {
                Integer moveId = savedMoveIds.get(name);
                if (moveId != null) {
                    links.add(new PokemonMoveEntity(e.getKey(), moveId));
                }
            }
        }
        pokemonMoveRepository.saveAll(links);

        log.info("시딩 완료: 포켓몬 {}마리, 기술 {}개, 관계 {}줄",
                pokemonRepository.count(), moveRepository.count(), pokemonMoveRepository.count());
    }

    @SuppressWarnings("unchecked")
    private PokemonEntity toPokemon(int id, Map<String, Object> data,
                                    Map<String, Object> species) {
        String name = (String) data.get("name");
        if (species != null) name = koreanName(species, name);

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
        Object accuracy = data.get("accuracy");
        Object pp = data.get("pp");
        if (accuracy == null || pp == null) return null;

        Map<String, Object> meta = (Map<String, Object>) data.get("meta");
        if (meta == null) return null;

        // 여러 턴 묶는 기술(잠듦류 등)은 지금 범위 밖
        if (meta.get("max_turns") != null) return null;

        // 상태이상 정보 추출. 우리가 지원하는 셋만 남기고 나머지는 none 으로 정규화
        Map<String, Object> ailmentMap = (Map<String, Object>) meta.get("ailment");
        String ailment = ailmentMap == null ? "none" : (String) ailmentMap.get("name");
        if (!ailment.equals("burn") && !ailment.equals("poison")
                && !ailment.equals("paralysis")) {
            ailment = "none";
        }

        // 카테고리 필터: 공격기(damage/damage-heal)는 그대로 받고,
        // 그 외에는 우리가 지원하는 상태이상을 거는 변화기만 받는다
        Map<String, Object> category = (Map<String, Object>) meta.get("category");
        String cat = category == null ? "" : (String) category.get("name");
        boolean isDamage = "damage".equals(cat) || "damage-heal".equals(cat);
        if (!isDamage && ailment.equals("none")) return null;

        Object drainObj = meta.get("drain");
        int drain = drainObj == null ? 0 : (int) drainObj;

        // 위력: 공격기는 그대로, 위력 없는 변화기는 0
        Object powerObj = data.get("power");
        int power = powerObj == null ? 0 : (int) powerObj;

        int ailmentChance = meta.get("ailment_chance") == null
                ? 0 : (int) meta.get("ailment_chance");

        int id = (int) data.get("id");
        String name = koreanName(data, (String) data.get("name"));
        String type = (String) ((Map<String, Object>) data.get("type")).get("name");
        String klass = (String) ((Map<String, Object>) data.get("damage_class")).get("name");

        return new MoveEntity(id, name, type,
                power, (int) accuracy,
                "physical".equals(klass), (int) pp, drain,
                ailment, ailmentChance);
    }

    // ---------- 도우미 ----------

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String url) {
        try {
            return http.get().uri(url).retrieve().body(Map.class);
        } catch (Exception e) {
            log.warn("요청 실패: {} ({})", url, e.getMessage());
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

    @SuppressWarnings("unchecked")
    private String koreanName(Map<String, Object> data, String fallback) {
        Object namesObj = data.get("names");
        if (namesObj instanceof List<?> names) {
            for (Object o : names) {
                Map<String, Object> entry = (Map<String, Object>) o;
                Map<String, Object> lang = (Map<String, Object>) entry.get("language");
                if (lang != null && "ko".equals(lang.get("name"))) {
                    return (String) entry.get("name");
                }
            }
        }
        return fallback;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
