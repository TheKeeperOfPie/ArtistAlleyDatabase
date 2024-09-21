@file:Suppress("UnstableApiUsage")

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.jaredsburrows.license")
    id("com.google.android.gms.oss-licenses-plugin")
    id("org.jetbrains.kotlin.plugin.compose")
}

