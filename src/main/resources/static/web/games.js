$(start);

var url = "/api/games"

function start() {
    $.get(url, recibirJson);
}

function recibirJson(json) {
    console.log(json);
    app.games = json.games;
    listaScores(json.games);
    app.player = json.player;


}



function listaScores(games) {
    var listaP = [];

    for (var i in games) {

        for (var j in games[i].gamePlayers) {
            //console.log(games[i].gamePlayers[j].player.username);
            //validar que el player no este ya agregado en listap

            if (listaP.includes(games[i].gamePlayers[j].player.username) == false)
                listaP.push(games[i].gamePlayers[j].player.username);
        };
    }

    for (var i in listaP) {
        //console.log(totalResults(listaP[i], games));
        app.Scores.push(totalResults(listaP[i], games));
    }
}


function totalResults(username, games) {

    var scores = {
        player: username,
        win: 0,
        tied: 0,
        lost: 0,
        total: 0,
    }

    for (var i in games) {

        for (var j in games[i].gamePlayers)

            if (username == games[i].gamePlayers[j].player.username) {

                var score = games[i].gamePlayers[j].score;
                if (score == 1) {
                    scores.win++;
                } else if (score == 0.5) {
                    scores.tied++;
                } else if (score == 0)
                    scores.lost++;

                scores.total += score;
            }
    }

    return scores;

}






var app = new Vue({
            el: '#app',
            data: {
                games: [],
                Scores: [],
                player: null,
                userEmail: '',
                userPassword: '',
               
            },

            filters: {
                dateFormat: function (value) {
                    if (!value) return '';
                    return moment(value).format('MMMM Do YYYY, h:mm:ss a');
                }
            },


            methods: {
                loginEmail: function () {
                    $.post("/api/login", {
                            username: app.userEmail,
                            password: app.userPassword
                        }, function () {
                            app.userEmail = '';
                            app.userPassword = '';
                            console.log("Welcome");
                            location.reload();
                        })
                        .fail(function () {
                            alert('Sorry, we couldn`t log you in');
                            location.reload();

                        });
                },
                singUp: function () {
                    $.post("/api/players", {
                            username: app.userEmail,
                            password: app.userPassword
                        }, function () {
                            app.loginEmail();
                            console.log("hola")
                        })
                        .fail(function () {
                            alert('Sorry, we couldn`t sing you up')
                        });
                },


                logOut: function () {
                    $.post("/api/logout", function () {
                            console.log("You`re out"),
                                location.reload();

                        })
                        .fail(function () {
                            alert('Sorry, we Couldn`t log you out')
                        })

                },

                identPlayer: function (game) {

                    var result = false;
                    if (app.player !== null) {
                        for (var j in game.gamePlayers) {
                            if (game.gamePlayers[j].player.username == app.player.username) {
                                result = true;
                            }
                        }
                    }
                    console.log("lalala" + result)
                    return result;
                },
                gameView: function (game) {
                    var gp = null;
                    if (app.player !== null) {
                        for (var j in game.gamePlayers) {
                            if (game.gamePlayers[j].player.username == app.player.username) {
                                gp = game.gamePlayers[j].id;
                            }
                        }
                    }
                    location.href = 'game.html?gp=' + gp
                },

                createGame: function () {

                    $.post("/api/games", null, function (respuesta) {
                            console.log(respuesta.id);
                            location.href = 'game.html?gp=' + respuesta.id
                        })
                        .fail(function () {
                            alert('Sorry, we couldn`t create a game')
                        });
                },
                joinGame: function (id) {
                    console.log(id);
                    $.post('/api/game/' + id + '/players', function (data) {
                            console.log(id);
                            location.href = '/web/game.html?gp=' + data.id;



                        })

                        .fail(function () {
                            alert('Sorry, the game is full')
                        });

                },
                findUser: function (gp) {
                    return gp.player.username == app.player.username;
                },

               
            }
            });
