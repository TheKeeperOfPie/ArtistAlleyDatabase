package com.thekeeperofpie.artistalleydatabase.inject

import dev.zacsweers.metro.Qualifier
import dev.zacsweers.metro.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ActivityScope

@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
)
annotation class Named(val value: String)
