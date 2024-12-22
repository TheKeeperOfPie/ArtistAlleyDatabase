package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings

interface AppSettings : AniListSettings, AnimeSettings, CropSettings, MonetizationSettings,
    NewsSettings, NetworkSettings
