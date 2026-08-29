package com.example.pokemonbattle.web;

import org.springframework.web.socket.WebSocketSession;

public class Player {
    public final WebSocketSession session;
    public Player(WebSocketSession session) {
        this.session = session;
    }
}