package com.thekeeperofpie.artistalleydatabase.anilist.character

import androidx.annotation.Discouraged
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Ignore
import androidx.room.PrimaryKey
import co.touchlab.kermit.Logger
import com.anilist.data.fragment.AniListCharacter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity("character_entries")
data class CharacterEntry(
    @PrimaryKey
    val id: String,
    @Embedded(prefix = "name_")
    val name: Name? = null,
    @Embedded(prefix = "image_")
    val image: Image? = null,
    val mediaIds: List<String>? = null,
    val mediaTitle: String? = null,
    @Discouraged("Prefer #voiceActors(AppJson)")
    val voiceActors: Map<String, String>? = null
) {

    companion object {
        private const val TAG = "CharacterEntry"
    }

    constructor(
        character: AniListCharacter,
        json: Json,
    ) : this(
        id = character.id.toString(),
        name = character.name?.run {
            Name(
                first = first?.trim(),
                middle = middle?.trim(),
                last = last?.trim(),
                full = full?.trim(),
                native = native?.trim(),
                alternative = alternative?.filterNotNull()?.map(String::trim),
            )
        },
        image = Image(
            large = character.image?.large,
            medium = character.image?.medium,
        ),
        mediaIds = character.media?.nodes?.mapNotNull { it?.id?.toString() },
        mediaTitle = character.media?.nodes?.firstNotNullOf { it?.title?.romaji },
        voiceActors = CharacterUtils.parseVoiceActors(character)
            .mapValues { (_, value) -> json.encodeToString<List<VoiceActor>>(value) },
    )

    @Ignore
    @Transient
    @kotlinx.serialization.Transient
    private lateinit var _voiceActors: Map<String, List<VoiceActor>>

    fun voiceActors(json: Json): Map<String, List<VoiceActor>> {
        if (!::_voiceActors.isInitialized) {
            _voiceActors = voiceActors?.mapNotNull { (mediaId, value) ->
                if (value.startsWith("[")) {
                    try {
                        return@mapNotNull mediaId to
                                json.decodeFromString<List<VoiceActor>>(value)
                    } catch (e: Exception) {
                        Logger.e(TAG, e) { "Fail to parse VoiceActor: $value" }
                    }
                }
                null
            }?.associate { it }
                .orEmpty()
        }

        return _voiceActors
    }

    @Serializable
    data class Name(
        val first: String? = null,
        val middle: String? = null,
        val last: String? = null,
        val full: String? = null,
        val native: String? = null,
        val alternative: List<String>? = null,
    )

    @Serializable
    data class Image(
        val large: String? = null,
        val medium: String? = null,
    )

    @Serializable
    data class VoiceActor(
        val id: String,
        val language: String?,
        val name: Name,
        val image: Image?,
    )
}

@Entity(tableName = "character_entries_fts")
@Fts4(contentEntity = CharacterEntry::class)
data class CharacterEntryFts(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowId: Int? = null,
    val id: String,
    @Embedded(prefix = "name_")
    val name: CharacterEntry.Name?,
    @Embedded(prefix = "image_")
    val image: CharacterEntry.Image?,
    val mediaIds: List<String>?,
    val mediaTitle: String?,
    val voiceActors: Map<String, String>?,
)
