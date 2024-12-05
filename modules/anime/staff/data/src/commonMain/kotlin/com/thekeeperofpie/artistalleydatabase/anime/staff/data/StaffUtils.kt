package com.thekeeperofpie.artistalleydatabase.anime.staff.data

import androidx.compose.runtime.Composable
import com.anilist.data.fragment.StaffNameLanguageFragment
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionStaff

object StaffUtils {

    @Composable
    fun StaffNameLanguageFragment.primaryName() = primaryName(LocalLanguageOptionStaff.current)

    fun StaffNameLanguageFragment.primaryName(languageOption: AniListLanguageOption) =
        when (languageOption) {
            AniListLanguageOption.DEFAULT -> userPreferred
            AniListLanguageOption.NATIVE -> native
            AniListLanguageOption.ENGLISH,
            AniListLanguageOption.ROMAJI,
            -> full
        }

    @Composable
    fun StaffNameLanguageFragment.subtitleName() = subtitleName(LocalLanguageOptionStaff.current)

    fun StaffNameLanguageFragment.subtitleName(languageOption: AniListLanguageOption) =
        when (languageOption) {
            AniListLanguageOption.DEFAULT -> native.takeIf { it != userPreferred } ?: full
            AniListLanguageOption.NATIVE -> full.takeIf { it != native } ?: userPreferred
            AniListLanguageOption.ENGLISH,
            AniListLanguageOption.ROMAJI,
            -> native.takeIf { it != full } ?: userPreferred
        }
}
