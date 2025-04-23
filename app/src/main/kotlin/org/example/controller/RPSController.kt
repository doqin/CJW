@file:JvmName("RPSController")

package org.example.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.RPSSession
import org.example.RPSPlayer
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import org.slf4j.LoggerFactory

enum class MatchResult {
    Draw,
    Win,
    Defeat
}

data class ClientMessage(
    val type: String = "",  // Add default value
    val username: String? = null,  // Already nullable, add default value
    val sessionId: String? = null,
    val move: String? = null,
)

data class MatchFoundMessage(
    val type: String = "match_found",
    val opponent: String,
    val sessionId: String
)

data class MatchResultMessage(
    val type: String = "match_result",
    val opponentMove: String,
    val result: String,
    val playerPoint: Int,
    val opponentPoint: Int,
)

data class InvalidSession(
    val type: String = "invalid_session",
    val content: String,
)

@Component
class MatchmakingHandler( private var objectMapper: ObjectMapper ) : TextWebSocketHandler() {
    private val waitingPlayers: Queue<RPSPlayer> = ConcurrentLinkedQueue()
    private val activeGames: MutableMap<String, RPSSession> = ConcurrentHashMap()

    private val logger = LoggerFactory.getLogger(MatchmakingHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Client connected: ${session.id}")
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        val msg = objectMapper.readValue(message.payload, ClientMessage::class.java)

        if (msg.type == "join_queue" && msg.username != null) {
            val player = RPSPlayer(msg.username, wsSession = session)
            waitingPlayers.add(player)
            tryMatchPlayers()
        }

        if (msg.type == "player_move" && msg.username != null && msg.sessionId != null && msg.move != null) {
            logger.info("${msg.username} played ${msg.move}!")
            val gameSession = activeGames[msg.sessionId]
            if (gameSession == null) {
                val response = objectMapper.writeValueAsString(InvalidSession(content = "Couldn't find your active game!"))
                session.sendMessage(TextMessage(response))
                return
            }
            var player: RPSPlayer
            var opponent: RPSPlayer
            if (gameSession.player1.sessionId == msg.sessionId) {
                player = gameSession.player1
                opponent = gameSession.player2
            } else if (gameSession.player2.sessionId == msg.sessionId) {
                player = gameSession.player2
                opponent = gameSession.player1
            } else {
                val response = objectMapper.writeValueAsString(InvalidSession(content = "Your session id was not found in your supposed game!"))
                session.sendMessage(TextMessage(response))
                return
            }
            player.move = msg.move
            evalMoves(player, opponent)
        }
    }

    private fun evalMoves(p1: RPSPlayer, p2: RPSPlayer) {
        if (p1.move == "none" || p2.move == "none") {
            return
        }

        if (p1.move == p2.move) {
            notifyResult(p1, p2, MatchResult.Draw)
            notifyResult(p2, p1, MatchResult.Draw)
        } else if ((p1.move == "rock" && p2.move == "scissors")
            || (p1.move == "paper" && p2.move == "rock")
            || (p1.move == "scissors" && p2.move == "paper")) {
            notifyResult(p1, p2, MatchResult.Win)
            notifyResult(p2, p1, MatchResult.Defeat)
        } else if ((p2.move == "rock" && p1.move == "scissors")
            || (p2.move == "paper" && p1.move == "rock")
            || (p2.move == "scissors" && p1.move == "paper")) {
            notifyResult(p2, p1, MatchResult.Win)
            notifyResult(p1, p2, MatchResult.Defeat)
        } else {
            return
        }
        p1.move = "none"
        p2.move = "none"
    }

    private fun notifyResult(player: RPSPlayer, opponent: RPSPlayer, result: MatchResult) {
        var resultText: String
        when(result) {
            MatchResult.Draw -> resultText = "Draw"
            MatchResult.Win -> { resultText = "Win"; player.point++; }
            MatchResult.Defeat -> resultText = "Defeat"
        }
        val response = MatchResultMessage(
            opponentMove = opponent.move,
            result = resultText,
            playerPoint = player.point,
            opponentPoint = opponent.point,
        )
        val text = objectMapper.writeValueAsString(response)
        player.wsSession.sendMessage(TextMessage(text))
    }

    private fun tryMatchPlayers() {
        if (waitingPlayers.size >= 2) {
            val p1 = waitingPlayers.poll()
            val p2 = waitingPlayers.poll()

            val session = RPSSession(p1, p2)
            activeGames[p1.sessionId] = session
            activeGames[p2.sessionId] = session

            notifyMatch(p1, p2)
            notifyMatch(p2, p1)
        }
    }

    private fun notifyMatch(player: RPSPlayer, opponent: RPSPlayer) {
        val response = MatchFoundMessage(
            opponent = opponent.username,
            sessionId = player.sessionId
        )
        val text = objectMapper.writeValueAsString(response)
        player.wsSession.sendMessage(TextMessage(text))
    }
}