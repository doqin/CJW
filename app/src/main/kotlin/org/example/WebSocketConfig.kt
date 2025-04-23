package org.example

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.controller.MatchmakingHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
open class WebSocketConfig : WebSocketConfigurer {

    @Bean
    open fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(MatchmakingHandler(objectMapper()), "/ws/matchmaking")
            .setAllowedOrigins("*")
    }
}