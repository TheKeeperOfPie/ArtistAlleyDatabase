package com.thekeeperofpie.artistalleydatabase.alley.edit

import coil3.ComponentRegistry
import coil3.map.Mapper
import com.thekeeperofpie.artistalleydatabase.alley.AlleyCoilInit
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageKey
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import dev.zacsweers.metro.Inject
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.addPlatformFileSupport

@Inject
class AlleyEditCoilInit(
    private val alleyCoilInit: AlleyCoilInit,
) {

    context(builder: ComponentRegistry.Builder)
    fun addComponents() {
        alleyCoilInit.addComponents()
        builder.run {
            add(Mapper<ImageWithDimensions, PlatformImageKey> { data, _ ->
                data.coilImageModel as? PlatformImageKey
            })
            add(Mapper<PlatformImageKey, PlatformFile> { data, _ -> PlatformImageCache[data] })
            addPlatformFileSupport()
        }
    }
}
