import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class AlleyChangelog(
    val artistDiffs: Map<DataYear, List<ArtistDiff>>,
    val artistLastEditTimes: Map<DataYear, Map<Uuid, Instant>>,
    val rallyDiffs: Map<DataYear, List<StampRallyDiff>>,
    val rallyLastEditTimes: Map<DataYear, Map<Uuid, Instant>>,
    val seriesDiffs: List<SeriesDiff>,
    val merchDiffs: List<MerchDiff>,
)
