package com.example.pokemonbattle;

import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TypeChart {

    private static final Map<String, Map<String, Double>> CHART = load();

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Double>> load() {
        try (InputStream in = new ClassPathResource("typechart.json").getInputStream()) {
            return new ObjectMapper().readValue(in, Map.class);
        } catch (Exception e) {
            System.err.println("typechart.json 로드 실패: " + e.getMessage());
            return Map.of();
        }
    }

    public static double multiplier(String moveType, List<String> defenderTypes) {
        Map<String, Double> row = CHART.get(moveType);
        if (row == null) return 1.0;

        double result = 1.0;
        for (String t : defenderTypes) {
            double m = row.getOrDefault(t, 1.0);
            if (m == 0.0) return 0.0;
            result *= m;
        }

        if (result >= 4.0) return 3.0;
        if (result <= 0.25) return 0.5;
        return result;
    }
}