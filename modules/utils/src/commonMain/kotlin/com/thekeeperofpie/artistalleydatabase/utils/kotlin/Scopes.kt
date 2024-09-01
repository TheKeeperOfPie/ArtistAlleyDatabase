package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Scope

typealias ApplicationScope = CoroutineScope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class SingletonScope
