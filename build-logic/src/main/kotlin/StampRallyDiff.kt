import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

// Rally diffs only capture new and added images
@Serializable
internal data class StampRallyDiff(
    val stampRallyId: Uuid,
    val date: LocalDate,
    val name: String,
    val images: List<DatabaseImage>?,
    val isBrandNew: Boolean,
)
