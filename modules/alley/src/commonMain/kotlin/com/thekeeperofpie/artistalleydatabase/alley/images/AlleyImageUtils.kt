package com.thekeeperofpie.artistalleydatabase.alley.images

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkCategory
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.category
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link

object AlleyImageUtils {

    fun getArtistImages(
        year: DataYear,
        images: List<DatabaseImage>,
    ) = images.mapNotNull {
        try {
            val path = "files/images/${year.folderName}/catalogs/${it.name}"
            CatalogImage(
                uri = Uri.parse(Res.getUri(path)),
                width = it.width,
                height = it.height,
            )
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    const val EMBED_MIN_DIMENSION = 300

    fun getEmbedImagesMap(embeds: Map<String, DatabaseImage>) = embeds
        .filter {
            val width = it.value.width
            val height = it.value.height
            width != null && height != null &&
                    width > EMBED_MIN_DIMENSION && height > EMBED_MIN_DIMENSION
        }
        .mapNotNull {
            try {
                it.key to Triple(
                    "files/embeds/${it.value.name}",
                    it.value.width,
                    it.value.height,
                )
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }
        .map { LinkModel.parse(it.first) to it.second }
        .sortedWith(embedComparator)
        .map { it.second }

    fun getEmbedImages(embeds: Map<String, DatabaseImage>) =
        getEmbedImagesMap(embeds)
            .map { (path, width, height) ->
                CatalogImage(
                    uri = Uri.parse(Res.getUri(path)),
                    width = width,
                    height = height,
                )
            }

    private val embedOrder = listOf(
        LinkCategory.PORTFOLIOS,
        LinkCategory.SOCIALS,
        LinkCategory.STORES,
        LinkCategory.COMMISSIONS,
        LinkCategory.SUPPORT,
        LinkCategory.OTHER,
    )
    private val embedComparator =
        compareBy<Pair<LinkModel, Triple<String, *, *>>>(
            {
                // Sort Linktree last because the embed is not very useful
                when (it.first.type) {
                    Link.Type.LINKTREE -> 1
                    else -> 0
                }
            },
            { embedOrder.indexOf(it.first.type.category) },
            { it.first.link },
        )

    fun getArtistImagesWithEmbedFallback(
        year: DataYear,
        images: List<DatabaseImage>,
        embeds: Map<String, DatabaseImage>,
    ) = getArtistImages(year, images)
        .ifEmpty { getEmbedImages(embeds) }

    fun getProfileImageWithPath(embeds: Map<String, DatabaseImage>) =
        embeds.asSequence()
            .filter {
                val width = it.value.width
                val height = it.value.height
                width != null && height != null &&
                        width < EMBED_MIN_DIMENSION && height < EMBED_MIN_DIMENSION
            }
            .firstOrNull { LinkModel.parse(it.key).type.category == LinkCategory.SOCIALS }
            ?.let {
                "files/embeds/${it.value.name}" to it.value
            }

    fun getProfileImage(embeds: Map<String, DatabaseImage>) =
        getProfileImageWithPath(embeds)
            ?.let { (path, image) ->
                CatalogImage(
                    uri = Uri.parse(Res.getUri(path)),
                    width = image.width,
                    height = image.height,
                    color = image.color,
                )
            }

    fun getRallyImages(
        year: DataYear,
        images: List<DatabaseImage>,
    ) = images.mapNotNull {
        try {
            val path = "files/images/${year.folderName}/rallies/${it.name}"
            CatalogImage(
                uri = Uri.parse(Res.getUri(path)),
                width = it.width,
                height = it.height,
            )
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    suspend fun artistImageExists(artistEntryDao: ArtistEntryDao, path: String): Boolean {
        val parts = path.substringAfter("generated.resources/files/images/").split("/")
        if (parts.size < 3) return false
        val yearFolderName = parts[0]
        val name = parts[2]
        val imageName = parts[3]

        val dataYear = DataYear.entries.find { it.folderName == yearFolderName } ?: return false

        return artistEntryDao.getImagesById(dataYear, name.substringAfter("-").trim())
            ?.any { it.name.contains(imageName) }
            ?: false
    }
}
