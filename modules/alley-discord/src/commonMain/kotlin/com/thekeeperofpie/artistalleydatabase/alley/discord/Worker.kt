@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class, ExperimentalStdlibApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.alley.discord.secrets.BuildKonfig
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

        private const val DEBUG = BuildKonfig.isWasmDebug

        @JsStatic
        fun request(request: Request, env: Env): Promise<Response> = promise {
            val path = URL(request.url).pathname.removePrefix("/")
            val api = DiscordApi(env, json)
            return@promise when (path) {
                "connect" -> BotBackend.verifyArtistBrowser(request, env, api)
                "verify" -> BotBackend.verifyArtistDiscord(request, env, api)
                "interactions" -> BotBackend.handleInteraction(request, env, json, api)
                "commands" -> {
                    if (DEBUG) {
                        val result = api.syncCommands()
                        jsonResponse("Updated commands $result")
                    } else {
                        Responses.response404
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
                                    |        tempImages TEXT DEFAULT NULL,
                                    |        profileImage TEXT DEFAULT NULL,
                                    |        embeds TEXT DEFAULT NULL,
                                    |        editorNotes TEXT,
                                    |        lastEditor TEXT,
                                    |        lastEditTime TEXT,
                                    |        verifiedArtist INTEGER NOT NULL DEFAULT 0,
                                    |        newArtist INTEGER NOT NULL DEFAULT 0,
                                    |        PRIMARY KEY (id)
                                    |    );
                                    |CREATE TABLE
                                    |    IF NOT EXISTS artistCatalogQueueEntry (
                                    |        dataYear TEXT NOT NULL,
                                    |        booth TEXT NOT NULL,
                                    |        link TEXT NOT NULL,
                                    |        PRIMARY KEY (dataYear, booth)
                                    |    );
                                    |CREATE TABLE
                                    |    IF NOT EXISTS stampRallyQueueEntry (
                                    |        dataYear TEXT NOT NULL,
                                    |        booths TEXT NOT NULL,
                                    |        link TEXT NOT NULL,
                                    |        PRIMARY KEY (dataYear, link)
                                    |    );
                                    |    
                                    |CREATE TABLE
                                    |    IF NOT EXISTS stampRallyEntryAnimeExpo2026 (
                                    |        id TEXT NOT NULL,
                                    |        fandom TEXT NOT NULL COLLATE NOCASE,
                                    |        tables TEXT NOT NULL,
                                    |        startTables TEXT DEFAULT NULL,
                                    |        endTables TEXT DEFAULT NULL,
                                    |        links TEXT NOT NULL,
                                    |        tableMin INTEGER,
                                    |        totalCost INTEGER,
                                    |        prize TEXT,
                                    |        prizeLimit INTEGER,
                                    |        prizeMerch TEXT DEFAULT NULL,
                                    |        series TEXT NOT NULL,
                                    |        merch TEXT NOT NULL,
                                    |        notes TEXT,
                                    |        images TEXT NOT NULL,
                                    |        editorNotes TEXT,
                                    |        lastEditor TEXT,
                                    |        lastEditTime TEXT,
                                    |        PRIMARY KEY (id)
                                    |    );
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
                        val queries = Databases.backendDatabase(env).discordArtistEntryAnimeExpo2026Queries
                        queries.insertTestArtist()
                        queries.updateTestArtistSocialLink(listOf(socialLink))
                        jsonResponse("Updated test artist social link to $socialLink")
                    } else {
                        Responses.response404
                    }
                }
                else -> Responses.response404
            }
        }
    }
}
