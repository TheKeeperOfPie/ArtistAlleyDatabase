package com.thekeeperofpie.artistalleydatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryFts
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.util.Date

@Database(entities = [ArtEntry::class, ArtEntryFts::class], exportSchema = false, version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artEntryDao(): ArtEntryDao
}

object Converters {

    @TypeConverter
    fun serializeStringList(value: List<String>?) = Json.Default.encodeToString(value)

    @TypeConverter
    fun deserializeStringList(value: String?) =
        value?.let<String, List<String>>(Json.Default::decodeFromString).orEmpty()

    @TypeConverter
    fun serializeDate(value: Date?) = value?.time

    @TypeConverter
    fun deserializeDate(value: Long?) = value?.let { Date(it) }

    @TypeConverter
    fun serializeBigDecimal(value: BigDecimal?) = value?.toPlainString()

    @TypeConverter
    fun deserializeBigDecimal(value: String?) = value?.let(::BigDecimal)
}