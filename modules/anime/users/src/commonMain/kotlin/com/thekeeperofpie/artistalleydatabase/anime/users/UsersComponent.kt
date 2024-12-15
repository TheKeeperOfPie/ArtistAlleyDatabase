package com.thekeeperofpie.artistalleydatabase.anime.users

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.users.social.UserSocialViewModel

interface UsersComponent {
    val aniListUserViewModelFactory: (SavedStateHandle, MediaDetailsRoute) -> AniListUserViewModel.Factory
    val userSocialViewModelFollowers: (userId: String?) -> UserSocialViewModel.Followers
    val userSocialViewModelFollowing: (userId: String?) -> UserSocialViewModel.Following
}
