package com.thekeeperofpie.artistalleydatabase.alley.functions
import app.cash.sqldelight.ColumnAdapter
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import kotlinx.serialization.json.Json

object ColumnAdapters {

    val listStringAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<String>>(databaseValue)

        override fun encode(value: List<String>) = Json.encodeToString(value)
    }

    val listCatalogImageAdapter = object : ColumnAdapter<List<CatalogImage>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<CatalogImage>>(databaseValue)

        override fun encode(value: List<CatalogImage>) = Json.encodeToString(value)
    }
}
