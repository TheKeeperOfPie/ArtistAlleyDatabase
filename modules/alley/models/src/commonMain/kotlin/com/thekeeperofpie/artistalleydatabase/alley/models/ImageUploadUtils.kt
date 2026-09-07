package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlin.uuid.Uuid

expect object ImageUploadUtils {
    fun maxArtistUploadCount(isDebug: Boolean): Int
    fun maxStampRallyUploadCount(isDebug: Boolean): Int
}

@Suppress("UnusedReceiverParameter")
fun ImageUploadUtils.makeArtistKey(
    dataYear: DataYear,
    artistId: Uuid,
    imageId: Uuid,
    extension: String,
) = "${dataYear.serializedName}/artist/$artistId/$imageId.$extension"

@Suppress("UnusedReceiverParameter")
fun ImageUploadUtils.makeStampRallyKey(
    dataYear: DataYear,
    stampRallyId: String,
    imageId: Uuid,
    extension: String,
) = "${dataYear.serializedName}/rally/$stampRallyId/$imageId.$extension"
