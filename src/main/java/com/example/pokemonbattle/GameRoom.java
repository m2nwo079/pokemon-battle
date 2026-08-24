package com.example.pokemonbattle;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {

    public final String code;
    public final List<Player> players = new ArrayList<>();

    public int round = 0;
    public int[] wins = new int[2];

    public List<Card>[] hands = new List[2];
    public Card[] played = new Card[2];
    public Integer[] chosenMove = new Integer[2];
    public Battle battle;

    public GameRoom(String code) {
        this.code = code;
    }

    public int indexOf(Player p) {
        return players.indexOf(p);
    }

    public Player opponentOf(int me) {
        return players.get(1 - me);
    }

    public boolean bothPlayed() {
        return played[0] != null && played[1] != null;
    }

    public boolean bothChose()  {
        return chosenMove[0] != null && chosenMove[1] != null;
    }

    public void clearTurn() {
        chosenMove[0] = null; chosenMove[1] = null;
    }

    public void clearRound() {
        played[0] = null; played[1] = null; battle = null; clearTurn();
    }
}