package com.thekeeperofpie.artistalleydatabase.android_utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.squareup.moshi.Moshi
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonElement
import java.util.Date

class AppMoshi {

    val moshi by lazy {
        Moshi.Builder()
            .add(Date::class.java, Converters.DateConverter)
            .add(LocalDate::class.java, Converters.LocalDateConverter)
            .add(Instant::class.java, Converters.InstantConverter)
            .add(BigDecimal::class.java, Converters.BigDecimalConverter)
            .add(JsonElement::class.java, Converters.JsonElementConverter)
            .build()!!
    }

    val jsonElementAdapter by lazy { moshi.adapter(JsonElement::class.java).indent("    ")!! }
}
