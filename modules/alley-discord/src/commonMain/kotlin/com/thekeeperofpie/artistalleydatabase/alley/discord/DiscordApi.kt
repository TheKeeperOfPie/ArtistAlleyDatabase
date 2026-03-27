package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.alley.discord.models.AuthTokenResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.Command
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.CommandRegisterRequest
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.Connection
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionPatchResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionRequest
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionCallbackType
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionResponseData
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.OptionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.FOLLOW
import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestRedirect

private const val BASE_URL = "https://discord.com/api/v10"

internal class DiscordApi(
    private val env: Env,
    private val json: Json,
) {
    suspend fun syncCommands(): String {
        val listCommandsResult = fetch(
            Request(
                "$BASE_URL/applications/${env.DISCORD_BOT_APP_ID}/commands",
                RequestInit(
                    method = "GET",
                    headers = Headers().apply {
                        this.set("Authorization", "Bot ${env.DISCORD_BOT_TOKEN}")
                    },
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await()
        val listCommandsBody = listCommandsResult.text().await()
        if (!listCommandsResult.ok) {
            println("Failed to list commands: $listCommandsBody")
        }
        json.decodeFromString<List<Command>>(listCommandsBody)
            .forEach { deleteCommand(it) }

        return registerCommand(
            CommandRegisterRequest(
                name = "aa",
                type = CommandRegisterRequest.CommandType.CHAT_INPUT,
                description = "Interact with the artistalley.directory site",
                options = listOf(
                    CommandRegisterRequest.Option(
                        name = "artist",
                        type = OptionType.SUB_COMMAND,
                        description = "Look up an artist",
                        options = listOf(
                            CommandRegisterRequest.Option(
                                name = "booth",
                                type = OptionType.STRING,
                                description = "Table number (e.g. M39)",
                            )
                        ),
                    ),
                    CommandRegisterRequest.Option(
                        name = "verify",
                        type = OptionType.SUB_COMMAND,
                        description = "Verify as an artist tabling",
                        options = listOf(
                            CommandRegisterRequest.Option(
                                name = "convention",
                                type = OptionType.STRING,
                                description = "Convention and year",
                                required = true,
                                choices = listOf(
                                    CommandRegisterRequest.Option.Choice(
                                        name = "AX 2026",
                                        value = DataYear.ANIME_EXPO_2026.serializedName,
                                    ),
                                ),
                            ),
                            CommandRegisterRequest.Option(
                                name = "booth",
                                type = OptionType.STRING,
                                description = "Table number",
                                required = true,
                            )
                        ),
                    )
                ),
            )
        )
    }

    private suspend fun deleteCommand(command: Command) {
        fetch(
            Request(
                "$BASE_URL/applications/${env.DISCORD_BOT_APP_ID}/commands/${command.id}",
                RequestInit(
                    method = "DELETE",
                    headers = Headers().apply {
                        this.set("Authorization", "Bot ${env.DISCORD_BOT_TOKEN}")
                    },
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await()
    }

    private suspend fun registerCommand(command: CommandRegisterRequest) = fetch(
        Request(
            "$BASE_URL/applications/${env.DISCORD_BOT_APP_ID}/commands",
            RequestInit(
                method = "POST",
                headers = headers(),
                body = json.encodeToString(command),
                cache = undefined,
                integrity = undefined,
                redirect = RequestRedirect.FOLLOW,
            )
        )
    ).await().text().await()

    suspend fun deferResponse(interaction: DiscordInteractionRequest) {
        val body = json.encodeToString(
            DiscordInteractionResponse(
                type = InteractionCallbackType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE,
                data = InteractionResponseData(
                    flags = MessageFlags(MessageFlag.EPHEMERAL),
                ),
            )
        )
        val result = fetch(
            Request(
                "$BASE_URL/interactions/${interaction.id}/${interaction.token}/callback",
                RequestInit(
                    method = "POST",
                    headers = headers(),
                    body = body,
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await()
        if (!result.ok) {
            println("Failed to defer response with $body, received ${result.text().await()}")
        }
    }

    suspend fun patchInteractionResponse(
        interactionToken: String,
        response: DiscordInteractionPatchResponse,
    ) {
        val body = json.encodeToString(response)
        val result = fetch(
            Request(
                "$BASE_URL/webhooks/${env.DISCORD_BOT_APP_ID}/$interactionToken/messages/@original",
                RequestInit(
                    method = "PATCH",
                    headers = headers(),
                    body = body,
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await()
        if (!result.ok) {
            println("Failed to patch response with $body, received ${result.text().await()}")
        }
    }

    suspend fun getAuth(authCode: String): AuthTokenResponse {
        val result = fetch(
            Request(
                "https://discord.com/api/oauth2/token",
                RequestInit(
                    method = "POST",
                    headers = Headers().apply {
                        this.set("Authorization", "Bot ${env.DISCORD_BOT_TOKEN}")
                        this.set("Content-Type", "application/x-www-form-urlencoded")
                    },
                    body = URLSearchParams().apply {
                        set("client_id", env.DISCORD_BOT_APP_ID)
                        set("client_secret", env.DISCORD_BOT_CLIENT_SECRET)
                        set("grant_type", "authorization_code")
                        set("code", authCode)
                        set("redirect_uri", env.DISCORD_BOT_REDIRECT_URL)
                    },
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await().text().await()
        return json.decodeFromString<AuthTokenResponse>(result)
    }

    suspend fun revokeToken(token: String, isRefreshToken: Boolean) {
        val result = fetch(
            Request(
                "https://discord.com/api/oauth2/token/revoke",
                RequestInit(
                    method = "POST",
                    headers = Headers().apply {
                        this.set("Authorization", "Bot ${env.DISCORD_BOT_TOKEN}")
                        this.set("Content-Type", "application/x-www-form-urlencoded")
                    },
                    body = URLSearchParams().apply {
                        set("client_id", env.DISCORD_BOT_APP_ID)
                        set("client_secret", env.DISCORD_BOT_CLIENT_SECRET)
                        set("token", token)
                        set(
                            "token_type_hint",
                            if (isRefreshToken) "refresh_token" else "access_token"
                        )
                    },
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await()
        println("Revoked ${if (isRefreshToken) "refresh_token" else "access_token"}")
        if (!result.ok) {
            println("Failed to revoke token")
        }
    }

    suspend fun getConnections(accessToken: String): List<Connection> {
        val result = fetch(
            Request(
                "$BASE_URL/users/@me/connections",
                RequestInit(
                    method = "GET",
                    headers = Headers().apply {
                        this.set("Authorization", "Bearer $accessToken")
                        this.set("Content-Type", "application/json")
                    },
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await()
        val body = result.text().await()
        val connections = json.decodeFromString<List<Connection>>(body)
        if (!result.ok) {
            println("Failed to get connections, received $body")
        }
        return connections
    }

    private fun headers() = Headers().apply {
        this.set("Authorization", "Bot ${env.DISCORD_BOT_TOKEN}")
        this.set("Content-Type", "application/json")
    }
}
