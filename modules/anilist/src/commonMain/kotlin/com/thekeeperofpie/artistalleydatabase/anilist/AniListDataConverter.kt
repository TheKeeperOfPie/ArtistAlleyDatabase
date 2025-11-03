package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Monitor
import artistalleydatabase.modules.anilist.generated.resources.Res
import artistalleydatabase.modules.anilist.generated.resources.aniList_entry_anime_indicator_content_description
import artistalleydatabase.modules.anilist.generated.resources.aniList_entry_manga_indicator_content_description
import com.anilist.data.fragment.AniListCharacter
import com.anilist.data.fragment.AniListMedia
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class AniListDataConverter(
    private val aniListJson: AniListJson
) {
    fun seriesEntry(media: AniListMedia): Entry.Prefilled<AniListMedia> {
        val title = media.title?.romaji ?: media.id.toString()
        val serializedValue =
            aniListJson.toJson(MediaColumnEntry(media.id.toString(), title.trim()))
        return Entry.Prefilled(
            value = media,
            id = mediaEntryId(media.id.toString()),
            text = title,
            trailingIcon = when (media.type) {
                MediaType.ANIME -> Icons.Default.Monitor
                MediaType.MANGA -> Icons.Default.Book
                else -> null
            },
            trailingIconContentDescription = when (media.type) {
                MediaType.ANIME -> Res.string.aniList_entry_anime_indicator_content_description
                MediaType.MANGA -> Res.string.aniList_entry_manga_indicator_content_description
                else -> null
            },
            image = media.coverImage?.medium,
            imageLink = media.type?.let { AniListDataUtils.mediaUrl(it, media.id.toString()) },
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

    fun seriesEntry2(media: AniListMedia): EntryForm2.MultiTextState.Entry.Prefilled<AniListMedia> {
        val title = media.title?.romaji ?: media.id.toString()
        val serializedValue =
            aniListJson.toJson(MediaColumnEntry(media.id.toString(), title.trim()))
        return EntryForm2.MultiTextState.Entry.Prefilled(
            value = media,
            id = mediaEntryId(media.id.toString()),
            text = title,
            image = media.coverImage?.medium,
            imageLink = media.type?.let { AniListDataUtils.mediaUrl(it, media.id.toString()) },
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

    fun seriesEntry(entry: MediaEntry): Entry.Prefilled<*> {
        val title = entry.title
        val nonNullTitle = title?.romaji ?: entry.id
        val serializedValue = aniListJson.toJson(MediaColumnEntry(entry.id, nonNullTitle))
        return Entry.Prefilled(
            value = entry,
            id = mediaEntryId(entry.id),
            text = nonNullTitle,
            trailingIcon = when (entry.type) {
                MediaEntry.Type.ANIME -> Icons.Default.Monitor
                MediaEntry.Type.MANGA -> Icons.Default.Book
                else -> null
            },
            trailingIconContentDescription = when (entry.type) {
                MediaEntry.Type.ANIME ->
                    Res.string.aniList_entry_anime_indicator_content_description
                MediaEntry.Type.MANGA ->
                    Res.string.aniList_entry_manga_indicator_content_description
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

    fun seriesEntry2(entry: MediaEntry): EntryForm2.MultiTextState.Entry.Prefilled<*> {
        val title = entry.title
        val nonNullTitle = title?.romaji ?: entry.id
        val serializedValue = aniListJson.toJson(MediaColumnEntry(entry.id, nonNullTitle))
        return EntryForm2.MultiTextState.Entry.Prefilled(
            value = entry,
            id = mediaEntryId(entry.id),
            text = nonNullTitle,
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

    fun seriesEntry(entry: MediaColumnEntry): Entry.Prefilled<MediaColumnEntry> {
        val serializedValue = aniListJson.toJson(MediaColumnEntry(entry.id, entry.title))
        return Entry.Prefilled(
            value = entry,
            id = mediaEntryId(entry.id),
            text = entry.title,
            image = null,
            imageLink = null,
            serializedValue = serializedValue,
            searchableValue = entry.title.trim()
        )
    }

    fun seriesEntry2(entry: MediaColumnEntry): EntryForm2.MultiTextState.Entry.Prefilled<MediaColumnEntry> {
        val serializedValue = aniListJson.toJson(MediaColumnEntry(entry.id, entry.title))
        return EntryForm2.MultiTextState.Entry.Prefilled(
            value = entry,
            id = mediaEntryId(entry.id),
            text = entry.title,
            image = null,
            imageLink = null,
            serializedValue = serializedValue,
            searchableValue = entry.title.trim()
        )
    }

    fun characterEntry(character: AniListCharacter): Entry.Prefilled<AniListCharacter> {
        val firstMedia = character.media?.nodes?.firstOrNull()
        val voiceActor = CharacterUtils.findVoiceActor(character, firstMedia)
        return characterEntry(
            value = character,
            id = character.id.toString(),
            image = character.image?.medium,
            first = character.name?.first,
            middle = character.name?.middle,
            last = character.name?.last,
            full = character.name?.full,
            native = character.name?.native,
            alternative = character.name?.alternative?.filterNotNull(),
            mediaTitle = firstMedia?.title?.romaji,
            staffId = voiceActor?.id,
            staffName = voiceActor?.name?.full,
            staffImage = voiceActor?.image?.medium,
        )
    }

    fun characterEntry2(character: AniListCharacter): EntryForm2.MultiTextState.Entry.Prefilled<AniListCharacter> {
        val firstMedia = character.media?.nodes?.firstOrNull()
        val voiceActor = CharacterUtils.findVoiceActor(character, firstMedia)
        return characterEntry2(
            value = character,
            id = character.id.toString(),
            image = character.image?.medium,
            first = character.name?.first,
            middle = character.name?.middle,
            last = character.name?.last,
            full = character.name?.full,
            native = character.name?.native,
            alternative = character.name?.alternative?.filterNotNull(),
            mediaTitle = firstMedia?.title?.romaji,
            staffId = voiceActor?.id,
            staffName = voiceActor?.name?.full,
            staffImage = voiceActor?.image?.medium,
        )
    }

    fun characterEntry(entry: CharacterColumnEntry): Entry.Prefilled<CharacterColumnEntry> =
        characterEntry(
            value = entry,
            id = entry.id,
            image = null,
            first = entry.name?.first,
            middle = entry.name?.middle,
            last = entry.name?.last,
            full = entry.name?.full,
            native = entry.name?.native,
            alternative = emptyList(),
            mediaTitle = null,
            staffId = null,
            staffName = null,
            staffImage = null,
        )

    fun characterEntry2(entry: CharacterColumnEntry): EntryForm2.MultiTextState.Entry.Prefilled<CharacterColumnEntry> =
        characterEntry2(
            value = entry,
            id = entry.id,
            image = null,
            first = entry.name?.first,
            middle = entry.name?.middle,
            last = entry.name?.last,
            full = entry.name?.full,
            native = entry.name?.native,
            alternative = emptyList(),
            mediaTitle = null,
            staffId = null,
            staffName = null,
            staffImage = null,
        )

    fun characterEntry(
        entry: CharacterEntry,
    ): Entry.Prefilled<CharacterEntry> {
        val voiceActor =
            CharacterUtils.findVoiceActor(aniListJson.json, entry, entry.mediaIds?.firstOrNull())
        return characterEntry(
            value = entry,
            id = entry.id,
            image = entry.image?.medium,
            first = entry.name?.first,
            middle = entry.name?.middle,
            last = entry.name?.last,
            full = entry.name?.full,
            native = entry.name?.native,
            alternative = entry.name?.alternative,
            mediaTitle = entry.mediaTitle,
            staffId = voiceActor?.id,
            staffName = voiceActor?.name?.full,
            staffImage = voiceActor?.image?.medium
        )
    }

    fun characterEntry2(
        entry: CharacterEntry,
    ): EntryForm2.MultiTextState.Entry.Prefilled<CharacterEntry> {
        val voiceActor =
            CharacterUtils.findVoiceActor(aniListJson.json, entry, entry.mediaIds?.firstOrNull())
        return characterEntry2(
            value = entry,
            id = entry.id,
            image = entry.image?.medium,
            first = entry.name?.first,
            middle = entry.name?.middle,
            last = entry.name?.last,
            full = entry.name?.full,
            native = entry.name?.native,
            alternative = entry.name?.alternative,
            mediaTitle = entry.mediaTitle,
            staffId = voiceActor?.id,
            staffName = voiceActor?.name?.full,
            staffImage = voiceActor?.image?.medium
        )
    }

    private inline fun <reified T> characterEntry(
        value: T,
        id: String,
        image: String?,
        first: String?,
        middle: String?,
        last: String?,
        full: String?,
        native: String?,
        alternative: List<String>?,
        mediaTitle: String?,
        staffId: String?,
        staffName: String?,
        staffImage: String?,
    ): Entry.Prefilled<T> {
        val canonicalName = CharacterUtils.buildCanonicalName(
            first = first,
            middle = middle,
            last = last,
        ) ?: id

        val displayName = CharacterUtils.buildDisplayName(canonicalName, alternative)

        val serializedValue = aniListJson.toJson(
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

        return Entry.Prefilled(
            value = value,
            id = characterEntryId(id),
            text = canonicalName,
            image = image,
            imageLink = AniListUtils.characterUrl(id),
            secondaryImage = staffImage,
            secondaryImageLink = staffId?.let(AniListUtils::staffUrl),
            titleText = displayName,
            subtitleText = listOfNotNull(mediaTitle, staffName).joinToString(separator = " / ")
                .ifEmpty { null },
            serializedValue = serializedValue,
            searchableValue = (listOf(last, middle, first) + alternative.orEmpty())
                .filterNot(String?::isNullOrBlank)
                .mapNotNull { it?.trim() }
                .joinToString(),
        )
    }

    private inline fun <reified T> characterEntry2(
        value: T,
        id: String,
        image: String?,
        first: String?,
        middle: String?,
        last: String?,
        full: String?,
        native: String?,
        alternative: List<String>?,
        mediaTitle: String?,
        staffId: String?,
        staffName: String?,
        staffImage: String?,
    ): EntryForm2.MultiTextState.Entry.Prefilled<T> {
        val canonicalName = CharacterUtils.buildCanonicalName(
            first = first,
            middle = middle,
            last = last,
        ) ?: id

        val displayName = CharacterUtils.buildDisplayName(canonicalName, alternative)

        val serializedValue = aniListJson.toJson(
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

        return EntryForm2.MultiTextState.Entry.Prefilled(
            value = value,
            id = characterEntryId(id),
            text = canonicalName,
            image = image,
            imageLink = AniListUtils.characterUrl(id),
            secondaryImage = staffImage,
            secondaryImageLink = staffId?.let(AniListUtils::staffUrl),
            titleText = displayName,
            subtitleText = listOfNotNull(mediaTitle, staffName).joinToString(separator = " / ")
                .ifEmpty { null },
            serializedValue = serializedValue,
            searchableValue = (listOf(last, middle, first) + alternative.orEmpty())
                .filterNot(String?::isNullOrBlank)
                .mapNotNull { it?.trim() }
                .joinToString(),
        )
    }

    private fun characterEntryId(id: String) = "aniListCharacter_$id"
    private fun mediaEntryId(id: String) = "aniListCharacter_$id"
}
