package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.image.crop.CropController
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope

/**
 * Dumping ground for dependencies which need to eventually be migrated to multiplatform.
 */
@Module
@InstallIn(SingletonComponent::class)
class MiscHiltModule {

    @Provides
    fun provideCropController(
        scope: CoroutineScope,
        application: Application,
        appFileSystem: AppFileSystem,
        settings: CropSettings,
    ) = CropController(scope, application, appFileSystem, settings)
}
