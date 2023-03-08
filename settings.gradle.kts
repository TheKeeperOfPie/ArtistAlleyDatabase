@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        maven("https://androidx.dev/storage/compose-compiler/repository/")
        google()
        mavenCentral()
        flatDir { dirs = setOf(rootProject.projectDir.resolve("libs")) }
    }

    versionCatalogs {
        create("kaptProcessors") {
            prefix("androidx") { library("androidx.hilt:hilt-compiler:1.0.0") }
            prefix("dagger") { library("com.google.dagger:hilt-compiler:2.45") }
        }

        create("kspProcessors") {
            library("androidx.room:room-compiler:2.5.0")
            library("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
        }

        create("libs") {
            fun plugin(id: String) = plugin(id, id)
            plugin("com.apollographql.apollo3").version("3.7.4")
            plugin("com.autonomousapps.dependency-analysis").version("1.19.0")
            plugin("com.github.ben-manes.versions").version("0.46.0")
            plugin("com.google.dagger.hilt.android").version("2.45")
            plugin("com.jaredsburrows.license").version("0.9.0")
            plugin("de.mannodermaus.android-junit5").version("1.8.2.1")
            plugin("org.barfuin.gradle.taskinfo").version("2.1.0")
            plugin("org.jetbrains.kotlin.plugin.serialization").version("1.8.20-Beta")

            library("androidx.activity:activity-compose:1.8.0-alpha01")
            library("androidx.compose.material3:material3:1.1.0-alpha07")
            library("androidx.compose.material:material-icons-core:1.4.0-beta02")
            library("androidx.compose.material:material-icons-extended:1.4.0-beta02")
            library("androidx.compose.material:material:1.4.0-beta02")
            library("androidx.core:core-ktx:1.9.0")
            library("androidx.hilt:hilt-navigation-compose:1.1.0-alpha01")
            library("androidx.hilt:hilt-work:1.0.0")
            library("androidx.lifecycle:lifecycle-livedata-ktx:2.6.0-rc01")
            library("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0-rc01")
            library("androidx.navigation:navigation-compose:2.6.0-alpha06")
            library("androidx.paging:paging-compose:1.0.0-alpha18")
            library("androidx.paging:paging-runtime:3.2.0-alpha04")
            library("androidx.room:room-compiler:2.5.0")
            library("androidx.room:room-ktx:2.5.0")
            library("androidx.room:room-paging:2.5.0")
            library("androidx.room:room-runtime:2.5.0")
            library("androidx.room:room-testing:2.5.0")
            library("androidx.test.ext:junit:1.1.5", alias = "androidx.junit.test")
            library("androidx.test:runner:1.5.3-alpha01", alias = "androidx.test.runner")
            library("androidx.work:work-runtime-ktx:2.8.0")
            library("androidx.work:work-runtime:2.8.0")
            library("com.apollographql.apollo3:apollo-runtime:3.7.4")
            library("com.google.accompanist:accompanist-pager-indicators:0.29.1-alpha")
            library("com.google.accompanist:accompanist-pager:0.29.1-alpha")
            library("com.google.dagger:hilt-android:2.45")
            library("com.google.truth:truth:1.1.3")
            library("com.squareup.moshi:moshi-kotlin:1.14.0")
            library("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
            library("io.coil-kt:coil-compose:2.2.1")
            library("io.github.hoc081098:FlowExt:0.5.0")
            library("it.skrape:skrapeit:1.3.0-alpha.1")
            library("junit:junit:4.13.2")
            library("org.apache.commons:commons-compress:1.22")
            library("org.awaitility:awaitility:4.2.0")
            library("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
            library("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            library("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            library("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
            library("org.junit.jupiter:junit-jupiter-api:5.9.2")
            library("org.junit.jupiter:junit-jupiter-engine:5.9.2")
            library("org.mockito.kotlin:mockito-kotlin:4.1.0")
            library("org.mockito:mockito-android:5.1.1")
            library("org.mockito:mockito-core:5.1.1")

            prefix("compose") {
                library("androidx.compose.ui:ui-test-manifest:1.4.0-beta02")
                library("androidx.compose.ui:ui-tooling-preview:1.4.0-beta02")
                library("androidx.compose.ui:ui-tooling:1.4.0-beta02")
                library("androidx.compose.ui:ui:1.4.0-beta02")
            }
            prefix("junit5") {
                library("de.mannodermaus.junit5:android-test-core:1.3.0")
                library("de.mannodermaus.junit5:android-test-runner:1.3.0")
            }
        }
    }
}

rootProject.name = "Artist Alley Database"
include(
    ":app",
    ":modules:android-utils",
    ":modules:anilist",
    ":modules:art",
    ":modules:browse",
    ":modules:compose-utils",
    ":modules:cds",
    ":modules:data",
    ":modules:dependencies",
    ":modules:entry",
    ":modules:musical-artists",
    ":modules:settings",
    ":modules:test-utils",
    ":modules:vgmdb",
    ":modules:web-infra",
)

// Utilities

data class Prefix(val prefix: String, val builder: VersionCatalogBuilder)

// gav is group:artifact:version but shortened for readability
fun VersionCatalogBuilder.library(
    gav: String,
    alias: String? = null,
    prefix: String? = null
) {
    val realPrefix = if (prefix == null) "" else "$prefix."
    val artifact = gav.substringAfter(":").substringBefore(":")
        .replaceFirstChar { it.lowercaseChar() }
    library(alias ?: (realPrefix + artifact), gav)
}

fun VersionCatalogBuilder.prefix(prefix: String, block: Prefix.() -> Unit) =
    Prefix(prefix, this).block()

fun Prefix.library(gav: String) = builder.library(gav = gav, prefix = prefix)