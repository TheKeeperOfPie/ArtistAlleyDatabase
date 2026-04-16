package com.thekeeperofpie.artistalleydatabase.alley.forum

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
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.takeFrom
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.jsonIo
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val LOG = false

@OptIn(ExperimentalSerializationApi::class)
internal class DiscordApi(private val environment: Environment) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    private val client = HttpClient {
        defaultRequest {
            url("https://discord.com/api/v10")
            headers {
                this["Authorization"] = "Bot ${environment.botToken}"
                this["Content-Type"] = "application/json"
            }
        }
        install(ContentNegotiation) {
            jsonIo(json)
        }
        if (LOG) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) = println(message)
                }
                level = LogLevel.ALL
                sanitizeHeader { it == "Authorization" }
            }
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
        firstMessage: CreateMessage,
        secondMessage: CreateMessage? = null,
        imageAttachments: List<Pair<String, ByteArray>> = emptyList(),
    ) {
        val threadChannel = client.post("channels/$channelId/threads") {
            headers {
                this["Content-Type"] = "multipart/form-data"
            }
            setBody(MultiPartFormDataContent(formData {
                append(
                    "payload_json",
                    Json.encodeToString(
                        CreateThread(
                            name = title,
                            message = firstMessage,
                            attachments = imageAttachments.mapIndexed { index, attachment ->
                                CreateMessage.Attachment(
                                    id = index,
                                    fileName = attachment.first,
                                    ephemeral = true,
                                )
                            }.ifEmpty { null },
                        )
                    ),
                    Headers.build {
                        append(HttpHeaders.ContentType, "application/json")
                    },
                )
                imageAttachments.forEachIndexed { index, attachment ->
                    append("file[$index]", attachment.second, Headers.build {
                        append(HttpHeaders.ContentType, "image/webp")
                        append(HttpHeaders.ContentDisposition, "filename=\"${attachment.first}\"")
                    })
                }
            }))
        }.assertSuccess().body<Channel>()
        if (secondMessage != null) {
            createMessage(threadChannel.id, secondMessage)
        }
    }

    suspend fun createMessage(channelId: String, message: CreateMessage) {
        client.post("channels/$channelId/messages") {
            setBody(message)
        }.assertSuccess()
    }

    suspend fun editMessage(
        channelId: String,
        messageId: String,
        message: CreateMessage,
        imageAttachments: List<Pair<String, ByteArray>> = emptyList(),
    ) {
        client.patch("channels/$channelId/messages/$messageId") {
            headers {
                this["Content-Type"] = "multipart/form-data"
            }
            setBody(MultiPartFormDataContent(formData {
                append(
                    "payload_json",
                    Json.encodeToString(
                        message.copy(
                            // Including the attachments doesn't work,
                            // despite Discord claiming it's required
                            attachments = emptyList(),
                        )
                    ),
                    Headers.build {
                        append(HttpHeaders.ContentType, "application/json")
                    },
                )
                imageAttachments.forEachIndexed { index, attachment ->
                    append("file[$index]", attachment.second, Headers.build {
                        append(HttpHeaders.ContentType, "image/webp")
                        append(HttpHeaders.ContentDisposition, "filename=\"${attachment.first}\"")
                    })
                }
            }))
        }.assertSuccess()
    }

    suspend fun getOldestThreadMessages(channelId: String) =
        client.get("channels/$channelId/messages") {
            parameter("after", "0")
            parameter("limit", "2")
        }
            .body<List<Message>>()

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
                println("Delaying by $retryAfter")
                delay(retryAfter + 3.seconds)
                val retry = client.request { takeFrom(request) }
                if (!retry.status.isSuccess()) {
                    System.err.println(
                        "Failed retry ${retry.request.url}: ${retry.headers} ${retry.bodyAsText()}"
                    )
                }
            }
        }
    }
}
