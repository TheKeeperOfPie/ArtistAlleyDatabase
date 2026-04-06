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
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.jsonIo
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

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
        client.get("channels/$channelId/threads/active")
            .body<ThreadsList>()

    suspend fun getChannel(channelId: String) =
        client.get("channels/$channelId").body<Channel>()

    suspend fun getChannelMessage(channelId: String, messageId: String) =
        client.get("channels/$channelId/messages/$messageId")
            .body<Message>()

    suspend fun createThread(
        channelId: String,
        title: String,
        firstMessage: String,
        secondMessage: String? = null,
    ) {
        val threadChannel = client.post("channels/$channelId/threads") {
            setBody(
                CreateThread(
                    name = title,
                    message = CreateMessage(firstMessage),
                )
            )
        }.assertSuccess().body<Channel>()
        createMessage(threadChannel.id, secondMessage?.ifBlank { null } ?: "Reserved")
    }

    suspend fun createMessage(channelId: String, message: String) {
        client.post("channels/$channelId/messages") {
            setBody(CreateMessage(message))
        }.assertSuccess()
    }

    suspend fun editMessage(channelId: String, messageId: String, message: String) {
        client.patch("channels/$channelId/messages/$messageId") {
            setBody(CreateMessage(message))
        }.assertSuccess()
    }

    suspend fun getOldestThreadMessage(channelId: String) =
        client.get("channels/$channelId/messages") {
            parameter("after", "0")
            parameter("limit", "2")
        }
            .body<List<Message>>()
            .first()

    suspend fun modifyThread(threadId: String, name: String? = null, archived: Boolean? = null) {
        client.patch("channels/$threadId") {
            setBody(ModifyThread(name = name, archived = archived))
        }.assertSuccess()
    }

    suspend fun deleteThread(threadId: String) {
        client.delete("channels/$threadId").assertSuccess()
    }

    private suspend fun HttpResponse.assertSuccess() = apply {
        if (!status.isSuccess()) {
            println("Failed request ${this.request.url}: $headers ${bodyAsText()}")
            val retryAfter = headers["Retry-After"]?.toIntOrNull()?.milliseconds
            if (retryAfter != null) {
                println("Delaying by $retryAfter, this will not retry the request")
                delay(retryAfter)
            }
        }
    }
}
