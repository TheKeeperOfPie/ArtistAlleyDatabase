package com.thekeeperofpie.artistalleydatabase.anime.songs

import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AnimeSongsOptionalsHiltModule {

    @BindsOptionalOf
    fun bindOptionalAnimeSongsProvider(): AnimeSongsProvider
}
