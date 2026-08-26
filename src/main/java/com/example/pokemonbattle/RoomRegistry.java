package com.example.pokemonbattle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RoomRegistry {

    private static final Logger log = LoggerFactory.getLogger(RoomRegistry.class);

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public GameRoom create() {
        while (true) {
            String code = randomCode();
            GameRoom room = new GameRoom(code);

            if (rooms.putIfAbsent(code, room) == null) {
                log.info("방 생성 {} (현재 {}개)", code, rooms.size());
                return room;
            }
        }
    }

    public GameRoom find(String code) {
        if (code == null) return null;
        return rooms.get(code.trim().toUpperCase());
    }

    public void remove(String code) {
        if (rooms.remove(code) != null) {
            log.info("방 삭제 {} (남은 {}개)", code, rooms.size());
        }
    }

    public int size() {
        return rooms.size();
    }

    private String randomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }
}