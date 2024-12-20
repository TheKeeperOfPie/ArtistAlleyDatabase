package com.thekeeperofpie.artistalleydatabase.anime.ui

import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination

// TODO: Move these elsewhere

typealias ActivityDetailsRoute = (
    activityId: String,
    sharedTransitionScopeKey: String?,
) -> NavDestination

typealias ForumThreadRoute = (
    threadId: String,
    title: String?,
) -> NavDestination

typealias ForumThreadCommentRoute = (
    threadId: String,
    commentId: String,
    title: String?,
) -> NavDestination

typealias UserRoute = (
    id: String,
    SharedTransitionKey?,
    name: String,
    ImageState?,
) -> NavDestination

typealias StaffDetailsRoute = (
    staffId: String,
    SharedTransitionKey?,
    staffName: String?,
    staffSubtitle: String?,
    ImageState?,
    favorite: Boolean?,
) -> NavDestination

typealias StudioMediasRoute = (
    studioId: String,
    studioName: String,
) -> NavDestination

typealias SearchMediaGenreRoute = (
    genre: String,
    mediaType: MediaType,
) -> NavDestination

typealias SearchMediaTagRoute = (
    tagId: String,
    tagName: String,
    mediaType: MediaType,
) -> NavDestination

typealias SeasonalCurrentRoute = () -> NavDestination
