package org.example

import org.springframework.web.socket.WebSocketSession
import java.util.UUID

data class RPSPlayer(
    val username: String,
    val wsSession: WebSocketSession,
    val sessionId: String = UUID.randomUUID().toString(),
    var move: String = "none",
    var point: Int = 0
)