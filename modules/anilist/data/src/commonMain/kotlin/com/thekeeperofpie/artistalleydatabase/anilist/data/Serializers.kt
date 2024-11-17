package com.thekeeperofpie.artistalleydatabase.anilist.data

import com.anilist.data.fragment.AniListDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object AniListDateSerializer : KSerializer<AniListDate> {

    override val descriptor = AniListDateImpl.serializer().descriptor

    override fun deserialize(decoder: Decoder): AniListDate =
        AniListDateImpl.serializer().deserialize(decoder)

    override fun serialize(encoder: Encoder, value: AniListDate) =
        AniListDateImpl.serializer()
            .serialize(encoder, AniListDateImpl(value.year, value.month, value.day))
}

@Serializable
private data class AniListDateImpl(
    override val year: Int?,
    override val month: Int?,
    override val day: Int?,
) : AniListDate
