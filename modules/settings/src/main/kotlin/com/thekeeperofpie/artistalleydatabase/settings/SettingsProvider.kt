package com.thekeeperofpie.artistalleydatabase.settings

import android.app.Application
import android.app.PendingIntent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.notification.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.android_utils.notification.NotificationIds
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty0
import kotlin.reflect.full.createType

class SettingsProvider(
    application: Application,
    masterKey: MasterKey,
    private val appJson: AppJson,
    sharedPreferencesFileName: String = PREFERENCES_NAME,
    private val crashNotificationContentIntent: PendingIntent?,
) : ArtSettings, EntrySettings, NetworkSettings, AnimeSettings, MonetizationSettings {

    companion object {
        const val EXPORT_FILE_NAME = "settings.json"
        const val PREFERENCES_NAME = "settings"
    }

    @VisibleForTesting
    val sharedPreferences = EncryptedSharedPreferences.create(
        application,
        sharedPreferencesFileName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override val artEntryTemplate = MutableStateFlow<ArtEntry?>(deserialize("artEntryTemplate"))
    override val cropDocumentUri = MutableStateFlow<Uri?>(deserialize("cropDocumentUri"))
    override val networkLoggingLevel = MutableStateFlow(
        deserialize("networkLoggingLevel") ?: NetworkSettings.NetworkLoggingLevel.NONE
    )
    override var savedAnimeFilters = MutableStateFlow(deserializeAnimeFilters())

    @Suppress("KotlinConstantConditions")
    override var showAdult = if (BuildConfig.BUILD_TYPE == "release") {
        val flow = MutableStateFlow(false)
        object : MutableStateFlow<Boolean> {
            override val replayCache: List<Boolean>
                get() = flow.replayCache
            override val subscriptionCount: StateFlow<Int>
                get() = flow.subscriptionCount
            override var value: Boolean
                get() = flow.value
                set(value) {
                    // Do not set anything on release
                }

            override suspend fun collect(collector: FlowCollector<Boolean>) =
                flow.collect(collector)

            override fun compareAndSet(expect: Boolean, update: Boolean) = true

            @ExperimentalCoroutinesApi
            override fun resetReplayCache() = Unit

            override fun tryEmit(value: Boolean) = true

            override suspend fun emit(value: Boolean) = Unit
        }
    } else {
        MutableStateFlow(deserialize("showAdult") ?: false)
    }

    override var collapseAnimeFiltersOnClose = MutableStateFlow(
        deserialize("collapseAnimeFiltersOnClose") ?: true
    )
    override var showIgnored = MutableStateFlow(deserialize("showIgnored") ?: true)
    override var ignoredAniListMediaIds =
        MutableStateFlow(deserialize("ignoredAniListMediaIds") ?: emptySet<Int>())
    override val animeNewsNetworkRegion =
        MutableStateFlow(deserialize("animeNewsNetworkRegion") ?: AnimeNewsNetworkRegion.USA_CANADA)

    override val animeNewsNetworkCategoriesIncluded = MutableStateFlow(
        deserialize("animeNewsNetworkCategoriesIncluded") ?: emptyList<AnimeNewsNetworkCategory>()
    )
    override val animeNewsNetworkCategoriesExcluded = MutableStateFlow(
        deserialize("animeNewsNetworkCategoriesExcluded") ?: emptyList<AnimeNewsNetworkCategory>()
    )

    override val crunchyrollNewsCategoriesIncluded = MutableStateFlow(
        deserialize("crunchyrollNewsCategoriesIncluded") ?: emptyList<CrunchyrollNewsCategory>()
    )
    override val crunchyrollNewsCategoriesExcluded = MutableStateFlow(
        deserialize("crunchyrollNewsCategoriesExcluded") ?: emptyList<CrunchyrollNewsCategory>()
    )

    override val adsEnabled = MutableStateFlow(deserialize("adsEnabled") ?: false)
    override val subscribed = MutableStateFlow(deserialize("subscribed") ?: false)

    var searchQuery = MutableStateFlow<ArtEntry?>(deserialize("searchQuery"))
    var navDrawerStartDestination =
        MutableStateFlow<String?>(deserialize("navDrawerStartDestination"))
    var hideStatusBar = MutableStateFlow(deserialize("hideStatusBar") ?: false)

    // Not exported
    var lastCrash = MutableStateFlow(deserialize("lastCrash") ?: "")
    var lastCrashShown = MutableStateFlow(deserialize("lastCrashShown") ?: false)
    var screenshotMode = MutableStateFlow(deserialize("screenshotMode") ?: false)

    override var unlockAllFeatures = MutableStateFlow(deserialize("unlockAllFeatures") ?: false)

    init {
        val mainThreadId = Looper.getMainLooper().thread.id
        val existingExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (thread.id == mainThreadId) {
                val stackString = throwable.stackTraceToString()
                lastCrash.value = stackString
                lastCrashShown.value = false
                serializeWithoutApply("lastCrash", stackString).commit()
                serializeWithoutApply("lastCrashShown", false).commit()
            }
            existingExceptionHandler?.uncaughtException(thread, throwable)
        }

        val lastCrashText = lastCrash.value
        if (crashNotificationContentIntent != null
            && lastCrashText.isNotBlank()
            && !lastCrashShown.value
        ) {
            NotificationManagerCompat.from(application).apply {
                // TODO: Prompt user for POST_NOTIFICATIONS permission
                if (ContextCompat.checkSelfPermission(
                        application,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@apply
                }

                // TODO: This should live somewhere else
                notify(
                    NotificationIds.INFO_CRASH.id,
                    NotificationCompat.Builder(application, NotificationChannels.INFO.channel)
                        .setAutoCancel(true)
                        .setContentTitle(
                            application.getString(
                                R.string.settings_notification_info_crash_title,
                                application.getString(UtilsStringR.app_name)
                            )
                        )
                        .setSmallIcon(R.drawable.baseline_error_outline_24)
                        .setContentText(lastCrashText)
                        .setContentIntent(crashNotificationContentIntent)
                        .build()
                )
            }
        }
    }

    private fun deserializeAnimeFilters(): Map<String, FilterData> {
        val stringValue = sharedPreferences.getString("savedAnimeFilters", "")
        if (stringValue.isNullOrBlank()) return emptyMap()
        return appJson.json.decodeFromString(stringValue)
    }

    val settingsData: SettingsData
        get() = SettingsData(
            artEntryTemplate = artEntryTemplate.value,
            cropDocumentUri = cropDocumentUri.value,
            networkLoggingLevel = networkLoggingLevel.value,
            searchQuery = searchQuery.value,
            collapseAnimeFiltersOnClose = collapseAnimeFiltersOnClose.value,
            savedAnimeFilters = savedAnimeFilters.value,
            showAdult = showAdult.value,
            showIgnored = showIgnored.value,
            ignoredAniListMediaIds = ignoredAniListMediaIds.value,
            animeNewsNetworkRegion = animeNewsNetworkRegion.value,
            animeNewsNetworkCategoriesIncluded = animeNewsNetworkCategoriesIncluded.value,
            animeNewsNetworkCategoriesExcluded = animeNewsNetworkCategoriesExcluded.value,
            crunchyrollNewsCategoriesIncluded = crunchyrollNewsCategoriesIncluded.value,
            crunchyrollNewsCategoriesExcluded = crunchyrollNewsCategoriesExcluded.value,
            navDrawerStartDestination = navDrawerStartDestination.value,
            hideStatusBar = hideStatusBar.value,
            adsEnabled = adsEnabled.value,
            subscribed = subscribed.value,
        )

    // Initialization separated into its own method so that tests can cancel the StateFlow job
    fun initialize(scope: CoroutineScope) {
        subscribeProperty(scope, ::artEntryTemplate)
        subscribeProperty(scope, ::cropDocumentUri)
        subscribeProperty(scope, ::networkLoggingLevel)
        subscribeProperty(scope, ::searchQuery)
        subscribeProperty(scope, ::collapseAnimeFiltersOnClose)
        subscribeProperty(scope, ::showAdult)
        subscribeProperty(scope, ::showIgnored)
        subscribeProperty(scope, ::navDrawerStartDestination)
        subscribeProperty(scope, ::hideStatusBar)
        subscribeProperty(scope, ::lastCrash)
        subscribeProperty(scope, ::lastCrashShown)
        subscribeProperty(scope, ::screenshotMode)
        subscribeProperty(scope, ::unlockAllFeatures)
        subscribeProperty(scope, ::animeNewsNetworkRegion)
        subscribeProperty(scope, ::adsEnabled)
        subscribeProperty(scope, ::subscribed)

        scope.launch(CustomDispatchers.IO) {
            ignoredAniListMediaIds.drop(1).collectLatest {
                val stringValue = appJson.json.run { encodeToString(it) }
                sharedPreferences.edit()
                    .putString("ignoredAniListMediaIds", stringValue)
                    .apply()
            }
        }
        scope.launch(CustomDispatchers.IO) {
            savedAnimeFilters.drop(1).collectLatest {
                val stringValue = appJson.json.run { encodeToString(it) }
                sharedPreferences.edit()
                    .putString("savedAnimeFilters", stringValue)
                    .apply()
            }
        }
        scope.launch(CustomDispatchers.IO) {
            animeNewsNetworkCategoriesIncluded.drop(1).collectLatest {
                val stringValue = appJson.json.run { encodeToString(it) }
                sharedPreferences.edit()
                    .putString("animeNewsNetworkCategoriesIncluded", stringValue)
                    .apply()
            }
        }
        scope.launch(CustomDispatchers.IO) {
            animeNewsNetworkCategoriesExcluded.drop(1).collectLatest {
                val stringValue = appJson.json.run { encodeToString(it) }
                sharedPreferences.edit()
                    .putString("animeNewsNetworkCategoriesExcluded", stringValue)
                    .apply()
            }
        }
        scope.launch(CustomDispatchers.IO) {
            crunchyrollNewsCategoriesIncluded.drop(1).collectLatest {
                val stringValue = appJson.json.run { encodeToString(it) }
                sharedPreferences.edit()
                    .putString("crunchyrollNewsCategoriesIncluded", stringValue)
                    .apply()
            }
        }
        scope.launch(CustomDispatchers.IO) {
            crunchyrollNewsCategoriesExcluded.drop(1).collectLatest {
                val stringValue = appJson.json.run { encodeToString(it) }
                sharedPreferences.edit()
                    .putString("crunchyrollNewsCategoriesExcluded", stringValue)
                    .apply()
            }
        }
    }

    private inline fun <reified T> subscribeProperty(
        scope: CoroutineScope,
        property: KProperty0<MutableStateFlow<T>>,
    ) = scope.launch(CustomDispatchers.IO) {
        property.get().drop(1).collectLatest {
            serialize(property.name, it)
        }
    }

    suspend fun overwrite(data: SettingsData) {
        artEntryTemplate.emit(data.artEntryTemplate)
        cropDocumentUri.emit(data.cropDocumentUri)
        networkLoggingLevel.emit(data.networkLoggingLevel)
        searchQuery.emit(data.searchQuery)
        savedAnimeFilters.emit(data.savedAnimeFilters)
        collapseAnimeFiltersOnClose.emit(data.collapseAnimeFiltersOnClose)
        showAdult.emit(data.showAdult)
        showIgnored.emit(data.showIgnored)
        ignoredAniListMediaIds.emit(data.ignoredAniListMediaIds)
        animeNewsNetworkRegion.emit(data.animeNewsNetworkRegion)
        animeNewsNetworkCategoriesIncluded.emit(data.animeNewsNetworkCategoriesIncluded)
        animeNewsNetworkCategoriesExcluded.emit(data.animeNewsNetworkCategoriesExcluded)
        crunchyrollNewsCategoriesIncluded.emit(data.crunchyrollNewsCategoriesIncluded)
        crunchyrollNewsCategoriesExcluded.emit(data.crunchyrollNewsCategoriesExcluded)
        navDrawerStartDestination.emit(data.navDrawerStartDestination)
        hideStatusBar.emit(data.hideStatusBar)
        adsEnabled.emit(data.adsEnabled)
        subscribed.emit(data.subscribed)
    }

    private inline fun <reified T> deserialize(name: String): T? {
        val stringValue = sharedPreferences.getString(name, "")
        return if (stringValue.isNullOrBlank()) {
            null
        } else try {
            appJson.json.decodeFromString<T>(stringValue)
        } catch (ignored: Throwable) {
            null
        }
    }

    private inline fun <reified T> serialize(name: String, value: T) =
        serializeWithoutApply(name, value).apply()

    private inline fun <reified T> serializeWithoutApply(
        name: String,
        value: T,
    ): SharedPreferences.Editor {
        val stringValue = appJson.json.run {
            encodeToString(
                // Create type as nullable to encapsulate both null and non-null
                serializersModule.serializer(T::class.createType(nullable = true)),
                value
            )
        }
        return sharedPreferences.edit()
            .putString(name, stringValue)
    }
}
