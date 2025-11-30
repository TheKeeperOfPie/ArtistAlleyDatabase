package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import co.touchlab.stately.collections.ConcurrentMutableMap
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * [PlatformFile] doesn't support [kotlinx.serialization.Serializable], so this serves as a process
 * local cache which allows [PlatformFile] to be passed between screens. If the process dies, then
 * the images uploaded will be lost and the editor will have to delete and re-select the image.
 *
 * This avoids persisting/uploading the image until an entry using it is saved.
 */
object PlatformImageCache {

    private val cache = ConcurrentMutableMap<PlatformImageKey, PlatformFile>()

    operator fun get(key: PlatformImageKey) = cache[key]

    fun add(platformFile: PlatformFile): PlatformImageKey {
        val key = PlatformImageKey(Uuid.random())
        cache[key] = platformFile
        return key
    }
}

@JvmInline
@Serializable
value class PlatformImageKey(val value: Uuid)
