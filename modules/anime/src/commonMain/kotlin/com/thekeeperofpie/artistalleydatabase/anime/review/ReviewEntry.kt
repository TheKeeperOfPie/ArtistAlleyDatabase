package com.thekeeperofpie.artistalleydatabase.anime.review

import com.anilist.fragment.MediaAndReviewsReview
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry

data class ReviewEntry(
    val review: MediaAndReviewsReview,
    val media: MediaCompactWithTagsEntry,
)
