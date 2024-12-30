package com.thekeeperofpie.artistalleydatabase.anime.users

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteMediaViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteStaffViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteStudiosViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.follow.UserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.social.UserSocialViewModel

interface UsersComponent {
    val aniListUserViewModelFactory: (SavedStateHandle) -> AniListUserViewModel.Factory
    val userFavoriteCharactersViewModelFactory: (SavedStateHandle) -> UserFavoriteCharactersViewModel.Factory
    val userFavoriteMediaViewModelFactory: (SavedStateHandle) -> UserFavoriteMediaViewModel.Factory
    val userFavoriteStaffViewModelFactory: (SavedStateHandle) -> UserFavoriteStaffViewModel.Factory
    val userFavoriteStudiosViewModelFactory: (SavedStateHandle) -> UserFavoriteStudiosViewModel.Factory
    val userListViewModelFollowersFactory: (SavedStateHandle) -> UserListViewModel.Followers.Factory
    val userListViewModelFollowingFactory: (SavedStateHandle) -> UserListViewModel.Following.Factory
    val userSocialViewModelFollowers: (userId: String?) -> UserSocialViewModel.Followers
    val userSocialViewModelFollowing: (userId: String?) -> UserSocialViewModel.Following
}
