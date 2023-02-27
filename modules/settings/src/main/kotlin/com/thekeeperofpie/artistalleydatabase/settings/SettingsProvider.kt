package com.thekeeperofpie.artistalleydatabase.settings

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import kotlinx.serialization.Transient
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class SettingsProvider constructor(
    application: Application,
    private val appJson: AppJson,
) {

    companion object {
        private const val TAG = "SettingsProvider"
        const val EXPORT_FILE_NAME = "settings.json"
    }

    val serializer = Converters.PropertiesSerializer(SettingsData::class, appJson)

    private val sharedPreferences =
        application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var settingsData = SettingsData(appJson = appJson, sharedPreferences = sharedPreferences)
        private set

    fun overwrite(data: SettingsData) {
        data.initialize(appJson = appJson, sharedPreferences = sharedPreferences)
        settingsData = data
        serializer.nonTransientProperties()
            .forEach {
                (it.getDelegate(data) as SettingsData.SharedPreferenceDelegate<*>)
                    .onRestore(it, settingsData)
            }
    }

    class SettingsData(
        @Transient
        private var appJson: AppJson? = null,
        @Transient
        private var sharedPreferences: SharedPreferences? = null,
    ) : ArtSettings, EntrySettings {
        override var artEntryTemplate by delegate<ArtEntry?>()
        override var cropDocumentUri by delegate<Uri?>()
        var searchQuery by delegate<ArtEntry?>()

        fun initialize(appJson: AppJson, sharedPreferences: SharedPreferences) {
            this.appJson = appJson
            this.sharedPreferences = sharedPreferences
        }

        override fun toString(): String {
            return "SettingsData(" +
                    SettingsData::class.memberProperties
                        .sortedBy { it.name }
                        .filter { it.findAnnotations(Transient::class).isEmpty() }
                        .onEach { it.isAccessible = true }
                        .joinToString {
                            "${it.name}=${it.get(this@SettingsData)}"
                        } +
                    ")"
        }

        private fun <T> delegate() = SharedPreferenceDelegate<T>({ appJson }, { sharedPreferences })

        class SharedPreferenceDelegate<T>(
            private val appJson: () -> AppJson? = { null },
            private val sharedPreferences: () -> SharedPreferences? = { null },
            private val defaultValue: (() -> T)? = null
        ) : Converters.PropertiesSerializer.WritableDelegate {
            internal var value: T? = null
            private var loaded = false

            operator fun getValue(
                settingsData: SettingsData,
                property: KProperty<*>
            ): T? {
                if (!loaded) {
                    value = try {
                        val stringValue = sharedPreferences()?.getString(property.name, null)
                            .takeUnless(String?::isNullOrEmpty) ?: return null
                        @Suppress("UNCHECKED_CAST")
                        appJson()!!.json.run {
                            decodeFromString(
                                serializersModule.serializer(property.returnType),
                                stringValue
                            )
                        } as T
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading ${property.name}", e)
                        defaultValue?.invoke()
                    }

                    loaded = true
                }
                return value
            }

            override fun setValue(value: Any?) {
                loaded = true
                @Suppress("UNCHECKED_CAST")
                this.value = value as T
            }

            operator fun setValue(
                settingsData: SettingsData,
                property: KProperty<*>,
                value: T?
            ) {
                this.value = value
                serialize(property.returnType, property.name, value)
            }

            fun onRestore(property: KProperty1<SettingsData, *>, settingsData: SettingsData) {
                val value = property.get(settingsData)
                // Ignore null values to prefer anything that's set in the existing state
                // TODO: Codify how to merge old and new settings values
                if (value != null) {
                    loaded = true
                    serialize(property.returnType, property.name, value)
                }
            }

            private fun <T> serialize(type: KType, name: String, value: T) {
                val stringValue = appJson()!!.json.run {
                    encodeToString(serializersModule.serializer(type), value)
                }
                sharedPreferences()!!.run {
                    edit()
                        .putString(name, stringValue)
                        .apply()
                }
            }
        }
    }
}
