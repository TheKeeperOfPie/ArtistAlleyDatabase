import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

// Diffs only capture additions, since removals aren't useful enough to surface to the user
@Serializable
internal data class ArtistDiff(
    val artistId: Uuid,
    val date: LocalDate,
    val booth: String? = null,
    val name: String,
    val seriesInferred: Set<String>? = null,
    val seriesConfirmed: Set<String>? = null,
    val merchInferred: Set<String>? = null,
    val merchConfirmed: Set<String>? = null,
    val isBrandNew: Boolean = false,
)
