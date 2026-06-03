import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class MerchDiff(
    val date: LocalDate,
    val merchIds: Set<String>,
)
