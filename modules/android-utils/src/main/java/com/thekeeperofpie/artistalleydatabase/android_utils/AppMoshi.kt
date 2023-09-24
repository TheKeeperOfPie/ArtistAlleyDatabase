package com.thekeeperofpie.artistalleydatabase.android_utils

import com.squareup.moshi.Moshi
import kotlinx.serialization.json.JsonElement
import java.math.BigDecimal
import java.util.Date

class AppMoshi {

    val moshi by lazy {
        Moshi.Builder()
            .add(Date::class.java, Converters.DateConverter)
            .add(BigDecimal::class.java, Converters.BigDecimalConverter)
            .add(JsonElement::class.java, Converters.JsonElementConverter)
            .build()!!
    }

    val jsonElementAdapter by lazy { moshi.adapter(JsonElement::class.java).indent("    ")!! }
}
