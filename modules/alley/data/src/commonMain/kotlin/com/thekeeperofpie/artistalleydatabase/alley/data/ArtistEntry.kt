package com.thekeeperofpie.artistalleydatabase.alley.data

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlin.uuid.Uuid

fun ArtistEntry.toArtistDatabaseEntry() =
    ArtistDatabaseEntry.Impl(
        year = dataYear,
        id = id.toString(),
        status = status ?: ArtistStatus.UNKNOWN,
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
        _images = images,
        fallbackImageYear = fallbackImageYear,
        profileImage = profileImage,
        tempImages = tempImages.orEmpty(),
        embeds = embeds.orEmpty(),
        editorNotes = editorNotes,
        lastEditor = lastEditor,
        lastEditTime = lastEditTime,
        verifiedArtist = verifiedArtist,
        newArtist = newArtist,
    )

fun ArtistDatabaseEntry.Impl.toArtistEntry(dataYear: DataYear) =
    ArtistEntry(
        id = Uuid.parse(id),
        dataYear = dataYear,
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
        fallbackImageYear = fallbackImageYear,
        tempImages = tempImages,
        profileImage = profileImage,
        embeds = embeds,
        editorNotes = editorNotes,
        lastEditor = lastEditor,
        lastEditTime = lastEditTime,
        verifiedArtist = verifiedArtist,
        newArtist = newArtist,
    )
