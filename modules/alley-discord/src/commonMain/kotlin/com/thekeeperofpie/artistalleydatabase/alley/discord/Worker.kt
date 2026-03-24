@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.alley.discord.models.CommandRegisterRequest
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionPatchResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionRequest
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionCallbackType
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionType
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URL
import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.Promise
import kotlin.time.Duration.Companion.seconds

@JsExport
class Worker {
    companion object {
        private val response401 = Response("Bad signature", ResponseInit(status = 401))
        private val response404 = Response("Failed to resolve request", ResponseInit(status = 404))
        private val response202 = Response(null, ResponseInit(status = 202))

        private const val BASE_URL = "https://discord.com/api/v10"

        private val json = Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
            isLenient = true
        }

        private inline fun <reified T> jsonResponse(value: T) = Response(
            body = Json.encodeToString(value),
            init = ResponseInit(status = 200, headers = Headers().apply {
                set("Content-Type", "application/json")
            })
        )

        @JsStatic
        fun request(request: Request, env: Env): Promise<Response> = promise {
            val path = URL(request.url).pathname.removePrefix("/")
            if (path == "commands") {
                val result = fetch(
                    Request(
                        "$BASE_URL/applications/${env.DISCORD_BOT_APP_ID}/commands",
                        RequestInit(
                            method = "POST",
                            headers = Headers().apply {
                                this.set("Authorization", "Bot ${env.DISCORD_BOT_TOKEN}")
                                this.set("Content-Type", "application/json")
                            },
                            body = Json.encodeToString(
                                CommandRegisterRequest(
                                    name = "artist",
                                    type = CommandRegisterRequest.CommandType.CHAT_INPUT,
                                    description = "Look up an artist",
                                    options = listOf(
                                        CommandRegisterRequest.Option(
                                            name = "booth",
                                            type = CommandRegisterRequest.Option.OptionType.STRING,
                                            description = "Table number",
                                        )
                                    ),
                                )
                            ),
                        )
                    )
                ).await().text().await()
                return@promise jsonResponse("Updated commands $result")
            }
            if (path != "interactions") response404
            val method = request.method
            if (method != "POST") {
                return@promise Response("Invalid method $method", ResponseInit(status = 405))
            }
            val signature = request.headers.get("X-Signature-Ed25519")
            val timestamp = request.headers.get("X-Signature-Timestamp")
            if (signature == null || timestamp == null) {
                return@promise response401
            }
            // TODO: Verify timestamp is within acceptable window
            val body = request.text().await()
            val verified = verifyKey(body, signature, timestamp, env.DISCORD_BOT_PUBLIC_KEY).await()
            if (!verified) {
                return@promise response401
            }

            val interaction = json.decodeFromString<DiscordInteractionRequest>(body)
            val interactionType = interaction.type
            if (interactionType == InteractionType.PING) {
                return@promise jsonResponse(DiscordInteractionResponse(type = InteractionCallbackType.PONG))
            } else if (interactionType == null) {
                return@promise response404
            }

            fetch(
                Request(
                    "$BASE_URL/interactions/${interaction.id}/${interaction.token}/callback",
                    RequestInit(
                        method = "POST",
                        headers = Headers().apply {
                            this.set("Authorization", "Bot ${env.DISCORD_BOT_TOKEN}")
                            this.set("Content-Type", "application/json")
                        },
                        body = json.encodeToString(DiscordInteractionResponse(type = InteractionCallbackType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE))
                    )
                )
            ).await().text().await()

            delay(1.seconds)

            fetch(
                Request(
                    "$BASE_URL/webhooks/${env.DISCORD_BOT_APP_ID}/${interaction.token}/messages/@original",
                    RequestInit(
                        method = "PATCH",
                        headers = Headers().apply {
                            this.set("Authorization", "Bot ${env.DISCORD_BOT_TOKEN}")
                            this.set("Content-Type", "application/json")
                        },
                        body = json.encodeToString(
                            DiscordInteractionPatchResponse(content = "Loaded artist")
                        )
                    )
                )
            ).await().text().await()

            return@promise response202
        }
    }
}
