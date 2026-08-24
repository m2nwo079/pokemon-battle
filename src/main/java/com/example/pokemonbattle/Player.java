package com.example.pokemonbattle;

import org.springframework.web.socket.WebSocketSession;

public class Player {
    public final WebSocketSession session;
    public final String token;          // 재접속용 신분증
    public Player(WebSocketSession session, String token) {
        this.session = session;
        this.token = token;
    }
}
