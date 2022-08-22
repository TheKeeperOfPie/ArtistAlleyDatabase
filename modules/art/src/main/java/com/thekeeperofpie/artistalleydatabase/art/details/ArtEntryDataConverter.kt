package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Monitor
import com.anilist.fragment.AniListCharacter
import com.anilist.fragment.AniListMedia
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListStringR
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.art.json.ArtJson
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils.Either
import javax.inject.Inject

class ArtEntryDataConverter @Inject constructor(
    private val artJson: ArtJson
) {

    fun databaseToSeriesEntry(value: String) =
        when (val either = artJson.parseSeriesColumn(value)) {
            is Either.Right -> seriesEntry(either.value)
            is Either.Left -> EntrySection.MultiText.Entry.Custom(either.value)
        }

    fun databaseToCharacterEntry(value: String) =
        when (val either = artJson.parseCharacterColumn(value)) {
            is Either.Right -> characterEntry(either.value)
            is Either.Left -> EntrySection.MultiText.Entry.Custom(either.value)
        }

    fun seriesEntry(media: AniListMedia): EntrySection.MultiText.Entry.Prefilled {
        val title = media.title?.romaji ?: media.id.toString()
        val serializedValue = artJson.toJson(MediaColumnEntry(media.id, title.trim()))
        return EntrySection.MultiText.Entry.Prefilled(
            id = media.id.toString(),
            text = title,
            trailingIcon = when (media.type) {
                MediaType.ANIME -> Icons.Default.Monitor
                MediaType.MANGA -> Icons.Default.Book
                else -> null
            },
            trailingIconContentDescription = when (media.type) {
                MediaType.ANIME -> AniListStringR.aniList_entry_anime_indicator_content_description
                MediaType.MANGA -> AniListStringR.aniList_entry_manga_indicator_content_description
                else -> null
            },
            image = media.coverImage?.medium,
            imageLink = AniListUtils.mediaUrl(media.type, media.id),
            serializedValue = serializedValue,
            searchableValue = (listOf(
                media.title?.romaji,
                media.title?.english,
                media.title?.native,
            ) + media.synonyms.orEmpty())
                .filterNot(String?::isNullOrBlank)
                .mapNotNull { it?.trim() }
                .joinToString()
        )
    }

    fun seriesEntry(entry: MediaEntry): EntrySection.MultiText.Entry {
        val title = entry.title
        val nonNullTitle = title?.romaji ?: entry.id.toString()
        val serializedValue = artJson.toJson(MediaColumnEntry(entry.id, nonNullTitle))
        return EntrySection.MultiText.Entry.Prefilled(
            id = entry.id.toString(),
            text = nonNullTitle,
            trailingIcon = when (entry.type) {
                MediaEntry.Type.ANIME -> Icons.Default.Monitor
                MediaEntry.Type.MANGA -> Icons.Default.Book
                else -> null
            },
            trailingIconContentDescription = when (entry.type) {
                MediaEntry.Type.ANIME ->
                    AniListStringR.aniList_entry_anime_indicator_content_description
                MediaEntry.Type.MANGA ->
                    AniListStringR.aniList_entry_manga_indicator_content_description
                else -> null
            },
            image = entry.image?.medium,
            imageLink = AniListUtils.mediaUrl(entry.type, entry.id),
            serializedValue = serializedValue,
            searchableValue = (listOf(
                title?.romaji,
                title?.english,
                title?.native
            ) + entry.synonyms.orEmpty())
                .filterNot(String?::isNullOrBlank)
                .joinToString()
        )
    }

    fun seriesEntry(entry: MediaColumnEntry): EntrySection.MultiText.Entry {
        val serializedValue = artJson.toJson(MediaColumnEntry(entry.id, entry.title))
        return EntrySection.MultiText.Entry.Prefilled(
            id = entry.id.toString(),
            text = entry.title,
            image = null,
            imageLink = null,
            serializedValue = serializedValue,
            searchableValue = entry.title.trim()
        )
    }

    fun characterEntry(character: AniListCharacter) =
        characterEntry(
            id = character.id,
            image = character.image?.medium,
            first = character.name?.first,
            middle = character.name?.middle,
            last = character.name?.last,
            full = character.name?.full,
            native = character.name?.native,
            alternative = character.name?.alternative?.filterNotNull(),
            mediaTitle = character.media?.nodes?.firstOrNull()?.aniListMedia?.title?.romaji
        )

    fun characterEntry(entry: CharacterColumnEntry) =
        characterEntry(
            id = entry.id,
            image = null,
            first = entry.name?.first,
            middle = entry.name?.middle,
            last = entry.name?.last,
            full = entry.name?.full,
            native = entry.name?.native,
            alternative = emptyList(),
            mediaTitle = null,
        )

    fun characterEntry(entry: CharacterEntry, media: List<MediaEntry>) =
        characterEntry(
            id = entry.id,
            image = entry.image?.medium,
            first = entry.name?.first,
            middle = entry.name?.middle,
            last = entry.name?.last,
            full = entry.name?.full,
            native = entry.name?.native,
            alternative = entry.name?.alternative,
            mediaTitle = media.firstOrNull()?.title?.romaji,
        )

    private fun characterEntry(
        id: Int,
        image: String?,
        first: String?,
        middle: String?,
        last: String?,
        full: String?,
        native: String?,
        alternative: List<String>?,
        mediaTitle: String?
    ): EntrySection.MultiText.Entry.Prefilled {
        val canonicalName = CharacterUtils.buildCanonicalName(
            first = first,
            middle = middle,
            last = last,
        ) ?: id.toString()

        val displayName = CharacterUtils.buildDisplayName(canonicalName, alternative)

        val serializedValue = artJson.toJson(
            CharacterColumnEntry(
                id, CharacterColumnEntry.Name(
                    first = first?.trim(),
                    middle = middle?.trim(),
                    last = last?.trim(),
                    full = full?.trim(),
                    native = native?.trim(),
                )
            )
        )
        return EntrySection.MultiText.Entry.Prefilled(
            id = id.toString(),
            text = canonicalName,
            image = image,
            imageLink = AniListUtils.characterUrl(id),
            titleText = displayName,
            subtitleText = mediaTitle,
            serializedValue = serializedValue,
            searchableValue = (listOf(last, middle, first) + alternative.orEmpty())
                .filterNot(String?::isNullOrBlank)
                .mapNotNull { it?.trim() }
                .joinToString()
        )
    }
}