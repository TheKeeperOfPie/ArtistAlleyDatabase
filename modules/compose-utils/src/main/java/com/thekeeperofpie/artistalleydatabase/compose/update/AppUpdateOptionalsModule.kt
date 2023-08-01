package com.thekeeperofpie.artistalleydatabase.compose.update

import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AppUpdateOptionalsModule {

    @BindsOptionalOf
    fun bindOptionalAppUpdateChecker(): AppUpdateChecker
}
