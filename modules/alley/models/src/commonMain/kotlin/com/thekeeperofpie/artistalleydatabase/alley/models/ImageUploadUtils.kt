package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlin.uuid.Uuid

expect object ImageUploadUtils {
    val MAX_ARTIST_UPLOAD_COUNT: Int
    val MAX_STAMP_RALLY_UPLOAD_COUNT: Int
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
