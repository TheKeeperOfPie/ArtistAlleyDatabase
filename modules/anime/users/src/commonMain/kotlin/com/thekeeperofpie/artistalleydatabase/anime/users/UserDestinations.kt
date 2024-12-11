package com.thekeeperofpie.artistalleydatabase.anime.users

import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable

object UserDestinations {

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
}
