package com.thekeeperofpie.artistalleydatabase.android_utils.kotlin

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList

val EmptyImmutableList = persistentListOf<Any>()
val EmptyImmutableMap = persistentMapOf<Any, Any>()

@Suppress("UNCHECKED_CAST")
fun <T> emptyImmutableList(): ImmutableList<T> = EmptyImmutableList as ImmutableList<T>

@Suppress("UNCHECKED_CAST")
fun <K, V> emptyImmutableMap(): ImmutableMap<K, V> = EmptyImmutableMap as ImmutableMap<K, V>

@Suppress("UNCHECKED_CAST")
fun <T> ImmutableList<T>?.orEmpty(): ImmutableList<T> =
    this ?: (EmptyImmutableList as ImmutableList<T>)

@Suppress("UNCHECKED_CAST")
fun <K, V> ImmutableMap<K, V>?.orEmpty(): ImmutableMap<K, V> =
    this ?: (EmptyImmutableMap as ImmutableMap<K, V>)

fun <T> persistentListOfNotNull(vararg elements: T?): ImmutableList<T> =
    listOfNotNull(*elements).toPersistentList()
