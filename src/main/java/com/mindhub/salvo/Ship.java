package com.mindhub.salvo;


import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GamePlayerId")
    private GamePlayer gamePlayer;


    @ElementCollection
    @Column(name="ShipLocations")
    private List<String> locations = new ArrayList<>();

    private String type;


    public Ship() { }

    public Ship(String type, List<String> locations, GamePlayer gamePlayer ) {
        this.type = type ;
        this.locations = locations;
        this.gamePlayer = gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public List<String> getLocations() {
        return locations;
    }

    public String getType() {
        return type;
    }

    public long getId() {
        return id;
    }
}






