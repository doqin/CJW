@file:JvmName("SpotifyActivity")
package org.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.concurrent.atomic.AtomicReference

val dotenv = dotenv{
    directory = System.getProperty("user.dir")
    // ignoreIfMissing = false
}

val clientId: String = System.getenv("SPOTIFY_CLIENT_ID") ?: dotenv["SPOTIFY_CLIENT_ID"]
val clientSecret: String = System.getenv("SPOTIFY_CLIENT_SECRET") ?: dotenv["SPOTIFY_CLIENT_SECRET"]
val redirectUri: String = System.getenv("SPOTIFY_REDIRECT_URI") ?: dotenv["SPOTIFY_REDIRECT_URI"]
const val scope = "user-read-currently-playing user-read-playback-state"

val accessToken = AtomicReference<String?>(null)

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun refreshAccessToken(): String? {
    val response = httpClient.post("https://accounts.spotify.com/api/token") {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
            Parameters.build {
                append("grant_type", "refresh_token")
                append("refresh_token", dotenv["SPOTIFY_REFRESH_TOKEN"]!!)
                append("client_id", clientId)
                append("client_secret", clientSecret)
            }.formUrlEncode()
        )
    }

    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
    val token = json["access_token"]?.jsonPrimitive?.content
    accessToken.set(token)
    println("Access token: $accessToken")
    return token
}

fun fetchDoqinSong() {
    embeddedServer(Netty, port = 42069) {
        routing {

            get("/login") {
                val url = URLBuilder("https://accounts.spotify.com/authorize").apply {
                    parameters.append("client_id", clientId)
                    parameters.append("response_type", "code")
                    parameters.append("redirect_uri", redirectUri)
                    parameters.append("scope", scope)
                }.buildString()
                call.respondRedirect(url)
            }

            get("/callback") {
                val code = call.parameters["code"] ?: return@get call.respondText(
                    "No code received",
                    status = HttpStatusCode.BadRequest
                )

                val tokenResponse = httpClient.post("https://accounts.spotify.com/api/token") {
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(
                        Parameters.build {
                            append("grant_type", "authorization_code")
                            append("code", code)
                            append("redirect_uri", redirectUri)
                            append("client_id", clientId)
                            append("client_secret", clientSecret)
                        }.formUrlEncode()
                    )
                }

                val tokenJson = Json.parseToJsonElement(tokenResponse.bodyAsText()).jsonObject
                val token = tokenJson["access_token"]?.jsonPrimitive?.content
                accessToken.set(token)

                call.respondText("Logged in successfully! Go back to the homepage.")
            }

            get("/api/currently-playing") {
                val token = refreshAccessToken()
                if (token == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
                    return@get
                }

                val playingResponse = httpClient.get("https://api.spotify.com/v1/me/player/currently-playing") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${accessToken.get()}")
                    }
                }

                if (playingResponse.status == HttpStatusCode.NoContent) {
                    call.respond(HttpStatusCode.NoContent)
                    return@get
                }

                val playingJson = Json.parseToJsonElement(playingResponse.bodyAsText()).jsonObject
                val name = playingJson["item"]?.jsonObject
                    ?.get("name")?.jsonPrimitive
                    ?.contentOrNull ?: "Nothing playing"
                val artist = playingJson["item"]?.jsonObject
                    ?.get("artists")?.jsonArray?.getOrNull(0)
                    ?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull ?: "Unknown Artist"

                call.respond(mapOf("track" to name, "artist" to artist))
            }
        }
    }.start(wait = true)
}

