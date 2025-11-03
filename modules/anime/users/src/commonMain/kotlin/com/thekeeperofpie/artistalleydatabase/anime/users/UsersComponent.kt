package com.thekeeperofpie.artistalleydatabase.anime.users

import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteMediaViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteStaffViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteStudiosViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.follow.UserFollowSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.follow.UserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.social.UserSocialViewModel

interface UsersComponent {
    val aniListUserViewModelFactoryFactory: AniListUserViewModel.TypedFactory.Factory
    val userFavoriteCharactersViewModelFactoryFactory: UserFavoriteCharactersViewModel.TypedFactory.Factory
    val userFavoriteMediaViewModelFactoryFactory: UserFavoriteMediaViewModel.TypedFactory.Factory
    val userFavoriteStaffViewModelFactoryFactory: UserFavoriteStaffViewModel.TypedFactory.Factory
    val userFavoriteStudiosViewModelFactoryFactory: UserFavoriteStudiosViewModel.TypedFactory.Factory
    val userListViewModelFollowersFactoryFactory: UserListViewModel.Followers.TypedFactory.Factory
    val userListViewModelFollowingFactoryFactory: UserListViewModel.Following.TypedFactory.Factory
    val userSocialViewModelFollowersFactory: UserSocialViewModel.Followers.Factory
    val userSocialViewModelFollowingFactory: UserSocialViewModel.Following.Factory
    val userFollowSortFilterViewModelFactory: UserFollowSortFilterViewModel.Factory
    val usersSortFilterViewModelFactory: UsersSortFilterViewModel.Factory
}
