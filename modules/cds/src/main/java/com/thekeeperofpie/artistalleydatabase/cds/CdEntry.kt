package com.thekeeperofpie.artistalleydatabase.cds

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import com.thekeeperofpie.artistalleydatabase.utils.Converters
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

@Serializable
@JsonClass(generateAdapter = true)
@Entity(tableName = "cd_entries")
data class CdEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @Serializable(with = Converters.BigDecimalConverter::class)
    val price: BigDecimal? = null,
    @Serializable(with = Converters.DateConverter::class)
    val date: Date? = null,
    @Serializable(with = Converters.DateConverter::class)
    val lastEditTime: Date? = null,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val notes: String? = null,
    @Embedded val locks: Locks = Locks.EMPTY,
) {
    val imageWidthToHeightRatio by lazy {
        (imageHeight?.toFloat() ?: 1f) /
                (imageWidth ?: 1).coerceAtLeast(1)
    }

    @Serializable
    @JsonClass(generateAdapter = true)
    data class Locks(
        val artistsLocked: Boolean? = false,
        val sourceLocked: Boolean? = false,
        val seriesLocked: Boolean? = false,
        val charactersLocked: Boolean? = false,
        val tagsLocked: Boolean? = false,
        val notesLocked: Boolean? = false,
        val printSizeLocked: Boolean? = false,
    ) {
        companion object {
            val EMPTY = Locks()
        }
    }
}