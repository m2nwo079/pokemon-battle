package com.example.pokemonbattle;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RoomRegistry {
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public GameRoom create() {
        String code;
        do {
            code = randomCode();
        } while (rooms.containsKey(code));
        GameRoom room = new GameRoom(code);
        rooms.put(code, room);
        return room;
    }

    public GameRoom find(String code) {
        return rooms.get(code);
    }

    private String randomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }

    public void remove(String code) {
        rooms.remove(code);
    }
}
