package com.thekeeperofpie.artistalleydatabase.anime.utils

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.CheckResult
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.apollographql.apollo3.api.Optional
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

inline fun <T : Any> Flow<PagingData<T>>.enforceUniqueIds(
    crossinline id: suspend (value: T) -> String?,
) = map {
    // AniList can return duplicates across pages, manually enforce uniqueness
    val seenIds = mutableSetOf<String>()
    it.filterOnIO {
        @Suppress("NAME_SHADOWING") val id = id(it)
        if (id == null) false else seenIds.add(id)
    }
}

inline fun <T : Any> Flow<PagingData<T>>.enforceUniqueIntIds(
    crossinline id: suspend (value: T) -> Int?,
) = map {
    // AniList can return duplicates across pages, manually enforce uniqueness
    val seenIds = mutableSetOf<Int>()
    it.filterOnIO {
        @Suppress("NAME_SHADOWING") val id = id(it)
        if (id == null) false else seenIds.add(id)
    }
}

fun <Input : Any, Output : Any> PagingData<Input>.mapNotNull(
    transform: suspend (Input) -> Output?,
): PagingData<Output> = mapOnIO { Optional.presentIfNotNull(transform(it)) }
    .filterOnIO { it is Optional.Present }
    .mapOnIO { it.getOrThrow() }

data class PagingPlaceholderKey(private val index: Int) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingPlaceholderKey> =
            object : Parcelable.Creator<PagingPlaceholderKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingPlaceholderKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingPlaceholderKey?>(size)
            }
    }
}

@CheckResult
@JvmSynthetic
fun <T : Any> PagingData<T>.filterOnIO(predicate: suspend (T) -> Boolean) = filter {
    withContext(CustomDispatchers.IO) { predicate(it) }
}

@CheckResult
fun <T : Any, R : Any> PagingData<T>.mapOnIO(transform: suspend (T) -> R) = map {
    withContext(CustomDispatchers.IO) {
        transform(it)
    }
}

object PagingPlaceholderContentType
