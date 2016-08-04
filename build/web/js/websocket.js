var wsUri = "ws://" + document.location.host + document.location.pathname + "actions";
var socket = new WebSocket(wsUri);
socket.onerror = onError;
socket.onmessage = onMessage;
var output = document.getElementById("output");

$(document).ready(function () {
    $('.game').hide();
    $('#scoreContainer').hide();
    $('#countdown_box').hide();
    $('.login').show();
});

function setButtonId(button){
    var btnId = $(button).data('id');
    $(".modal-body #flag").val(btnId);
}

function startGame(rows, columns) {
    $('.login').hide();
    var action = document.getElementById("flag").value;
    if (action === "start") {
        $('#gameInfoPopup #msg').html("Please wait... Game will begin once another player joins !!");
        $('#gameInfoPopup').modal('show');
    }
    sendPlayerAndGameInfo(action, rows, columns);
}

function sendPlayerAndGameInfo(action, rows, columns) {
    var name = document.getElementById("userName").value;
    var color = getRandomColor();
    // set user assigned color in css variable
    document.querySelector("html").style.setProperty("--mycolor", color);
    var totalBlocks = rows * columns;
    var gameAction = {
        action: action,
        name: name,
        color: color,
        totalBlocks: totalBlocks
    };
    socket.send(JSON.stringify(gameAction));
}

function getRandomColor() {
    var letters = '0123456789ABCDEF';
    var color = '#';
    for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

function onMessage(event) {
    var data = JSON.parse(event.data);

    if (data.action === "scoreBoard") {
        addScoreRow(data.playerId, data.playerName, data.score, data.color);
    }
    
    if (data.action === "noGame") {
        $('#gameInfoPopup #msg').html(data.msg);
        $('#gameInfoPopup').modal('show');
        $('.login').show();
    }

    if (data.action === "gameState") {
        for (var i = 0; i < data.coloredBlockArray.length; i++) {
            addColorToBlock(data.coloredBlockArray[i]);
        }
        for (var i = 0; i < data.scoreArray.length; i++) {
            addScoreRow(data.scoreArray[i].playerId, data.scoreArray[i].playerName, data.scoreArray[i].score, data.scoreArray[i].color);
        }
    }

    if (data.action === "removeModal") {
        $('#gameInfoPopup').modal('hide');
        $('.game').show();
        $('#scoreContainer').show();
    }
    
    if (data.action === "showGame") {
        $('.game').show();
        $('#scoreContainer').show();
    }
    
    if (data.action === "blockUser") {
        lockScreen(data.duration);
    }
    
    if (data.action === "click") {
        addColorToBlock(data);
        updateScoreRow(data.playerId, data.score);
    }
    
    if (data.action === "updateScore") {
        updateScoreRow(data.playerId, data.score);
    }

    if (data.action === "gameOver") {
        declareWinner(data);
    }
}

function addScoreRow(playerId, playerName, score, color) {
    var table = document.getElementById("scoreboard").getElementsByTagName('tbody')[0];

    var rowCount = table.rows.length;
    var row = table.insertRow(rowCount);
    rowCount = rowCount + 1;

    row.insertCell(0).innerHTML = rowCount;
    row.insertCell(1).innerHTML = playerName;
    row.insertCell(2).innerHTML = '';
    row.insertCell(3).innerHTML = score;
    row.id = "player" + playerId;
    $("#scoreboard tr:nth-child(" + rowCount + ") td:nth-child(3)").css("background-color", color);
}

function addColorToAllConnectedSession(color, blockId) {
    var clickAction = {
        action: "click",
        color: color,
        blockId: blockId
    };
    socket.send(JSON.stringify(clickAction));
}

function lockScreen(duration) {
    //show content
    $('#countdown_box').show(); //countdown
    $('.game').hide();
    $('#scoreContainer').hide();
    //lock interface
    $.uiLock('');
    //start the countdown (unlocks interface at end)
    doCountdown(duration);
}

//function to show the countdown in seconds until the web page is unfrozen (active) again
function doCountdown(duration) {
    startNum = duration;
    var countdownOutput = document.getElementById('countdown_div');
    if (startNum > 0) {
        countdownOutput.innerHTML = formatAsTime(startNum);
        setTimeout("updateClock(\"countdown_div\", " + startNum + ")", 1000);
    }
    return false;
}

//helper function to update the timer on the web page this is frozen
function updateClock(countdown_div, new_value) {
    var countdownOutput = document.getElementById(countdown_div);
    new_value = new_value - 1;
    if (new_value > 0) {
        new_formatted_value = formatAsTime(new_value);
        countdownOutput.innerHTML = new_formatted_value;
        setTimeout("updateClock(\"countdown_div\", " + new_value + ")", 1000);
    } else {
        //finish!
        countdownOutput.innerHTML = "";
        $('#countdown_box').hide();
        //unlock UI
        $.uiUnlock();
        $('.game').show();
        $('#scoreContainer').show();
    }
}

//helper function to calculate the time (seconds) remaining as minutes and seconds
function formatAsTime(seconds) {
    var minutes = parseInt(seconds / 60);
    seconds = seconds - (minutes * 60);
    if (minutes < 10) {
        minutes = "0" + minutes;
    }
    if (seconds < 10) {
        seconds = "0" + seconds;
    }
    var return_var = minutes + ':' + seconds;
    return return_var;
}

function colorBlock(block) {
    if ($(block).hasClass("hoverEffect")) {
        var htmlStyles = window.getComputedStyle(document
                .querySelector("html"));
        var color = htmlStyles.getPropertyValue("--mycolor");

        $(block).removeClass("hoverEffect");
        $(block).css('background-color', color);
        $(block).css('color', '#000000');
        addColorToAllConnectedSession(color, block.id);
    }
}

function addColorToBlock(data) {
    $('#' + data.blockId).removeClass("hoverEffect");
    $('#' + data.blockId).css('background-color', data.color);
    $('#' + data.blockId).css('color', '#000000');
}


function updateScoreRow(playerId, score) {
    $("#player" + playerId + " td:nth-child(4)").html(score);
}

function declareWinner(data) {
    $('#resultPopup #msg').html(data.msg);
    $('#resultPopup').modal('show');
    $('.login').show();
    $(".game").load(location.href + " .game>*", "");
    $('.game').hide();
    $('#scoreContainer').hide();
}

function onError(event) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + event.data);
}

function writeToScreen(message) {
    output.innerHTML += message + "<br>";
}