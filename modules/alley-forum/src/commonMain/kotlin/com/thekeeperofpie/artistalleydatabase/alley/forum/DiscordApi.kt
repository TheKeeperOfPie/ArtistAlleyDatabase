package com.thekeeperofpie.artistalleydatabase.alley.forum

import com.thekeeperofpie.artistalleydatabase.alley.forum.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.discord.Channel
import com.thekeeperofpie.artistalleydatabase.discord.CreateMessage
import com.thekeeperofpie.artistalleydatabase.discord.CreateThread
import com.thekeeperofpie.artistalleydatabase.discord.Message
import com.thekeeperofpie.artistalleydatabase.discord.ModifyThread
import com.thekeeperofpie.artistalleydatabase.discord.ThreadsList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.jsonIo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
internal object DiscordApi {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    private val client = HttpClient {
        defaultRequest {
            url("https://discord.com/api/v10")
            headers {
                this["Authorization"] = "Bot ${BuildKonfig.discordBotToken}"
                this["Content-Type"] = "application/json"
            }
        }
        install(ContentNegotiation) {
            jsonIo(json)
        }
    }

    suspend fun getThreads(channelId: String) =
        client.get("guilds/${BuildKonfig.discordGuildId}/threads/active")
            .body<ThreadsList>()

    suspend fun getChannel(channelId: String) =
        client.get("channels/$channelId").body<Channel>()

    suspend fun getChannelMessage(channelId: String, messageId: String) =
        client.get("channels/$channelId/messages/$messageId")
            .body<Message>()

    suspend fun createThread(
        channelId: String,
        title: String,
        message: String,
    ) {
        client.post("channels/$channelId/threads") {
            setBody(
                CreateThread(
                    name = title,
                    message = CreateMessage(message),
                )
            )
        }
    }

    suspend fun editMessage(channelId: String, messageId: String, message: String) {
        client.patch("channels/$channelId/messages/$messageId") {
            setBody(CreateMessage(message))
        }.assertSuccess()
    }

    suspend fun modifyThread(threadId: String, name: String? = null, archived: Boolean? = null) {
        client.patch("channels/$threadId") {
            setBody(ModifyThread(name = name, archived = archived))
        }
    }

    private suspend fun HttpResponse.assertSuccess() {
        if (status != HttpStatusCode.OK) {
            println("Failed request ${this.request.url}: ${bodyAsText()}")
        }
    }
}
