package com.thekeeperofpie.artistalleydatabase.alley.merch

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.MerchEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.random
import kotlin.random.Random
import kotlin.uuid.Uuid

object MerchEntryProvider : PreviewParameterProvider<MerchEntry> {
    override val values = sequence {
        val merch = listOf(
            "Bags" to listOf("Functional"),
            "Prints" to listOf("Art"),
            "Shirts" to listOf("Apparel")
        )
        val random = Random(39)
        merch.forEach { (name, categories) ->
            yield(
                MerchEntry(
                    name = name,
                    uuid = Uuid.random(random).toString(),
                    notes = null,
                    categories = categories.joinToString(","),
                    yearFlags = 0,
                )
            )
        }
    }
}
