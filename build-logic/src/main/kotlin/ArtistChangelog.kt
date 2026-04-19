import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class ArtistChangelog(
    val additions: List<ArtistDiff>,
    val lastEditTimes: Map<DataYear, Map<Uuid, Instant>>,
)
