@file:Suppress("UnstableApiUsage")

buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

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
            prefix("dagger") { library("com.google.dagger:hilt-compiler:2.47") }
        }

        create("kspProcessors") {
            library("androidx.room:room-compiler:2.6.0-alpha02")
            library("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
        }

        create("libs") {
            fun plugin(id: String) = plugin(id, id)
            plugin("com.apollographql.apollo3.external").version("4.0.0-alpha.2")
            plugin("com.autonomousapps.dependency-analysis").version("1.20.0")
            plugin("com.github.ben-manes.versions").version("0.47.0")
            plugin("com.google.dagger.hilt.android").version("2.47")
            plugin("com.jaredsburrows.license").version("0.9.3")
            plugin("org.barfuin.gradle.taskinfo").version("2.1.0")
            plugin("org.jetbrains.kotlin.plugin.serialization").version("1.9.0")

            library("androidx.activity:activity-compose:1.8.0-alpha06")
            library("androidx.compose.material3:material3:1.2.0-alpha03")
            library("androidx.compose.material:material-icons-core:1.6.0-alpha01")
            library("androidx.compose.material:material-icons-extended:1.6.0-alpha01")
            library("androidx.compose.material:material:1.6.0-alpha01")
            library("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha10")
            library("androidx.core:core-ktx:1.12.0-alpha05")
            library("androidx.hilt:hilt-navigation-compose:1.1.0-alpha01")
            library("androidx.hilt:hilt-work:1.0.0")
            library("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
            library("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
            library("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
            library("androidx.media3:media3-datasource-cronet:1.1.0-beta01")
            library("androidx.media3:media3-exoplayer:1.1.0-beta01")
            library("androidx.media3:media3-exoplayer-dash:1.1.0-beta01")
            library("androidx.media3:media3-exoplayer-hls:1.1.0-beta01")
            library("androidx.media3:media3-exoplayer-rtsp:1.1.0-beta01")
            library("androidx.media3:media3-ui:1.1.0-beta01")

            // Upgrading past 2.6.0 will break shared element transitions
            library("androidx.navigation:navigation-compose:2.6.0")
            library("androidx.paging:paging-compose:3.2.0-rc01")
            library("androidx.paging:paging-runtime-ktx:3.2.0-rc01")
            library("androidx.palette:palette-ktx:1.0.0")
            library("androidx.room:room-compiler:2.6.0-alpha02")
            library("androidx.room:room-ktx:2.6.0-alpha02")
            library("androidx.room:room-paging:2.6.0-alpha02")
            library("androidx.room:room-runtime:2.6.0-alpha02")
            library("androidx.room:room-testing:2.6.0-alpha02")
            library("androidx.test.ext:junit:1.2.0-alpha01", alias = "androidx.junit.test")
            library("androidx.test:runner:1.6.0-alpha03", alias = "androidx.test.runner")
            library("androidx.work:work-runtime-ktx:2.9.0-alpha01")
            library("androidx.work:work-runtime:2.9.0-alpha01")
            library("com.apollographql.apollo3:apollo-runtime:4.0.0-alpha.2")
            library("com.apollographql.apollo3:apollo-normalized-cache:4.0.0-alpha.2")
            library("com.apollographql.apollo3:apollo-normalized-cache-sqlite:4.0.0-alpha.2")
            library("com.google.accompanist:accompanist-flowlayout:0.31.5-beta")
            library("com.google.accompanist:accompanist-placeholder-material:0.31.5-beta")
            library("com.google.accompanist:accompanist-navigation-animation:0.31.5-beta")
            library("com.google.accompanist:accompanist-pager-indicators:0.31.5-beta")
            library("com.google.android.gms:play-services-ads:22.2.0")
            library("com.google.android.gms:play-services-oss-licenses:17.0.1")
            library("com.google.android.ump:user-messaging-platform:2.1.0")
            library("com.google.dagger:hilt-android:2.47")
            library("com.google.net.cronet:cronet-okhttp:0.1.0")
            library("com.google.truth:truth:1.1.5")
            library("com.linkedin.dexmaker:dexmaker-mockito-inline:2.28.3")
            library("com.neovisionaries:nv-i18n:1.29")
            library(
                "com.pierfrancescosoffritti.androidyoutubeplayer:core:12.0.0",
                alias = "androidyoutubeplayer"
            )
            library("com.rometools:rome:2.1.0")
            library("com.squareup.leakcanary:leakcanary-android:2.12")
            library("com.squareup.leakcanary:leakcanary-android-release:2.12")
            library("com.squareup.leakcanary:leakcanary-object-watcher-android:2.12")
            library("com.squareup.moshi:moshi-kotlin:1.15.0")
            library("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
            library("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11", prefix = "okhttp3")
            library("de.charlex.compose:html-text:1.4.1")
            library("io.coil-kt:coil-compose:2.4.0")
            library("io.github.hoc081098:FlowExt:0.6.1")
            library("io.github.java-diff-utils:java-diff-utils:4.12")
            library("it.skrape:skrapeit:1.3.0-alpha.1")
            library("junit:junit:4.13.2")
            library("org.apache.commons:commons-compress:1.23.0")
            library("org.apache.commons:commons-csv:1.10.0")
            library("org.awaitility:awaitility:4.2.0")
            library("org.chromium.net:cronet-embedded:113.5672.61")
            library("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")
            library("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
            library("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.2")
            library("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
            library("org.junit.jupiter:junit-jupiter-api:5.10.0-RC1")
            library("org.junit.jupiter:junit-jupiter-engine:5.10.0-RC1")
            library("org.mockito.kotlin:mockito-kotlin:5.0.0")
            library("org.mockito:mockito-android:5.4.0")
            library("org.mockito:mockito-core:5.4.0")

            prefix("androidx") {
                library("androidx.browser:browser:1.6.0-beta01")
                library("androidx.security:security-crypto:1.1.0-alpha06")
            }

            prefix("compose") {
                library("androidx.compose.animation:animation:1.6.0-alpha01")
                library("androidx.compose.ui:ui-test-manifest:1.6.0-alpha01")
                library("androidx.compose.ui:ui-tooling-preview:1.6.0-alpha01")
                library("androidx.compose.ui:ui-tooling:1.6.0-alpha01")
                library("androidx.compose.ui:ui:1.6.0-alpha01")
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
    ":modules:alley",
    ":modules:alley-app",
    ":modules:android-utils",
    ":modules:anilist",
    ":modules:anime",
    ":modules:animethemes",
    ":modules:art",
    ":modules:browse",
    ":modules:compose-utils",
    ":modules:cds",
    ":modules:data",
    ":modules:dependencies",
    ":modules:entry",
    ":modules:monetization",
    ":modules:musical-artists",
    ":modules:network-utils",
    ":modules:play",
    ":modules:settings",
    ":modules:test-utils",
    ":modules:vgmdb",
)

// Utilities

data class Prefix(val prefix: String, val builder: VersionCatalogBuilder)

// gav is group:artifact:version but shortened for readability
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun VersionCatalogBuilder.library(
    gav: String,
    alias: String? = null,
    prefix: String? = null,
) {
    val realPrefix = if (prefix == null) "" else "$prefix."
    val artifact = gav.substringAfter(":").substringBefore(":")
        .replaceFirstChar { it.lowercaseChar() }
    library(alias ?: (realPrefix + artifact), gav)
}

fun VersionCatalogBuilder.prefix(prefix: String, block: Prefix.() -> Unit) =
    Prefix(prefix, this).block()

fun Prefix.library(gav: String) = builder.library(gav = gav, prefix = prefix)
