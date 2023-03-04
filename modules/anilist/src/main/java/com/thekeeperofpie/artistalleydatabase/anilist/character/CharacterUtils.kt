package com.thekeeperofpie.artistalleydatabase.anilist.character

import com.anilist.fragment.AniListCharacter
import com.anilist.fragment.AniListMedia
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson

object CharacterUtils {

    fun buildCanonicalName(
        entry: CharacterEntry
    ) = buildCanonicalName(
        first = entry.name?.first,
        middle = entry.name?.middle,
        last = entry.name?.last,
    )

    fun buildCanonicalName(
        entry: CharacterColumnEntry
    ) = buildCanonicalName(
        first = entry.name?.first,
        middle = entry.name?.middle,
        last = entry.name?.last,
    )

    fun buildCanonicalName(
        first: String?,
        middle: String?,
        last: String?,
    ) = when {
        !middle.isNullOrBlank() -> {
            when {
                last.isNullOrBlank() -> when {
                    first.isNullOrBlank() -> middle
                    else -> "$first $middle"
                }
                first.isNullOrBlank() -> when {
                    last.isBlank() -> middle
                    else -> "$middle $last"
                }
                else -> "$first $middle $last"
            }
        }
        last.isNullOrBlank() -> first
        first.isNullOrBlank() -> last
        else -> "$last $first"
    }.takeUnless(String?::isNullOrBlank)

    fun buildDisplayName(
        canonicalName: String,
        alternative: List<String>? = null,
    ) = canonicalName + alternative.orEmpty()
        .filterNot(String?::isNullOrBlank)
        .takeUnless(Collection<*>::isEmpty)
        ?.joinToString(prefix = " (", separator = ", ", postfix = ")")
        .orEmpty()

    fun parseVoiceActors(character: AniListCharacter) = character.media?.edges?.mapNotNull {
        val mediaId = it?.node?.id ?: return@mapNotNull null
        val voiceActors = it.voiceActors?.filterNotNull()?.mapNotNull {
            it.languageV2?.let { language ->
                CharacterEntry.VoiceActor(
                    id = it.id.toString(),
                    language = language,
                    name = CharacterEntry.Name(
                        first = it.name?.aniListStaffName?.first,
                        middle = it.name?.aniListStaffName?.middle,
                        last = it.name?.aniListStaffName?.last,
                        full = it.name?.aniListStaffName?.full,
                        native = it.name?.aniListStaffName?.native,
                        alternative = it.name?.aniListStaffName?.alternative
                            ?.filterNotNull(),
                    ),
                    image = CharacterEntry.Image(
                        large = it.image?.large,
                        medium = it.image?.medium,
                    ),
                )
            }
        }?.ifEmpty { null } ?: return@mapNotNull null
        mediaId.toString() to voiceActors
    }?.associate { it }.orEmpty()

    fun findVoiceActor(
        appJson: AppJson,
        character: CharacterEntry,
        mediaId: String?
    ): CharacterEntry.VoiceActor? {
        val voiceActors = character.voiceActors(appJson)

        // Find by exact media ID and exact language
        var voiceActor = mediaId?.let {
            voiceActors[it]?.find { it.language == "Japanese" }
        }

        // Find by any media ID and exact language
        if (voiceActor == null) {
            voiceActor = voiceActors.asSequence()
                .flatMap { it.value }
                .find { it.language == "Japanese" }
        }

        // Find by any media ID and any language
        if (voiceActor == null) {
            voiceActor = voiceActors.asSequence()
                .flatMap { it.value }
                .firstOrNull()
        }

        return voiceActor
    }

    fun findVoiceActor(
        character: AniListCharacter,
        media: AniListMedia?
    ): CharacterEntry.VoiceActor? {
        val voiceActors = parseVoiceActors(character)

        // Find by exact media ID and exact language
        var voiceActor = media?.let {
            voiceActors[it.id.toString()]?.find { it.language == "Japanese" }
        }

        // Find by any media ID and exact language
        if (voiceActor == null) {
            voiceActor = voiceActors.asSequence()
                .flatMap { it.value }
                .find { it.language == "Japanese" }
        }

        // Find by any media ID and any language
        if (voiceActor == null) {
            voiceActor = voiceActors.asSequence()
                .flatMap { it.value }
                .firstOrNull()
        }

        return voiceActor
    }
}