package com.thekeeperofpie.artistalleydatabase.test_utils

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TestAppHiltModule {

    @Singleton
    @Provides
    fun provideMasterKey(application: Application) = com.thekeeperofpie.artistalleydatabase.utils.CryptoUtils.masterKey(application)
}
