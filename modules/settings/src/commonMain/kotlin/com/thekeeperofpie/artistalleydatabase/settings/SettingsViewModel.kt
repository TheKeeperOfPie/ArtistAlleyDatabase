package com.thekeeperofpie.artistalleydatabase.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppSettingsAlt
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.settings.generated.resources.Res
import artistalleydatabase.modules.settings.generated.resources.settings_clear
import artistalleydatabase.modules.settings.generated.resources.settings_clear_aniList_cache
import artistalleydatabase.modules.settings.generated.resources.settings_clear_aniList_oAuth
import artistalleydatabase.modules.settings.generated.resources.settings_clear_vgmdb_cache
import artistalleydatabase.modules.settings.generated.resources.settings_crop_clear
import artistalleydatabase.modules.settings.generated.resources.settings_database_fetch
import artistalleydatabase.modules.settings.generated.resources.settings_database_type
import artistalleydatabase.modules.settings.generated.resources.settings_delete
import artistalleydatabase.modules.settings.generated.resources.settings_enable_network_caching
import artistalleydatabase.modules.settings.generated.resources.settings_fetch
import artistalleydatabase.modules.settings.generated.resources.settings_force_crash
import artistalleydatabase.modules.settings.generated.resources.settings_force_crash_button
import artistalleydatabase.modules.settings.generated.resources.settings_hide_status_bar
import artistalleydatabase.modules.settings.generated.resources.settings_ignore_viewer
import artistalleydatabase.modules.settings.generated.resources.settings_label_id
import artistalleydatabase.modules.settings.generated.resources.settings_media_hide_ignored
import artistalleydatabase.modules.settings.generated.resources.settings_media_history_clear
import artistalleydatabase.modules.settings.generated.resources.settings_media_history_size
import artistalleydatabase.modules.settings.generated.resources.settings_media_history_toggle
import artistalleydatabase.modules.settings.generated.resources.settings_media_ignore_clear
import artistalleydatabase.modules.settings.generated.resources.settings_media_ignore_toggle
import artistalleydatabase.modules.settings.generated.resources.settings_network_logging_level_label
import artistalleydatabase.modules.settings.generated.resources.settings_open
import artistalleydatabase.modules.settings.generated.resources.settings_rebuild
import artistalleydatabase.modules.settings.generated.resources.settings_screenshot_mode
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_about_author_description
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_about_author_title
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_about_build_description
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_about_build_title
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_about_discord_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_about_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_about_privacy_policy_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_language_option_characters_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_language_option_media_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_language_option_staff_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_language_option_voice_actor_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_language_option_voice_actor_show_fallback_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_media_type_anime
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_media_type_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_media_type_manga
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_media_view_option_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_behavior_starting_screen_option_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_debug_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_history_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_ignore_label
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_theme_label
import artistalleydatabase.modules.settings.generated.resources.settings_unlock_all_features
import co.touchlab.kermit.Logger
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeRootNavDestination
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtist
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDao
import com.thekeeperofpie.artistalleydatabase.secrets.Secrets
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsSection
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppMetadataProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
class SettingsViewModel(
    private val mediaEntryDao: MediaEntryDao,
    private val characterEntryDao: CharacterEntryDao,
    private val albumEntryDao: AlbumEntryDao,
    private val cdEntryDao: CdEntryDao,
    private val musicalArtistDao: MusicalArtistDao,
    private val vgmdbArtistDao: VgmdbArtistDao,
    private val vgmdbJson: VgmdbJson,
    private val settings: SettingsProvider,
    private val vgmdbApi: VgmdbApi,
    private val aniListOAuthStore: AniListOAuthStore,
    monetizationController: MonetizationController,
    featureOverrideProvider: FeatureOverrideProvider,
    appMetadataProvider: AppMetadataProvider,
    private val aniListApi: AuthedAniListApi,
    historyController: HistoryController,
    ignoreController: IgnoreController,
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    val adsEnabled = monetizationController.adsEnabled
    val hasAuth = aniListOAuthStore.hasAuth
    val onClickDatabaseFetch = MutableSharedFlow<Unit>()

    private val themeSection = SettingsSection.Subsection(
        icon = Icons.Filled.ColorLens,
        labelTextRes = Res.string.settings_subsection_theme_label,
        children = listOf(
            SettingsSection.Dropdown(
                labelTextRes = Res.string.settings_subsection_theme_label,
                options = AppThemeSetting.entries,
                optionToText = { stringResource(it.textRes) },
                property = settings.appTheme,
            ),
        )
    )

    private val behaviorSection = SettingsSection.Subsection(
        icon = Icons.Filled.AppSettingsAlt,
        labelTextRes = Res.string.settings_subsection_behavior_label,
        children = listOf(
            SettingsSection.Dropdown(
                labelTextRes = Res.string.settings_subsection_behavior_media_type_label,
                options = listOf(MediaType.ANIME, MediaType.MANGA),
                optionToText = {
                    stringResource(
                        if (it == MediaType.ANIME) {
                            Res.string.settings_subsection_behavior_media_type_anime
                        } else {
                            Res.string.settings_subsection_behavior_media_type_manga
                        }
                    )
                },
                property = settings.preferredMediaType,
            ),
            SettingsSection.Dropdown(
                labelTextRes = Res.string.settings_subsection_behavior_media_view_option_label,
                options = MediaViewOption.entries,
                optionToText = { stringResource(it.textRes) },
                property = settings.mediaViewOption,
            ),
            SettingsSection.Dropdown(
                labelTextRes = Res.string.settings_subsection_behavior_starting_screen_option_label,
                options = AnimeRootNavDestination.entries
                    .filterNot { it == AnimeRootNavDestination.UNLOCK }
                    .toList(),
                optionToText = { stringResource(it.textRes) },
                property = settings.rootNavDestination,
            ),
            SettingsSection.Dropdown(
                labelTextRes = Res.string.settings_subsection_behavior_language_option_media_label,
                options = AniListLanguageOption.entries,
                optionToText = { stringResource(it.textRes) },
                property = settings.languageOptionMedia,
            ),
            SettingsSection.Dropdown(
                labelTextRes = Res.string.settings_subsection_behavior_language_option_characters_label,
                options = listOf(
                    AniListLanguageOption.DEFAULT,
                    AniListLanguageOption.NATIVE,
                    AniListLanguageOption.ROMAJI,
                ),
                optionToText = { stringResource(it.textRes) },
                property = settings.languageOptionCharacters,
            ),
            SettingsSection.Dropdown(
                labelTextRes = Res.string.settings_subsection_behavior_language_option_staff_label,
                options = listOf(
                    AniListLanguageOption.DEFAULT,
                    AniListLanguageOption.NATIVE,
                    AniListLanguageOption.ROMAJI,
                ),
                optionToText = { stringResource(it.textRes) },
                property = settings.languageOptionStaff,
            ),
            SettingsSection.Dropdown(
                labelTextRes = Res.string.settings_subsection_behavior_language_option_voice_actor_label,
                options = VoiceActorLanguageOption.entries,
                optionToText = { stringResource(it.textRes) },
                property = settings.languageOptionVoiceActor,
            ),
            SettingsSection.Switch(
                labelTextRes = Res.string.settings_subsection_behavior_language_option_voice_actor_show_fallback_label,
                property = settings.showFallbackVoiceActor,
            ),
        )
    )

    private val historySection = SettingsSection.Subsection(
        icon = Icons.Filled.History,
        labelTextRes = Res.string.settings_subsection_history_label,
        children = listOf(
            SettingsSection.Switch(
                labelTextRes = Res.string.settings_media_history_toggle,
                property = settings.mediaHistoryEnabled,
            ),
            SettingsSection.TextField(
                labelTextRes = Res.string.settings_media_history_size,
                initialValue = settings.mediaHistoryMaxEntries.value.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let {
                        settings.mediaHistoryMaxEntries.value = it
                    }
                }
            ),
            SettingsSection.Button(
                labelTextRes = Res.string.settings_media_history_clear,
                buttonTextRes = Res.string.settings_clear,
                onClick = historyController::clear,
            ),
            SettingsSection.Placeholder("viewMediaHistory"),
        ),
    )

    private val ignoreSection = SettingsSection.Subsection(
        icon = Icons.Filled.Block,
        labelTextRes = Res.string.settings_subsection_ignore_label,
        children = listOf(
            SettingsSection.Switch(
                labelTextRes = Res.string.settings_media_ignore_toggle,
                property = settings.mediaIgnoreEnabled,
            ),
            SettingsSection.Switch(
                labelTextRes = Res.string.settings_media_hide_ignored,
                property = settings.mediaIgnoreHide,
            ),
            SettingsSection.Button(
                labelTextRes = Res.string.settings_media_ignore_clear,
                buttonTextRes = Res.string.settings_clear,
                onClick = ignoreController::clear,
            ),
            SettingsSection.Placeholder("viewMediaIgnore"),
        ),
    )

    private val aboutSection = SettingsSection.Subsection(
        icon = Icons.Filled.Info,
        labelTextRes = Res.string.settings_subsection_about_label,
        children = listOf(
            SettingsSection.TextByString(
                id = Res.string.settings_subsection_about_build_title.toString(),
                title = { stringResource(Res.string.settings_subsection_about_build_title) },
                description = {
                    stringResource(
                        Res.string.settings_subsection_about_build_description,
                        appMetadataProvider.versionName,
                        appMetadataProvider.versionCode,
                    )
                },
            ),
            SettingsSection.Text(
                titleTextRes = Res.string.settings_subsection_about_author_title,
                descriptionTextRes = Res.string.settings_subsection_about_author_description,
            ),
            object : SettingsSection.Custom("openDiscord") {
                @Composable
                override fun Content(modifier: Modifier) {
                    val uriHandler = LocalUriHandler.current
                    ButtonRow(
                        labelTextRes = Res.string.settings_subsection_about_discord_label,
                        buttonTextRes = Res.string.settings_open,
                        onClick = { uriHandler.openUri(Secrets.discordServerInviteLink) }
                    )
                }
            },
            object : SettingsSection.Custom("openPrivacyPolicy") {
                @Composable
                override fun Content(modifier: Modifier) {
                    val uriHandler = LocalUriHandler.current
                    ButtonRow(
                        labelTextRes = Res.string.settings_subsection_about_privacy_policy_label,
                        buttonTextRes = Res.string.settings_open,
                        onClick = { uriHandler.openUri(Secrets.privacyPolicyLink) }
                    )
                }
            },
            // TODO: Re-add licenses
//            SettingsSection.Placeholder(id = "showLicenses"),
            SettingsSection.Placeholder(id = "openLastCrash"),
        )
    )

    private val debugSection = SettingsSection.Subsection(
        icon = Icons.Filled.Build,
        labelTextRes = Res.string.settings_subsection_debug_label,
        children = listOf(
            SettingsSection.Switch(
                labelTextRes = Res.string.settings_ignore_viewer,
                property = settings.ignoreViewer,
            ),
            SettingsSection.Button(
                labelTextRes = Res.string.settings_clear_aniList_cache,
                buttonTextRes = Res.string.settings_clear,
                onClick = ::clearAniListCache,
            ),
            SettingsSection.Button(
                labelTextRes = Res.string.settings_clear_vgmdb_cache,
                buttonTextRes = Res.string.settings_clear,
                onClick = ::clearVgmdbCache,
            ),
            SettingsSection.Button(
                labelTextRes = Res.string.settings_crop_clear,
                buttonTextRes = Res.string.settings_clear,
                onClick = { settings.cropImageUri.value = null },
            ),
            SettingsSection.Button(
                labelTextRes = Res.string.settings_clear_aniList_oAuth,
                buttonTextRes = Res.string.settings_clear,
                onClick = ::onClickClearAniListOAuth,
            ),
            SettingsSection.Switch(
                labelTextRes = Res.string.settings_enable_network_caching,
                property = settings.enableNetworkCaching,
            ),
            SettingsSection.Switch(
                labelTextRes = Res.string.settings_unlock_all_features,
                property = settings.unlockAllFeatures,
            ),
            SettingsSection.Switch(
                labelTextRes = Res.string.settings_hide_status_bar,
                property = settings.hideStatusBar,
            ),
            SettingsSection.Switch(
                labelTextRes = Res.string.settings_screenshot_mode,
                property = settings.screenshotMode,
            ),
            SettingsSection.Button(
                labelTextRes = Res.string.settings_force_crash,
                buttonTextRes = Res.string.settings_force_crash_button,
                onClick = { throw Exception() },
            ),
            SettingsSection.Button(
                labelTextRes = Res.string.settings_database_fetch,
                buttonTextRes = Res.string.settings_fetch,
                onClick = { onClickDatabaseFetch.tryEmit(Unit) },
            ),
            SettingsSection.Dropdown(
                labelTextRes = Res.string.settings_network_logging_level_label,
                options = NetworkSettings.NetworkLoggingLevel.entries,
                optionToText = { it.name },
                property = settings.networkLoggingLevel,
            ),
            object : SettingsSection.Custom(id = "clearDatabaseById") {
                private var selectedDatabase by mutableStateOf(SettingsScreen.DatabaseType.entries[0])
                private val dropdown = Dropdown(
                    labelTextRes = Res.string.settings_database_type,
                    options = SettingsScreen.DatabaseType.entries,
                    optionToText = { stringResource(it.labelRes) },
                    onItemSelected = { selectedDatabase = it },
                )

                @Composable
                override fun Content(modifier: Modifier) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier.padding(end = 16.dp)
                    ) {
                        dropdown.Content(
                            Modifier.weight(1f)
                        )

                        var clearDatabaseId by remember { mutableStateOf("") }
                        TextField(
                            value = clearDatabaseId,
                            onValueChange = { clearDatabaseId = it },
                            label = { Text(stringResource(Res.string.settings_label_id)) },
                            modifier = Modifier
                                .width(120.dp)
                                .padding(vertical = 10.dp),
                        )

                        FilledTonalButton(onClick = {
                            onClickClearDatabaseById(selectedDatabase, clearDatabaseId)
                        }) {
                            Text(text = stringResource(Res.string.settings_delete))
                        }
                    }
                }
            },
            SettingsSection.Dropdown(
                id = "rebuildDatabase",
                labelTextRes = Res.string.settings_database_type,
                options = SettingsScreen.DatabaseType.entries,
                optionToText = { stringResource(it.labelRes) },
                buttonTextRes = Res.string.settings_rebuild,
                onClickButton = ::onClickRebuildDatabase,
            ),
        )
    )

    val sections = listOfNotNull(
        SettingsSection.Placeholder(id = "header"),
        themeSection,
        behaviorSection,
        historySection,
        ignoreSection,
        SettingsSection.Placeholder(id = "featureTiers"),
        aboutSection,
        debugSection.takeIf { !featureOverrideProvider.isReleaseBuild },
    )

    private fun clearAniListCache() {
        viewModelScope.launch(Dispatchers.IO) {
            mediaEntryDao.deleteAll()
            characterEntryDao.deleteAll()
        }
    }

    private fun clearVgmdbCache() {
        viewModelScope.launch(Dispatchers.IO) {
            albumEntryDao.deleteAll()
            vgmdbArtistDao.deleteAll()
        }
    }

    private fun onClickClearDatabaseById(databaseType: SettingsScreen.DatabaseType, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (id.isBlank()) {
                when (databaseType) {
                    SettingsScreen.DatabaseType.ANILIST_CHARACTERS -> characterEntryDao.deleteAll()
                    SettingsScreen.DatabaseType.ANILIST_MEDIA -> mediaEntryDao.deleteAll()
                    SettingsScreen.DatabaseType.VGMDB_ALBUMS -> albumEntryDao.deleteAll()
                    SettingsScreen.DatabaseType.VGMDB_ARTISTS -> vgmdbArtistDao.deleteAll()
                    SettingsScreen.DatabaseType.MUSICAL_ARTISTS -> musicalArtistDao.deleteAll()
                }
            } else {
                when (databaseType) {
                    SettingsScreen.DatabaseType.ANILIST_CHARACTERS -> characterEntryDao.delete(id)
                    SettingsScreen.DatabaseType.ANILIST_MEDIA -> mediaEntryDao.delete(id)
                    SettingsScreen.DatabaseType.VGMDB_ALBUMS -> albumEntryDao.delete(id)
                    SettingsScreen.DatabaseType.VGMDB_ARTISTS -> vgmdbArtistDao.delete(id)
                    SettingsScreen.DatabaseType.MUSICAL_ARTISTS -> musicalArtistDao.delete(id)
                }
            }
        }
    }

    private fun onClickRebuildDatabase(databaseType: SettingsScreen.DatabaseType) {
        viewModelScope.launch(Dispatchers.IO) {
            when (databaseType) {
                SettingsScreen.DatabaseType.ANILIST_CHARACTERS,
                SettingsScreen.DatabaseType.ANILIST_MEDIA,
                SettingsScreen.DatabaseType.VGMDB_ALBUMS,
                SettingsScreen.DatabaseType.VGMDB_ARTISTS,
                    -> {
                    // TODO: Rebuild?
                }
                SettingsScreen.DatabaseType.MUSICAL_ARTISTS -> {
                    musicalArtistDao.deleteAll()
                    cdEntryDao.iterateEntriesNoTransaction { _: Int, cdEntry: CdEntry ->
                        val musicalArtists = cdEntry.performers.map(vgmdbJson::parseArtistColumn)
                            .map {
                                when (it) {
                                    is Either.Left -> {
                                        MusicalArtist(
                                            id = "custom_${it.value}",
                                            name = it.value,
                                            type = MusicalArtist.Type.CUSTOM,
                                        )
                                    }
                                    is Either.Right -> {
                                        val artistId = it.value.id
                                        val artistEntry = vgmdbArtistDao.getEntry(artistId)
                                        if (artistEntry == null) {
                                            MusicalArtist(
                                                id = "vgmdb_$artistId",
                                                name = it.value.name ?: "",
                                                type = MusicalArtist.Type.VGMDB,
                                            )
                                        } else {
                                            MusicalArtist(
                                                id = "vgmdb_${artistEntry.id}",
                                                name = artistEntry.name,
                                                type = MusicalArtist.Type.VGMDB,
                                                image = artistEntry.pictureThumb,
                                            )
                                        }
                                    }
                                }
                            }

                        musicalArtistDao.insert(musicalArtists)
                    }
                }
            }
        }
    }

    private fun onClickClearAniListOAuth() {
        viewModelScope.launch {
            aniListApi.logOut()
        }
    }

    fun debugDisableAds() {
        settings.adsEnabled.value = false
    }

    fun logOut() = aniListApi.logOut()

    fun checkMismatchedCdEntryData() {
        viewModelScope.launch(Dispatchers.IO) {
            var offset = 0
            var entries = cdEntryDao.getEntries(limit = 50, offset = offset)
            while (entries.isNotEmpty()) {
                entries.filter {
                    it.catalogId?.isNotBlank() == true
                }.forEach {
                    val (catalogId, albumId) = when (val result =
                        vgmdbJson.parseCatalogIdColumn(it.catalogId)) {
                        // Ignore non-VGMdb entries
                        is Either.Left -> return@forEach
                        is Either.Right -> result.value.catalogId to result.value.id
                    }
                    if (catalogId == null) {
                        Logger.d(TAG) { "Empty catalogId, entryId = ${it.id}" }
                        return@forEach
                    }

                    val album = vgmdbApi.getAlbum(albumId) ?: run {
                        Logger.d(TAG) { "Failed to load album for $catalogId" }
                        return@forEach
                    }

                    if (it.performers.size != album.performers.size) {
                        Logger.d(TAG) {
                            "Mismatched performer for $catalogId," +
                                    " expected = ${album.performers.size}," +
                                    " actual = ${it.performers.size}"
                        }
                    }

                    if (it.composers.size != album.composers.size) {
                        Logger.d(TAG) {
                            "Mismatched composers for $catalogId," +
                                    " expected = ${album.composers.size}," +
                                    " actual = ${it.composers.size}"
                        }
                    }
                }

                offset += entries.size
                entries = cdEntryDao.getEntries(limit = 50, offset = offset)
            }
        }
    }
}
