package com.example.pokemonbattle;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    public final String code;
    public final List<Player> players = new ArrayList<>();
    public GameRoom(String code) {
        this.code = code;
    }
}