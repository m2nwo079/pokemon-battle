package com.example.pokemonbattle.game;

import com.example.pokemonbattle.web.Player;
import com.example.pokemonbattle.card.Card;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {

    public final String code;
    public final List<Player> players = new ArrayList<>();

    public int round = 0;
    public int[] wins = new int[2];

    @SuppressWarnings("unchecked")
    public List<Card>[] hands = new List[2];
    public Card[] played = new Card[2];
    public Integer[] chosenMove = new Integer[2];
    public Battle battle;

    public boolean finished = false;

    public boolean[] rematchReady = new boolean[2];

    public GameRoom(String code) { this.code = code; }

    public int indexOf(Player p) { return players.indexOf(p); }

    public Player opponentOf(int me) {
        int other = 1 - me;
        if (other < 0 || other >= players.size()) return null;
        return players.get(other);
    }

    public boolean bothPlayed() { return played[0] != null && played[1] != null; }
    public boolean bothChose()  { return chosenMove[0] != null && chosenMove[1] != null; }

    public void clearTurn() { chosenMove[0] = null; chosenMove[1] = null; }
    public void clearRound() { played[0] = null; played[1] = null; battle = null; clearTurn(); }

    public void resetForRematch() {
        wins = new int[2];
        round = 0;
        hands[0] = null;
        hands[1] = null;
        clearRound();
        finished = false;
        rematchReady[0] = false;
        rematchReady[1] = false;
    }
}