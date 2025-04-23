let socket;
let sessionId;
let username;
let opponent;

document.getElementById("startMatchmaking").addEventListener("click", () => {
    username = document.getElementById("usernameInput").value.trim();
    if (!username) {
        alert("Please enter a username!");
        return;
    }

    document.getElementById("startMatchmaking").hidden = true;
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    const host = window.location.host;
    socket = new WebSocket(`${protocol}://${host}/ws/matchmaking`);
    document.getElementById("status").textContent = "Matchmaking...";

    socket.onopen = () => {
        socket.send(JSON.stringify({
            type: "join_queue",
            username: `${username}`
        }));
        console.log("Finding players...");
    };

    socket.onmessage = (event) => {
        const msg = JSON.parse(event.data);
        if (msg.type === "match_found") {
            document.getElementById("status").textContent = "";
            console.log("Matched with:", msg.opponent);
            alert(`You matched with ${msg.opponent}`);
            opponent = msg.opponent;
            sessionId = msg.sessionId;
            const movesMenu = document.getElementById("moves");
            const rock = document.createElement("button");
            rock.className = "move";
            rock.textContent = "rock";
            const paper = document.createElement("button");
            paper.className = "move";
            paper.textContent = "paper";
            const scissors = document.createElement("button");
            scissors.className = "move";
            scissors.textContent = "scissors";
            movesMenu.append(rock);
            movesMenu.append(" ");
            movesMenu.append(paper);
            movesMenu.append(" ");
            movesMenu.append(scissors);
            document.getElementById("score").textContent = `0 | 0`
            const moves = document.getElementsByClassName("move");
            for (let move of moves) {
                move.addEventListener("click", () => {
                    socket.send(JSON.stringify({
                        type: "player_move",
                        username: username,
                        sessionId: sessionId,
                        move: move.textContent
                    }));
                    for (let move1 of moves) {
                        move1.hidden = true;
                    }
                    document.getElementById("status").textContent = `Waiting for ${opponent}'s move...`;
                });
            }
        }

        if (msg.type === "match_result") {
            console.log("Match result:", msg.result);
            document.getElementById("status")
                .textContent = `${opponent} played ${msg.opponentMove}: ${msg.result}`;
            const moves = document.getElementsByClassName("move");
            for (let move of moves) {
                move.hidden = false;
            }
            document.getElementById("score").textContent = `${msg.playerPoint} | ${msg.opponentPoint}`;
        }
    };

    socket.onerror = (err) => {
        console.error("WebSocket error:", err);
        document.getElementById("status").textContent = "An error has occurred! Check the console for more details.";
    };
});
