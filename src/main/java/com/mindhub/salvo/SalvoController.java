package com.mindhub.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String username, @RequestParam String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "username or password empty"), HttpStatus.FORBIDDEN);
        }
       Player player = playerRepository.findByUserName(username);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error", "Username already exists"), HttpStatus.CONFLICT);
        }
        Player newPlayer = playerRepository.save(new Player(username, passwordEncoder.encode(password)));
        return new ResponseEntity<>(makeMap("Id", newPlayer.getId()), HttpStatus.CREATED);
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }


    @RequestMapping("/games")
    private Map<String, Object> getAll(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        if(isGuest(authentication))
            dto.put("player", null);
        else{
            Player player = playerRepository.findByUserName(authentication.getName());
            dto.put("player", playerDto(player));
        }

        dto.put("games", gameRepository
               .findAll()
               .stream()
               .map(game -> gameDto(game))
               .collect(toList()));

        return dto;
    }


    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication){
        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "no access"), HttpStatus.UNAUTHORIZED);
        }
        Player player = playerRepository.findByUserName(authentication.getName());
        if (player == null) {
            return new ResponseEntity<>(makeMap("error", "player doesn't exists"), HttpStatus.UNAUTHORIZED);
        }
        Game game = new Game();
        gameRepository.save(game);
        GamePlayer gamePlayer = new GamePlayer(player, game);
        gamePlayerRepository.save(gamePlayer);
        return new ResponseEntity<>(makeMap("id", gamePlayer.getId()), HttpStatus.CREATED);



    }


    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    private Map<String, Object> gameDto(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getDate());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(gp -> gamePlayerDto(gp)).collect(toList()));

        return dto;
    }

    private Map<String, Object> gamePlayerDto(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", playerDto(gamePlayer.getPlayer()));
        if(gamePlayer.getScore() != null)
            dto.put("score", gamePlayer.getScore().getScore());
        else
            dto.put("score", null);
        return dto;

    }

    private Map<String, Object> playerDto(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("username", player.getUserName());
        return dto;
    }


    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity <Map<String, Object>>gameView(@PathVariable Long gamePlayerId, Authentication authentication) {

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "no access"), HttpStatus.UNAUTHORIZED);
        }
        Player player = playerRepository.findByUserName(authentication.getName());
        if (player == null) {
            return new ResponseEntity<>(makeMap("error", "player doesn't exists"), HttpStatus.UNAUTHORIZED);
        }
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
        if (!gamePlayer.isPresent()) {
            return new ResponseEntity<>(makeMap("error", "gamePlayer doesn't exists"), HttpStatus.UNAUTHORIZED);
        }

        if (player.getId() != gamePlayer.get().getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "Gp not the same"), HttpStatus.UNAUTHORIZED);
        }

        return  new ResponseEntity <> (game_viewDto(gamePlayer.get()),HttpStatus.ACCEPTED);

    }

    @PostMapping ("/game/{Id}/players")
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long Id, Authentication authentication) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "doesn't exist"), HttpStatus.UNAUTHORIZED);
        }
        Optional<Game> game = gameRepository.findById(Id);
        if (!game.isPresent()) {
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }
        if (game.get().getGamePlayers().size() >= 2) {
            return new ResponseEntity<>(makeMap("error", "game is full"), HttpStatus.FORBIDDEN);
        }
        Player player = playerRepository.findByUserName(authentication.getName());
        if (player == null) {
            return new ResponseEntity<>(makeMap("error", "player doesn't exists"), HttpStatus.UNAUTHORIZED);
        }
        if(game.get().getGamePlayers().stream().findFirst().get().getPlayer().getId()==player.getId()){//tener Id por medio del getplayer del game
            return new ResponseEntity<>(makeMap("error", "is already in the game"),HttpStatus.CONFLICT);
        }

        GamePlayer gamePlayer = new GamePlayer(player, game.get());
        gamePlayerRepository.save(gamePlayer);
        return new ResponseEntity<>(makeMap("id", gamePlayer.getId()), HttpStatus.CREATED);



    }

    @PostMapping ("/games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> placedShips(@PathVariable Long gamePlayerId, Authentication authentication,@RequestBody Set<Ship> listadoShips) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "there is no current user log in"), HttpStatus.UNAUTHORIZED);
        }

        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
        if (!gamePlayer.isPresent()) {
            return new ResponseEntity<>(makeMap("error", "No such game player"), HttpStatus.UNAUTHORIZED);
        }

        Player player = playerRepository.findByUserName(authentication.getName());
        if (player.getId() != gamePlayer.get().getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "Gp not the same Id"), HttpStatus.UNAUTHORIZED);
        }
        ;
        if (gamePlayer.get().getShips().size() > 0) {
            return new ResponseEntity<>(makeMap("error", "Gp is full"), HttpStatus.FORBIDDEN);
        }

        listadoShips.forEach(ship -> gamePlayer.get().addShip(ship));
        gamePlayerRepository.save(gamePlayer.get());
        return new ResponseEntity<>(makeMap("Great","Ships Added"),  HttpStatus.CREATED);

    };

    @PostMapping("/games/players/{gamePlayerId}/salvos")
    public ResponseEntity<Map<String, Object>> storeSalvos(@PathVariable Long gamePlayerId, Authentication authentication, @RequestBody Salvo salvos) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "there is no current user log in"), HttpStatus.UNAUTHORIZED);
        }
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
        if (!gamePlayer.isPresent()) {
            return new ResponseEntity<>(makeMap("error", "There is no game player with the given id"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.get().getState() != State.PLAY)
            return new ResponseEntity<>(makeMap("error", "You can't play"),HttpStatus.UNAUTHORIZED);

        int turn = gamePlayer.get().getSalvos().size() + 1;
        salvos.setTurn(turn);
        gamePlayer.get().addSalvo(salvos);
        gamePlayerRepository.save(gamePlayer.get());

        if (gamePlayer.get().getState() == State.WIN){
            Score score1 = new Score(gamePlayer.get().getGame(), gamePlayer.get().getPlayer(),1);
            Score score2 = new Score(gamePlayer.get().getGame(), gamePlayer.get().getOpponent().getPlayer(),0);
            scoreRepository.save(score1);
            scoreRepository.save(score2);
        }
        if (gamePlayer.get().getState() == State.TIE){
            Score score1 = new Score(gamePlayer.get().getGame(), gamePlayer.get().getPlayer(), 0.5);
            Score score2 = new Score(gamePlayer.get().getGame(), gamePlayer.get().getOpponent().getPlayer(), 0.5);
            scoreRepository.save(score1);
            scoreRepository.save(score2);
        }
        if (gamePlayer.get().getState() == State.LOST){
            Score score1 = new Score (gamePlayer.get().getGame(), gamePlayer.get().getPlayer(), 0);
            Score score2 = new Score (gamePlayer.get().getGame(), gamePlayer.get().getOpponent().getPlayer(), 1);
            scoreRepository.save(score1);
            scoreRepository.save(score2);

        }

        return new ResponseEntity<>(makeMap("Great","Salvos Added"),  HttpStatus.CREATED);
    }






    private Map<String, Object> game_viewDto(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getDate());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(gp -> gamePlayerDto(gp)).collect(toList()));
        dto.put("Ships", gamePlayer.getShips().stream().map(ship -> shipDto(ship)).collect(toList()));
        dto.put("Salvos", gamePlayer.getGame().getGamePlayers().stream().flatMap(gp -> gp.getSalvos().stream().map(salvo -> salvoDto(salvo))).collect(toList()));
        dto.put("Hits", gamePlayer.getHits());
        dto.put("hitsOpponent", gamePlayer.getOpponent() != null ? gamePlayer.getOpponent().getHits() : new ArrayList<>());
        dto.put("Sinks", gamePlayer.getSinks());
        dto.put("State", gamePlayer.getState());


        return dto;

    }

    private Map<String, Object> shipDto(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    private Map<String, Object> salvoDto(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("locations", salvo.getLocations());
        dto.put("player", playerDto(salvo.getGamePlayer().getPlayer()));
        return dto;

    }

    private Map<String, Object> scoreDto(Score score) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("score", score.getScore());
        return dto;
    }

}



