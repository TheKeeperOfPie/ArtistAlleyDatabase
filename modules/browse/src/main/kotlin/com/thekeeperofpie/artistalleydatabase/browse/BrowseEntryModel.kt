package com.thekeeperofpie.artistalleydatabase.browse

import com.thekeeperofpie.artistalleydatabase.android_utils.Either

data class BrowseEntryModel(
    val image: String? = null,
    val link: String? = null,
    val text: String,
    val queryType: String,
    val queryIdOrString: Either<String, String> = Either.Right(text),
)