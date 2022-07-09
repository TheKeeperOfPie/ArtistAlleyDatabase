package com.thekeeperofpie.artistalleydatabase.json

import com.squareup.moshi.Moshi
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.utils.Converters
import java.math.BigDecimal
import java.util.Date

class AppMoshi {

    private val moshi by lazy {
        Moshi.Builder()
            .add(Date::class.java, Converters.DateConverter)
            .add(BigDecimal::class.java, Converters.BigDecimalConverter)
            .build()
    }

    val artEntryAdapter by lazy { moshi.adapter(ArtEntry::class.java)!! }
}