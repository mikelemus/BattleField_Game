$(start);

var url = "http://localhost:8080/api/games"

function start(){
    fetch (url)
        .then(recibirResponse)
        .then(recibirJson);
}

function recibirResponse(response) {
    return response.json();
    
}
function recibirJson(myJson){
    console.log(myJson);
}
