package com.thekeeperofpie.artistalleydatabase.android_utils

import android.app.Application
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidUtilsHiltModule {

    @Singleton
    @Provides
    fun provideWorkManager(application: Application) = WorkManager.getInstance(application)

    @Singleton
    @Provides
    fun provideAppJson() = AppJson()
}
