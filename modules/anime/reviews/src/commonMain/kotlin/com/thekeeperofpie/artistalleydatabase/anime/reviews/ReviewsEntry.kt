package com.thekeeperofpie.artistalleydatabase.anime.reviews

import com.anilist.data.MediaDetailsQuery

data class ReviewsEntry(
    val reviews: List<MediaDetailsQuery.Data.Media.Reviews.Node>,
    val hasMore: Boolean,
)
