const urlParams = new URLSearchParams(window.location.search);
const myParam = urlParams.get("gp");
var url = "/api/game_view/" + myParam;
console.log(myParam)


$.get(url, recibirDatos);

function recibirDatos(datosJs) {
    console.log(datosJs);

    grid(datosJs);

    //dibujarShips(datosJs);
    dibujarShipsGrid(datosJs);

    definirViewer(datosJs);

    dibujarSalvos(datosJs);

    app.datosJs = datosJs;

    if (datosJs.State == 'WAIT') {
        app.Start();
    }


}

function grid(datosJs) {
    var static = false;
    if (datosJs.Ships.length > 0)
        static = true;


    var options = {
        column: 10,
        //separacion entre elementos (les llaman widgets)
        verticalMargin: 0,
        //altura de las celdas
        cellHeight: 40,
        //desabilitando el resize de los widgets
        disableResize: true,
        //widgets flotantes
        float: true,
        //removeTimeout: 100,
        //permite que el widget ocupe mas de una columna
        disableOneColumnMode: true,
        //false permite mover, true impide
        staticGrid: static,
        //activa animaciones (cuando se suelta el elemento se ve más suave la caida)
        animate: true
    }

    $('.grid-stack').gridstack(options);
}

//agregando un elemento(widget) desde el javascript
function dibujarShipsGrid(datosJs) {

    var grid = $('.grid-stack').data('gridstack');

    if (datosJs.Ships.length <= 0) {
        grid.addWidget($('<div id="carrier"><div class="grid-stack-item-content carrierHorizontal"></div><div/>'),
            0, 0, 5, 1);

        grid.addWidget($('<div id="patrol"><div class="grid-stack-item-content patrolHorizontal"></div><div/>'),
            0, 1, 2, 1);

        grid.addWidget($('<div id="submarine"><div class="grid-stack-item-content submarineHorizontal"></div><div/>'),
            0, 2, 3, 1);

        grid.addWidget($('<div id="destroyer"><div class="grid-stack-item-content destroyerHorizontal"></div><div/>'),
            0, 3, 3, 1);

        grid.addWidget($('<div id="battleship"><div class="grid-stack-item-content battleshipHorizontal"></div><div/>'),
            0, 4, 4, 1);

        $("#carrier,#patrol,#submarine,#destroyer,#battleship").click(function () {
            var clase = $(this).prop('id') + "Horizontal";
            var width = $(this).data("gs-width");
            var height = $(this).data("gs-height");
            if ($(this).children().hasClass(clase)) {
                grid.resize($(this), height, width);
                $(this).children().removeClass(clase);
                $(this).children().addClass($(this).prop('id') + "Vertical");
            } else {
                grid.resize($(this), width, height);
                $(this).children().addClass(clase);
                $(this).children().removeClass($(this).prop('id') + "Vertical");

            }
        });
    } else {
        for (var i in datosJs.Ships) {
            console.log(datosJs.Ships[i]);

            var y = datosJs.Ships[i].locations[0].charAt(0);
            var y2 = datosJs.Ships[i].locations[1].charAt(0);
            var w = 0;
            var h = 0;
            var direccion = 'Vertical';

            if (y === y2) {
                direccion = 'Horizontal';
                w = datosJs.Ships[i].locations.length;
                h = 1;
            } else {
                w = 1;
                h = datosJs.Ships[i].locations[0].length;
            }

            var xFinal = parseInt(datosJs.Ships[i].locations[0].charAt(1)) - 1;
            var yFinal = 0;
            switch (y) {
                case "A":
                    yFinal = 0;
                    break;
                case "B":
                    yFinal = 1;
                    break;
                case "C":
                    yFinal = 2;
                    break;
                case "D":
                    yFinal = 3;
                    break;
                case "E":
                    yFinal = 4;
                    break;
                case "F":
                    yFinal = 5;
                    break;
                case "G":
                    yFinal = 6;
                    break;
                case "H":
                    yFinal = 7;
                    break;
                case "I":
                    yFinal = 8;
                    break;
                case "J":
                    yFinal = 9;
                    break;
            }

            grid.addWidget($('<div id="' + datosJs.Ships[i].type + '"><div class="grid-stack-item-content ' + datosJs.Ships[i].type + direccion + '"></div><div/>'),
                xFinal, yFinal, w, h);

        }
    }
}

function dibujarShips(datosJs) {
    for (var i in datosJs.Ships) {
        console.log(datosJs.Ships[i]);

        for (var j in datosJs.Ships[i].locations) {
            console.log(datosJs.Ships[i].locations[j]);
            document.getElementById(datosJs.Ships[i].locations[j]).innerHTML = ""

            var elm = document.getElementById(datosJs.Ships[i].locations[j]);
            elm.classList.add("celdaColors");
        }
    }
}

function definirViewer(datosJs) {
    for (var i in datosJs.gamePlayers) {
        console.log(datosJs.gamePlayers[i]);

        if (myParam == datosJs.gamePlayers[i].id) {

            app.viewer = datosJs.gamePlayers[i].player;
            console.log(app.viewer)
        } else(app.opponent = datosJs.gamePlayers[i].player)
        console.log(app.opponent);
    }
}


