package com.thekeeperofpie.artistalleydatabase.json

import com.squareup.moshi.Moshi
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.utils.Converters
import kotlinx.serialization.json.JsonElement
import java.math.BigDecimal
import java.util.Date

class AppMoshi {

    private val moshi by lazy {
        Moshi.Builder()
            .add(Date::class.java, Converters.DateConverter)
            .add(BigDecimal::class.java, Converters.BigDecimalConverter)
            .add(JsonElement::class.java, Converters.JsonElementConverter)
            .build()
    }

    val artEntryAdapter by lazy { moshi.adapter(ArtEntry::class.java)!! }
    val jsonElementAdapter by lazy { moshi.adapter(JsonElement::class.java)!! }
}