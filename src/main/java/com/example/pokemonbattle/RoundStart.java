package com.example.pokemonbattle;

import java.util.List;
import java.util.Map;

public record RoundStart(
        String type,
        int round,
        String me,
        Map<String, Object> myCard,
        Map<String, Object> opponentCard,
        HpState myHp,
        HpState opponentHp,
        List<String> myTypes,
        List<String> opponentTypes
) {
    public RoundStart(int round, String me,
                      Map<String, Object> myCard, Map<String, Object> opponentCard,
                      HpState myHp, HpState opponentHp,
                      List<String> myTypes, List<String> opponentTypes) {
        this("round_start", round, me, myCard, opponentCard,
                myHp, opponentHp, myTypes, opponentTypes);
    }
}