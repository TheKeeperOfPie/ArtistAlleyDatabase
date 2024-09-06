package com.thekeeperofpie.artistalleydatabase.compose.update

import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
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
