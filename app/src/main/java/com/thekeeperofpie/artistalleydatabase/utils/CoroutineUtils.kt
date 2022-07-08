package com.thekeeperofpie.artistalleydatabase.utils

import kotlinx.coroutines.flow.Flow

fun <T> Flow<T>.nullable() = this as Flow<T?>