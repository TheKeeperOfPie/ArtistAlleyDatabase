package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.reflect.KClass

class AnimeMediaFilterController<T>(
    private val sortEnumClass: KClass<T>
) where T : AnimeMediaFilterController.Data.SortOption, T : Enum<*> {

    val sort = MutableStateFlow(sortEnumClass.java.enumConstants!!.first())
    val sortAscending = MutableStateFlow(false)

    private fun onSortChanged(option: T) = sort.update { option }

    private fun onSortAscendingChanged(ascending: Boolean) = sortAscending.update { ascending }

    fun data() = Data(
        defaultOptions = sortEnumClass.java.enumConstants!!.toList(),
        sort = { sort.collectAsState().value },
        onSortChanged = ::onSortChanged,
        sortAscending = { sortAscending.collectAsState().value },
        onSortAscendingChanged = ::onSortAscendingChanged,
    )

    class Data<SortOption>(
        val defaultOptions: List<SortOption>,
        val sort: @Composable () -> SortOption,
        val onSortChanged: (SortOption) -> Unit = {},
        val sortAscending: @Composable () -> Boolean = { false },
        val onSortAscendingChanged: (Boolean) -> Unit = {},
    ) {
        companion object {
            inline fun <reified T> forPreview(): Data<T>
                    where T : SortOption, T : Enum<*> {
                val enumConstants = T::class.java.enumConstants!!.toList()
                return Data(
                    defaultOptions = enumConstants,
                    sort = { enumConstants.first() }
                )
            }
        }

        interface SortOption {
            @get:StringRes
            val textRes: Int
        }
    }
}