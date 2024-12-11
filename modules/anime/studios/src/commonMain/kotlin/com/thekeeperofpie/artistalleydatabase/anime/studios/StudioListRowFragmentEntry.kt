package com.thekeeperofpie.artistalleydatabase.anime.studios

import com.anilist.data.fragment.StudioListRowFragment
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.StudioEntryProvider

data class StudioListRowFragmentEntry<MediaEntry>(
    override val studio: StudioListRowFragment,
    override val media: List<MediaEntry>,
) : StudioListRow.Entry<MediaEntry> {
    companion object {
        fun <MediaEntry> provider(): StudioEntryProvider<StudioListRowFragment, StudioListRowFragmentEntry<MediaEntry>, MediaEntry> {
            return object :
                StudioEntryProvider<StudioListRowFragment, StudioListRowFragmentEntry<MediaEntry>, MediaEntry> {
                override fun studioEntry(
                    studio: StudioListRowFragment,
                    media: List<MediaEntry>,
                ) = StudioListRowFragmentEntry(studio, media)

                override fun copyStudioEntry(
                    entry: StudioListRowFragmentEntry<MediaEntry>,
                    media: List<MediaEntry>,
                ) = entry.copy(media = media)

                override fun media(entry: StudioListRowFragmentEntry<MediaEntry>) = entry.media

                override fun id(entry: StudioListRowFragmentEntry<MediaEntry>) =
                    entry.studio.id.toString()
            }
        }
    }
}
