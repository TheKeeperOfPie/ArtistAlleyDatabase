package com.thekeeperofpie.artistalleydatabase.art.sections

import androidx.annotation.StringRes
import artistalleydatabase.modules.art.generated.resources.Res
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention
import artistalleydatabase.modules.art.generated.resources.art_entry_source_custom
import artistalleydatabase.modules.art.generated.resources.art_entry_source_different
import artistalleydatabase.modules.art.generated.resources.art_entry_source_online
import artistalleydatabase.modules.art.generated.resources.art_entry_source_unknown
import co.touchlab.kermit.Logger
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource

@Serializable
sealed class SourceType(
    @Transient val serializedType: String = "",
    @Transient @StringRes val textRes: StringResource = Res.string.art_entry_source_unknown,
) {

    abstract fun serializedValue(json: Json): String

    companion object {

        private const val TAG = "SourceType"

        fun fromEntry(json: Json, entry: ArtEntry): SourceType {
            val value = entry.sourceValue
            return when (entry.sourceType) {
                "unknown" -> Unknown
                "convention" -> {
                    if (value != null) {
                        try {
                            json.decodeFromString<Convention>(value)
                        } catch (e: Exception) {
                            Logger.e(TAG, e) { "Failed to parse ${entry.sourceType}, sourceValue $value" }
                            Convention()
                        }
                    } else Convention()
                }
                else -> if (value.isNullOrBlank()) Unknown else {
                    Custom(value)
                }
            }
        }
    }

    @Serializable
    data class Convention(
        val name: String = "",
        val year: Int? = null,
        val hall: String = "",
        val booth: String = "",
    ) : SourceType("convention", Res.string.art_entry_source_convention) {
        override fun serializedValue(json: Json) = json.encodeToString(this)
    }

    data class Online(
        val name: String,
        val url: String,
    ) : SourceType("online", Res.string.art_entry_source_online) {
        override fun serializedValue(json: Json) = json.encodeToString(this)
    }

    data class Custom(val value: String) : SourceType("custom", Res.string.art_entry_source_custom) {
        override fun serializedValue(json: Json) = value
    }

    data object Unknown : SourceType("unknown", Res.string.art_entry_source_unknown) {
        override fun serializedValue(json: Json) = ""
    }

    /**
     * [Different] is internal value used for multi-edit to represent multiply not equal fields
     * across multiple entries. It should never be serialized to disk.
     */
    data object Different : SourceType("different", Res.string.art_entry_source_different) {
        override fun serializedValue(json: Json) = ""
    }
}
