@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionPatchResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionRequest
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionCallbackType
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionRequestData
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionType
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.MessageComponent
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URL
import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.Promise

@JsExport
class Worker {
    companion object {
        private val response401 = Response("Bad signature", ResponseInit(status = 401))
        private val response404 = Response("Failed to resolve request", ResponseInit(status = 404))
        private val response200 = Response(null, ResponseInit(status = 200))
        private val response202 = Response(null, ResponseInit(status = 202))

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
            val api = DiscordApi(env, json)
            val path = URL(request.url).pathname.removePrefix("/")
            if (path == "commands") {
                val result = api.syncCommands()
                return@promise jsonResponse("Updated commands $result")
            }
            if (path == "verify") {
                return@promise verifyArtist(request, api)
            } else if (path != "interactions") {
                return@promise response404
            }
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
                return@promise jsonResponse(
                    DiscordInteractionResponse(type = InteractionCallbackType.PONG)
                )
            } else if (interactionType == null) {
                return@promise response404
            }

            api.deferResponse(interaction)

            val response = when (val data = interaction.data) {
                is InteractionRequestData.SlashCommand -> when (data.name) {
                    "artist" -> DiscordInteractionPatchResponse(
                        content = "Loaded artist",
                        flags = MessageFlags(MessageFlag.EPHEMERAL),
                    )
                    "verify" -> DiscordInteractionPatchResponse(
                        content = """
                            ## Verify your Artist Profile 
                            Click below to check your Discord connections. We’ll use this to match a social connection (i.e. Bluesky) to your artist page.
                            ### Privacy
                            This should only ask for the "connections" permission, and hidden accounts will still work. No data will be stored. Immediately after verification, the bot will revoke this permission from itself.
                        """.trimIndent(),
                        flags = MessageFlags(MessageFlag.EPHEMERAL),
                        components = listOf(
                            MessageComponent.ActionRow(
                                MessageComponent.Button(
                                    style = MessageComponent.Button.Style.LINK,
                                    label = "Verify",
                                    // TODO: Add state
                                    url = env.DISCORD_BOT_VERIFY_URL,
                                )
                            )
                        )
                    )
                    else -> null
                }
            } ?: return@promise response404

            api.patchInteractionResponse(interaction, response)

            return@promise response202
        }

        private suspend fun verifyArtist(request: Request, api: DiscordApi): Response {
            val code = URL(request.url).searchParams.get("code")
            if (code.isNullOrBlank()) return response401
            val authResponse = api.getAuth(code)
            try {
                api.getConnections(authResponse.accessToken)
            } finally {
                try {
                    api.revokeToken(authResponse.accessToken, isRefreshToken = false)
                } finally {
                    api.revokeToken(authResponse.accessToken, isRefreshToken = true)
                }
            }

            return response200
        }
    }
}
