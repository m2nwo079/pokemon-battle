package com.example.pokemonbattle;

import java.util.List;
import java.util.Map;

public class TypeChart {
    private static final Map<String, Map<String, Double>> CHART = Map.of(
            "fire", Map.of(
                    "grass", 2.0, "ice", 2.0, "bug", 2.0,
                    "water", 0.5, "fire", 0.5, "rock", 0.5, "dragon", 0.5),
            "water", Map.of(
                    "fire", 2.0, "ground", 2.0, "rock", 2.0,
                    "water", 0.5, "grass", 0.5, "dragon", 0.5),
            "grass", Map.of(
                    "water", 2.0, "ground", 2.0, "rock", 2.0,
                    "fire", 0.5, "grass", 0.5, "poison", 0.5,
                    "flying", 0.5, "bug", 0.5, "dragon", 0.5),
            "electric", Map.of(
                    "water", 2.0, "flying", 2.0,
                    "electric", 0.5, "grass", 0.5, "dragon", 0.5,
                    "ground", 0.0)
    );

    public static double multiplier(String moveType, List<String> defenderTypes) {
        Map<String, Double> row = CHART.get(moveType);
        if (row == null) return 1.0;

        double result = 1.0;
        for (String t : defenderTypes) {
            result *= row.getOrDefault(t, 1.0);
        }
        return result;
    }
}