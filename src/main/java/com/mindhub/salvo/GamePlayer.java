package com.mindhub.salvo;


import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private LocalDateTime joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GameId")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PlayerId")
    private Player player;

    @OneToMany (mappedBy="gamePlayer", cascade =  CascadeType.ALL,fetch=FetchType.EAGER)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany (mappedBy="gamePlayer", cascade =  CascadeType.ALL,fetch=FetchType.EAGER)
    private Set<Salvo> salvos = new HashSet<>();




    public GamePlayer(){}

    public GamePlayer (Player player, Game game) {
        this.player = player;
        this.game = game;
        this.joinDate = LocalDateTime.now();
    }

    public List<Object> getHits(){
        List<Object> hits = new ArrayList<>();
        GamePlayer opponent = getOpponent();
        if (opponent != null){
            hits = this.salvos.stream().map(Salvo::salvoHitsDto).collect(toList());
        }
        return hits;
    }

    public GamePlayer getOpponent() {
        return this.game.getGamePlayers().stream().filter(gamePlayer -> gamePlayer.id != this.id).findFirst().orElse(null);
    }


    public List<String> getSinks(){
        List<String> salvosGp = this.salvos.stream().flatMap(salvo -> salvo.getLocations().stream()).collect(toList());
        List<String> sinkShips = getOpponent() != null ? getOpponent().getShips().stream().filter(ship -> salvosGp.containsAll(ship.getLocations())).map(ship -> ship.getType()).collect(toList()) : new ArrayList<>();
        return sinkShips;
    }

    public State getState(){
        State respuesta = State.PLAY;
        if (this.getShips().size() ==  0){
            respuesta = State.WAITING_SHIPS;
        } else {
            if (this.getOpponent() == null){
                respuesta = State.WAITING_OPPONENT;
            } else {
                if (this.getSalvos().size() > this.getOpponent().getSalvos().size()){
                    respuesta = State.WAIT;
                } else if (this.getSalvos().size() == this.getOpponent().getSalvos().size()){
                    if (this.getSalvos().size() == getOpponent().getSalvos().size() && this.getSinks().size() == 5 && this.getOpponent().getSinks().size() ==  5){
                        respuesta = State.TIE;
                    }
                    else if (this.getSalvos().size() == getOpponent().getSalvos().size() && this.getSinks().size() == 5){
                        respuesta = State.WIN;
                    } else if (this.getSalvos().size() == getOpponent().getSalvos().size() && this.getOpponent().getSinks().size() == 5){
                        respuesta = State.LOST;
                    }
                }
            }
        }
        return respuesta;
    }































    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void addShip (Ship ship){
        ship.setGamePlayer(this);
        this.ships.add(ship);
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void addSalvo (Salvo salvo){
        salvo.setGamePlayer(this);
        this.salvos.add(salvo);

    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public void setSalvos(Set<Salvo> salvos) {
        this.salvos = salvos;
    }

    public Score getScore (){
        return this.player.getScore(this.game);
    }

    }

