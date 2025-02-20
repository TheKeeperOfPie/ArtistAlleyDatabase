package com.thekeeperofpie.artistalleydatabase.alley.data

import com.eygraber.uri.Uri
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

data class CatalogImage(
    val uri: Uri,
    val width: Int?,
    val height: Int?,
)

object CatalogImagePreviewProvider : PreviewParameterProvider<CatalogImage> {
    override val values = generateSequence<Pair<Int, CatalogImage?>>(0 to null) { (count, value) ->
        (count + 1) to CatalogImage(
            uri = Uri.parse("jar:file/composeResources/files/catalogs/A01/$count.webp"),
            width = 200,
            height = 100
        )
    }.filter { it.second != null }
        .map { it.second!! }
}
