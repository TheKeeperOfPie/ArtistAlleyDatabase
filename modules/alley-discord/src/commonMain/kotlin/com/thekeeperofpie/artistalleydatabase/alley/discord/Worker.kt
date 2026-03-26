@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionPatchResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionRequest
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionCallbackType
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionRequestData
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionType
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.MessageComponent
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import kotlinx.coroutines.await
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString
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
        private val response401 get() = Response("Bad signature", ResponseInit(status = 401))
        private val response404
            get() = Response("Failed to resolve request", ResponseInit(status = 404))
        private val response200 get() = Response(null, ResponseInit(status = 200))
        private val response202 get() = Response(null, ResponseInit(status = 202))
        private val responseReturnToDiscord
            get() = Response(
                """
                <!DOCTYPE html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8">
                    <meta name="color-scheme" content="dark light" />
                    <title>Artist Alley Directory Verification</title>
                  </head>
                  <body>
                    <h1>Please return to Discord to see the verification result</h1>
                  </body>
                </html>
            """.trimIndent(),
                ResponseInit(
                    status = 202,
                    headers = Headers().apply {
                        set("Content-Type", "text/html")
                    },
                )
            )

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
                return@promise verifyArtist(request, env, api)
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
                    "verify" -> {
                        val options = data.options
                        if (options.isNullOrEmpty()) return@promise response404
                        env.ARTIST_ALLEY_BOT_KV.put(interaction.member.user.id, interaction.token)
                            .await()
                        DiscordInteractionPatchResponse(
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
                                        url = buildOAuthUrl(env, interaction, options),
                                    )
                                )
                            )
                        )
                    }
                    else -> null
                }
            } ?: return@promise response404

            api.patchInteractionResponse(interaction.token, response)

            return@promise response202
        }

        private suspend fun verifyArtist(request: Request, env: Env, api: DiscordApi): Response {
            val params = URL(request.url).searchParams
            val code = params.get("code")
            val state = params.get("state")
            if (code.isNullOrBlank() || state.isNullOrBlank()) return response401

            val oAuthState = OAuthState.decode(symmetricDecrypt(env.ENCRYPTION_KEY, state))
                ?: return response401

            val interactionToken = env.ARTIST_ALLEY_BOT_KV.get(oAuthState.userId).await()
            env.ARTIST_ALLEY_BOT_KV.delete(oAuthState.userId).await()
            if (interactionToken.isNullOrBlank()) return response401

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

            // TODO: Actually verify connection
            if (oAuthState.booth.startsWith("C")) {
                api.patchInteractionResponse(
                    interactionToken = interactionToken,
                    response = DiscordInteractionPatchResponse(
                        content = """
                            ## Verified!
                            Your URL is https://form.artistalley.directory/form
                            
                            Be sure to bookmark this so that you can return to edit your data in the future.
                            ### Warning
                            Do not share this link with anyone or they'll be able to edit your data. If you ever lose it, use the bot to re-verify.
                        """.trimIndent(),
                        flags = MessageFlags(MessageFlag.EPHEMERAL),
                        components = emptyList(),
                    )
                )
            } else {
                // TODO: Use working booth link?
                api.patchInteractionResponse(
                    interactionToken = interactionToken,
                    response = DiscordInteractionPatchResponse(
                        content = """
                            ## Verification failed
                            Make sure that your Discord account has one of the social media accounts listed under your table at https://artistalley.directory and try again
                        """.trimIndent(),
                        flags = MessageFlags(MessageFlag.EPHEMERAL),
                    )
                )
            }

            return responseReturnToDiscord
        }

        private suspend fun buildOAuthUrl(
            env: Env,
            interaction: DiscordInteractionRequest,
            options: List<InteractionRequestData.SlashCommand.Option>,
        ): String {
            val encryptedState = symmetricEncrypt(
                key = env.ENCRYPTION_KEY,
                payload = OAuthState(
                    userId = interaction.member.user.id,
                    dataYear = DataYear.deserialize(options.first { it.name == "convention" }.value)!!,
                    booth = options.first { it.name == "booth" }.value,
                ).encode(),
            )
            return "${env.DISCORD_BOT_VERIFY_URL}&state=$encryptedState"
        }

        private suspend fun symmetricEncrypt(key: String, payload: String): String =
            CryptographyProvider.Default.get(AES.GCM).keyDecoder()
                .decodeFromByteString(AES.Key.Format.RAW, key.hexToByteString())
                .cipher()
                .encrypt(payload.encodeToByteString())
                .toHexString()

        private suspend fun symmetricDecrypt(key: String, payload: String): String =
            CryptographyProvider.Default.get(AES.GCM)
                .keyDecoder()
                .decodeFromByteString(AES.Key.Format.RAW, key.hexToByteString())
                .cipher()
                .decrypt(payload.hexToByteString())
                .decodeToString()
    }
}
