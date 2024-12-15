package com.thekeeperofpie.artistalleydatabase.anime.users

import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable

object UserDestinations {

    @Serializable
    data class User(
        val userId: String? = null,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: UserHeaderParams? = null,
    ) : NavDestination {
        companion object {
            val route: UserRoute = { id, userSharedTransitionKey, name, imageState ->
                User(
                    userId = id,
                    sharedTransitionKey = userSharedTransitionKey,
                    headerParams = UserHeaderParams(
                        name = name,
                        bannerImage = null,
                        coverImage = imageState,
                    )
                )
            }
        }
    }

    @Serializable
    data class UserFavoriteCharacters(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserFavoriteMedia(
        val userId: String?,
        val userName: String? = null,
        val mediaType: MediaType,
    ) : NavDestination

    @Serializable
    data class UserFavoriteStaff(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserFavoriteStudios(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserFollowers(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserFollowing(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserList(
        val userId: String?,
        val userName: String?,
        val mediaType: MediaType,
        val mediaListStatus: MediaListStatus? = null,
    ) : NavDestination
}
