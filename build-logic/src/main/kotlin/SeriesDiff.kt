import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class SeriesDiff(
    val date: LocalDate,
    val seriesIds: Set<String>,
)
