package com.thekeeperofpie.artistalleydatabase.browse

import androidx.compose.runtime.Immutable
import com.thekeeperofpie.artistalleydatabase.utils.Either

@Immutable
data class BrowseEntryModel(
    val image: String? = null,
    val link: String? = null,
    val text: String,
    val queryType: String,
    val queryIdOrString: Either<String, String> = Either.Right(text),
)
