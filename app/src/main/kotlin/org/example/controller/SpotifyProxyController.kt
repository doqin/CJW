@file:JvmName("SpotifyProxyController")
package org.example.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
// import org.springframework.web.client.RestTemplate
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference

@RestController
@RequestMapping("/api")
class SpotifyProxyController {

    private val dotenv = dotenv{
        directory = System.getProperty("user.dir")
        // ignoreIfMissing = false
    }

    private val clientId: String = System.getenv("SPOTIFY_CLIENT_ID") ?: dotenv["SPOTIFY_CLIENT_ID"]
    private val clientSecret: String = System.getenv("SPOTIFY_CLIENT_SECRET") ?: dotenv["SPOTIFY_CLIENT_SECRET"]
    private val refreshToken: String = System.getenv("SPOTIFY_REFRESH_TOKEN") ?: dotenv["SPOTIFY_REFRESH_TOKEN"]
    private val accessToken = AtomicReference<String?>(null)

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    private fun refreshAccessToken(): String? = runBlocking {
        val response = httpClient.post("https://accounts.spotify.com/api/token") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                }.formUrlEncode()
            )
        }

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val token = json["access_token"]?.jsonPrimitive?.content
        accessToken.set(token)
//        println("Access token: $accessToken")
        token
    }

//    private val restTemplate = RestTemplate()

    @GetMapping("/currently-playing")
    fun getCurrentlyPlaying(): ResponseEntity<Map<String, String>> = runBlocking {
        val token = refreshAccessToken()
            ?: return@runBlocking ResponseEntity.status(401).body(mapOf("error" to "Unauthorized"))

        val response = httpClient.get("https://api.spotify.com/v1/me/player/currently-playing") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (response.status == HttpStatusCode.NoContent) {
            return@runBlocking ResponseEntity.ok(mapOf("track" to "Nothing playing", "artist" to "N/A"))
        }

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val item = json["item"]?.jsonObject
        val name = item?.get("name")?.jsonPrimitive?.contentOrNull ?: "Nothing playing"
        val artistArray = item?.get("artists")?.jsonArray ?: JsonArray(emptyList())
        val artist = artistArray.joinToString(", ") {
            it.jsonObject["name"]?.jsonPrimitive?.content ?: "Unknown Artist"
        }

        return@runBlocking ResponseEntity.ok(mapOf("track" to name, "artist" to artist))
    }
}