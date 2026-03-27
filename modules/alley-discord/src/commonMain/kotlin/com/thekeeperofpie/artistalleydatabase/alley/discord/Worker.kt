@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class, ExperimentalStdlibApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.alley.discord.Responses.response404
import com.thekeeperofpie.artistalleydatabase.cloudflare.promise
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URL
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import kotlin.js.Promise

@JsExport
class Worker {
    companion object {
        private val json = Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
            isLenient = true
        }

        private const val DEBUG = false

        @JsStatic
        fun request(request: Request, env: Env): Promise<Response> = promise {
            val path = URL(request.url).pathname.removePrefix("/")
            val api = DiscordApi(env, json)
            return@promise when (path) {
                "verify" -> BotBackend.verifyArtist(request, env, api)
                "interactions" -> BotBackend.handleInteraction(request, env, json, api)
                "commands" -> {
                    if (DEBUG) {
                        val result = api.syncCommands()
                        jsonResponse("Updated commands $result")
                    } else {
                        response404
                    }
                }
                "database" -> {
                    if (DEBUG) {
                        val socialLink = request.text().await()
                        Databases.editSqlDriver(env)
                            .execute(
                                identifier = null,
                                sql = """
                                    |CREATE TABLE
                                    |    IF NOT EXISTS artistEntryAnimeExpo2026 (
                                    |        id TEXT NOT NULL,
                                    |        status TEXT NOT NULL,
                                    |        booth TEXT COLLATE NOCASE,
                                    |        name TEXT NOT NULL COLLATE NOCASE,
                                    |        summary TEXT,
                                    |        socialLinks TEXT NOT NULL,
                                    |        storeLinks TEXT NOT NULL,
                                    |        portfolioLinks TEXT NOT NULL,
                                    |        catalogLinks TEXT NOT NULL,
                                    |        linkFlags INTEGER NOT NULL DEFAULT 0,
                                    |        linkFlags2 INTEGER NOT NULL DEFAULT 0,
                                    |        notes TEXT,
                                    |        commissions TEXT NOT NULL,
                                    |        commissionFlags INTEGER NOT NULL DEFAULT 0,
                                    |        seriesInferred TEXT NOT NULL,
                                    |        seriesConfirmed TEXT NOT NULL,
                                    |        merchInferred TEXT NOT NULL,
                                    |        merchConfirmed TEXT NOT NULL,
                                    |        images TEXT NOT NULL,
                                    |        fallbackImageYear TEXT DEFAULT NULL,
                                    |        editorNotes TEXT,
                                    |        lastEditor TEXT,
                                    |        lastEditTime TEXT,
                                    |        verifiedArtist INTEGER NOT NULL DEFAULT 0,
                                    |        PRIMARY KEY (id)
                                    |    )
                                """.trimMargin(),
                                parameters = 0,
                            ).await()
                        Databases.formSqlDriver(env)
                            .execute(
                                identifier = null,
                                sql = """
                                    |CREATE TABLE IF NOT EXISTS artistFormPublicKey (
                                    |    artistId TEXT NOT NULL,
                                    |    publicKey TEXT NOT NULL,
                                    |    PRIMARY KEY (artistId)
                                    |)
                                """.trimMargin(),
                                parameters = 0
                            ).await()
                        val queries = Databases.editDatabase(env).artistEntryAnimeExpo2026Queries
                        queries.insertTestArtist()
                        queries.updateTestArtistSocialLink(listOf(socialLink))
                        jsonResponse("Updated test artist social link to $socialLink")
                    } else {
                        response404
                    }
                }
                else -> response404
            }
        }
    }
}