function dibujarSalvos(datosJs) {

    var hitsLocations_op = [];

    for (var i in datosJs.Hits) {
        for (var j in datosJs.Hits[i].locations)
            hitsLocations_op.push(datosJs.Hits[i].locations[j] + 's');
    }


    for (var i in datosJs.Salvos) {
        console.log(datosJs.Salvos[i]);

        if (app.viewer.id == datosJs.Salvos[i].player.id) {

            for (var j in datosJs.Salvos[i].locations) {
                var locationId = datosJs.Salvos[i].locations[j] + "s";

                var element = document.getElementById(locationId);

                element.innerHTML = datosJs.Salvos[i].turn;

                if (hitsLocations_op.includes(locationId)) {

                    element.classList.add("celdaRoja");

                } else {
                    element.classList.add("celdaSalvo");
                }


            }

        }


    }


}


console.log()








var app = new Vue({
    el: '#app',
    data: {
        num: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
        abc: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        salvo: [],
        viewer: "",
        opponent: "",
        ships: [],
        postSalvo: {
            turn: 1,
            locations: []
        },

        datosJs: {},


    },
    methods: {


        logOut: function () {
            $.post("/api/logout", function () {
                    console.log("You`re out");
                    location.href = ("/web/games.html")
                })
                .fail(function () {
                    alert('Sorry, we Couldn`t log you out');

                })

        },


        createShips: function () {
            var ships = document.getElementById('grid-stack').childNodes;

            for (var i = 0; i < ships.length; i++) {
                console.log(ships[i].dataset.gsY);

                var locations = [];

                var locationX = parseInt(ships[i].dataset.gsX);
                var locationY = parseInt(ships[i].dataset.gsY);
                var locationW = parseInt(ships[i].dataset.gsWidth);
                var locationH = parseInt(ships[i].dataset.gsHeight);


                if (locationW > locationH) {

                    var y = '';
                    var x = '';
                    switch (locationY) {
                        case 0:
                            y = "A"
                            break;
                        case 1:
                            y = "B"
                            break;
                        case 2:
                            y = "C"
                            break;
                        case 3:
                            y = "D"
                            break;
                        case 4:
                            y = "E"
                            break;
                        case 5:
                            y = "F"
                            break;
                        case 6:
                            y = "G"
                            break;
                        case 7:
                            y = "H"
                            break;
                        case 8:
                            y = "I"
                            break;
                        case 9:
                            y = "J"
                            break;
                    }

                    for (var j = 1; j <= locationW; j++) {
                        console.log("hola");
                        x = locationX + j;

                        locations.push(y + x);
                    }
                    console.log(locations);

                } else {

                    var x = locationX + 1;
                    var y = '';

                    for (var k = 0; k < locationH; k++) {

                        switch (locationY + k) {
                            case 0:
                                y = "A"
                                break;
                            case 1:
                                y = "B"
                                break;
                            case 2:
                                y = "C"
                                break;
                            case 3:
                                y = "D"
                                break;
                            case 4:
                                y = "E"
                                break;
                            case 5:
                                y = "F"
                                break;
                            case 6:
                                y = "G"
                                break;
                            case 7:
                                y = "H"
                                break;
                            case 8:
                                y = "I"
                                break;
                            case 9:
                                y = "J"
                        }

                        locations.push(y + x)
                    }
                    console.log(locations);
                }

                app.ships.push({
                    "type": ships[i].id,
                    "locations": locations,
                })

            }



            $.post({
                    url: '/api/games/players/' + myParam + '/ships',
                    data: JSON.stringify(app.ships),
                    success: function () {
                        console.log("Good");
                        location.reload();
                    },
                    dataType: "text",
                    contentType: "application/json"
                })

                .done(function () {
                    alert("Ships added");
                })
                .fail(function (data) {
                    alert("Failed");
                });


        },

        sendSalvos: function () {

            $.post({
                    url: '/api/games/players/' + myParam + '/salvos',
                    data: JSON.stringify(app.postSalvo),
                    success: function () {
                        console.log("Good");
                        location.reload();
                    },
                    dataType: "text",
                    contentType: "application/json"
                })

                .done(function () {
                    alert("Salvos added");
                })
                .fail(function (data) {
                    alert("Failed");
                });





        },

        createSalvo: function (id) {

            if (app.datosJs.State == 'PLAY') {

                var element = document.getElementById(id + 's');

                console.log(app.postSalvo.locations);


                if (element.classList.contains("celdaColors")) {

                    element.classList.remove("celdaColors")
                    app.postSalvo.locations.splice(app.postSalvo.locations.indexOf(id), 1);

                } else if (!app.postSalvo.locations.includes(id) && app.postSalvo.locations.length < 5 && !element.classList.contains("celdaSalvo") && !element.classList.contains("celdaRoja")) {

                    app.postSalvo.locations.push(id);

                    element.classList.add("celdaColors");



                } else {
                    alert("You can't select more than 5 grids or is selected");
                }


            }



        },




        Start: function () {
            var timerId;
            timerId = setInterval(function () {
                console.log("Loading!");
                location.reload();
            }, 10000);
        }

        



    },

});
