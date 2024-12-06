package com.thekeeperofpie.artistalleydatabase.anime.reviews

import com.anilist.data.fragment.MediaAndReviewsReview

data class ReviewEntry<MediaEntry>(
    val review: MediaAndReviewsReview,
    val media: MediaEntry,
)
