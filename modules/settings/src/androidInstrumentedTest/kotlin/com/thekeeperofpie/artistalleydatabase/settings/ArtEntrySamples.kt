package com.thekeeperofpie.artistalleydatabase.settings

import com.benasher44.uuid.Uuid
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import kotlinx.datetime.Instant
import kotlin.math.absoluteValue
import kotlin.random.Random

object ArtEntrySamples {

    /**
     * ```json
     * {
     *     "id": "76156313-a46f-4d35-b09d-04eb7a488304",
     *     "artists": [
     *         "SomeArtistOne"
     *     ],
     *     "sourceType": "convention",
     *     "sourceValue": "{\"name\":\"Anime Convention One\",\"year\":2023,\"hall\":\"ArtistAlleyOne\",\"booth\":\"One\"}",
     *     "seriesSerialized": [
     *         "{\"id\":100,\"title\":\"Default anime title one\"}"
     *     ],
     *     "seriesSearchable": [
     *         "Default anime title one, Secondary title one, Tertiary title one"
     *     ],
     *     "charactersSerialized": [
     *         "{\"id\":200,\"name\":{\"first\":\"Firstnameone\",\"last\":\"Lastnameone\",\"full\":\"Firstnameone Lastnameone\",\"native\":\"Native Name One\"}}"
     *     ],
     *     "charactersSearchable": [
     *         "Lastnameone, Firstnameone, Alternative Name One"
     *     ],
     *     "tags": [
     *         "TagOne1",
     *         "TagOne2"
     *     ],
     *     "lastEditTime": 1660000001234,
     *     "imageWidth": 1920,
     *     "imageHeight": 1080,
     *     "notes": "Title: piece title one",
     *     "locks": {
     *         "artistsLocked": true,
     *         "sourceLocked": true,
     *         "seriesLocked": true,
     *         "charactersLocked": true
     *     }
     * }
     * ```
     */
    private val BASE = ArtEntry(
        id = "76156313-a46f-4d35-b09d-04eb7a488304",
        artists = listOf("SomeArtistOne"),
        sourceType = "convention",
        sourceValue = """{\"name\":\"Anime Convention One\",\"year\":2023,\"hall\":\"ArtistAlleyOne\",\"booth\":\"One\"}""",
        seriesSerialized = listOf("""{\"id\":100,\"title\":\"Default anime titlec\"}"""),
        seriesSearchable = listOf("Default anime title one, Secondary title one, Tertiary title one"),
        charactersSerialized = listOf("""{\"id\":200,\"name\":{\"first\":\"Firstnameone\",\"last\":\"Lastnameone\",\"full\":\"Firstnameone Lastnameone\",\"native\":\"Native Name One\"}}"""),
        charactersSearchable = listOf("Lastnameone, Firstnameone, Alternative Name One"),
        tags = listOf("TagOne1", "TagOne2"),
        price = null,
        lastEditTime = Instant.fromEpochMilliseconds(1660000001234L),
        imageWidth = 1920,
        imageHeight = 1080,
        printWidth = null,
        printHeight = null,
        notes = "Title: piece title one",
        locks = ArtEntry.Locks(
            artistsLocked = true,
            sourceLocked = true,
            seriesLocked = true,
            charactersLocked = true,
            tagsLocked = false,
            notesLocked = false,
            printSizeLocked = false
        )
    )

    private const val PLACEHOLDER = "One"

    private fun String.capitalize() =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    fun build(uniqueIdentifier: String): ArtEntry {
        fun String.replace() = replace(PLACEHOLDER.capitalize(), uniqueIdentifier.capitalize())
            .replace(PLACEHOLDER.lowercase(), uniqueIdentifier.lowercase())

        return BASE.run {
            copy(
                id = Uuid.randomUUID().toString(),
                artists = artists.map(String::replace),
                sourceType = sourceType,
                sourceValue = sourceValue?.replace(),
                seriesSerialized = seriesSerialized.map(String::replace),
                seriesSearchable = seriesSearchable.map(String::replace),
                charactersSerialized = charactersSerialized.map(String::replace),
                charactersSearchable = charactersSearchable.map(String::replace),
                tags = tags.map(String::replace),
                lastEditTime = Instant.fromEpochMilliseconds(Random.nextInt().toLong()),
                imageWidth = uniqueIdentifier.hashCode().absoluteValue.coerceAtLeast(1),
                imageHeight = uniqueIdentifier.reversed().hashCode().absoluteValue.coerceAtLeast(1),
                notes = notes?.replace(),
            )
        }
    }
}
