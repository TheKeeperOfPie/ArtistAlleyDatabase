package com.thekeeperofpie.artistalleydatabase.alley.data

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

fun ArtistEntryAnimeExpo2026.toArtistDatabaseEntry() =
    ArtistDatabaseEntry.Impl(
        year = DataYear.ANIME_EXPO_2026,
        id = id,
        status = status,
        booth = booth,
        name = name,
        summary = summary,
        socialLinks = socialLinks,
        storeLinks = storeLinks,
        portfolioLinks = portfolioLinks,
        catalogLinks = catalogLinks,
        driveLink = null,
        notes = notes,
        commissions = commissions,
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        images = images,
        counter = counter,
        editorNotes = editorNotes,
        lastEditor = lastEditor,
        lastEditTime = lastEditTime,
        verifiedArtist = verifiedArtist,
    )

fun ArtistDatabaseEntry.Impl.toArtistEntryAnimeExpo2026() =
    ArtistEntryAnimeExpo2026(
        id = id,
        status = status,
        booth = booth,
        name = name,
        summary = summary,
        socialLinks = socialLinks,
        storeLinks = storeLinks,
        portfolioLinks = portfolioLinks,
        catalogLinks = catalogLinks,
        linkFlags = 0,
        linkFlags2 = 0,
        notes = notes,
        commissions = commissions,
        commissionFlags = 0,
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        images = images,
        counter = counter,
        editorNotes = editorNotes,
        lastEditor = lastEditor,
        lastEditTime = lastEditTime,
        verifiedArtist = verifiedArtist,
    )
