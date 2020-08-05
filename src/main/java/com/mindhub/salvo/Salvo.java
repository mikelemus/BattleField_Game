package com.mindhub.salvo;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_Id")
    private GamePlayer gamePlayer;

    private int turn;

    @ElementCollection
    @Column(name="SalvoLocations")
    private List<String> locations = new ArrayList<>();

    public Salvo (){ }

    public Salvo (GamePlayer gamePlayer, int turn, List<String> locations){
        this.gamePlayer = gamePlayer;
        this.turn = turn;
        this.locations = locations;
    }

    public Map<String, Object> salvoHitsDto() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", turn);
        dto.put("locations", locations.stream().filter(location ->  this.getGamePlayer().getOpponent().getShips().stream().flatMap(ship -> ship.getLocations().stream()).collect(Collectors.toList()).contains(location)).collect(Collectors.toList()) );
        return dto;
    }

    public long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }


}

