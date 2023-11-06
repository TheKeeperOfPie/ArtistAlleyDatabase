@file:Suppress("UnstableApiUsage")

import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.initialization.resolve.DependencyResolutionManagement
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.kotlin.dsl.maven

object Versions {
    const val accompanist = "0.33.2-alpha"
    const val apollo = "4.0.0-alpha.3"
    // 1.6.0-alpha07 is last version that doesn't IndexOutOfBoundsException
    const val compose = "1.6.0-alpha07"
    const val dagger = "2.48.1"
    const val hilt = "1.1.0-rc01"

    object junit {
        const val jupiter = "5.10.0"
    }

    object kotlin {
        const val coroutines = "1.7.3"
    }

    const val ktor = "2.3.4"
    const val lifecycle = "2.7.0-alpha03"
    const val room = "2.6.0"
    const val markwon = "4.6.2"
    const val media3 = "1.2.0-beta01"
    const val mockito = "5.6.0"
    const val paging = "3.3.0-alpha02"
    const val work = "2.9.0-rc01"
}

extra["versions"] = fun(dependencyResolutionManagement: DependencyResolutionManagement) =
    dependencyResolutionManagement.apply {
        repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
        repositories {
            maven("https://oss.sonatype.org/content/repositories/snapshots")
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
            maven("https://androidx.dev/storage/compose-compiler/repository/")
            google()
            mavenCentral()
            flatDir { dirs = setOf(rootProject.projectDir.resolve("/libs")) }
        }
        versionCatalogs {
            create("kspProcessors") {
                prefix("androidx") {
                    withVersion(Versions.hilt) {
                        library("androidx.hilt:hilt-compiler")
                    }
                }

                withVersion(Versions.dagger) {
                    library("com.google.dagger:hilt-compiler")
                    library("com.google.dagger:hilt-android-compiler")
                }

                withVersion(Versions.room) {
                    library("androidx.room:room-compiler")
                }
                library("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
            }

            create("libs") {
                fun plugin(id: String) = plugin(id, id)
                plugin("com.apollographql.apollo3.external").version(Versions.apollo)
                plugin("com.autonomousapps.dependency-analysis").version("1.25.0")
                plugin("com.github.ben-manes.versions").version("0.49.0")
                plugin("com.google.dagger.hilt.android").version(Versions.dagger)
                plugin("com.jaredsburrows.license").version("0.9.3")
                plugin("com.netflix.dgs.codegen").version("6.0.3")
                plugin("de.mannodermaus.android-junit5").version("1.9.3.0")
                plugin("io.ktor.plugin").version(Versions.ktor)
                plugin("org.barfuin.gradle.taskinfo").version("2.1.0")
                plugin("org.jetbrains.kotlin.plugin.serialization").version("1.9.20-RC")

                library("androidx.activity:activity-compose:1.8.0-alpha06")
                // 1.2.0-alpha09 is last version that doesn't IndexOutOfBoundsException
                library("androidx.compose.material3:material3:1.2.0-alpha09")
                library("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha13")
                library("androidx.core:core-ktx:1.13.0-alpha01")

                withVersion(Versions.hilt) {
                    library("androidx.hilt:hilt-navigation-compose")
                    library("androidx.hilt:hilt-work")
                }

                withVersion(Versions.lifecycle) {
                    library("androidx.lifecycle:lifecycle-livedata-ktx")
                    library("androidx.lifecycle:lifecycle-viewmodel-compose")
                    library("androidx.lifecycle:lifecycle-viewmodel-ktx")
                    library("androidx.lifecycle:lifecycle-runtime-ktx")
                }

                withVersion(Versions.media3) {
                    library("androidx.media3:media3-datasource-okhttp")
                    library("androidx.media3:media3-exoplayer")
                    library("androidx.media3:media3-exoplayer-dash")
                    library("androidx.media3:media3-exoplayer-hls")
                    library("androidx.media3:media3-exoplayer-rtsp")
                    library("androidx.media3:media3-ui")
                }

                // Upgrading past 2.6.0 will break shared element transitions
                library("androidx.navigation:navigation-compose:2.6.0")

                withVersion(Versions.paging) {
                    library("androidx.paging:paging-compose")
                    library("androidx.paging:paging-runtime-ktx")
                }

                library("androidx.palette:palette-ktx:1.0.0")

                withVersion(Versions.room) {
                    library("androidx.room:room-compiler")
                    library("androidx.room:room-ktx")
                    library("androidx.room:room-paging")
                    library("androidx.room:room-runtime")
                    library("androidx.room:room-testing")
                }

                library("androidx.test.ext:junit:1.2.0-alpha01", alias = "androidx.junit.test")
                library("androidx.test:runner:1.6.0-alpha04", alias = "androidx.test.runner")

                withVersion(Versions.work) {
                    library("androidx.work:work-runtime-ktx")
                    library("androidx.work:work-runtime")
                }

                library("com.android.billingclient:billing-ktx:6.0.1")
                library("com.android.tools.build:gradle:8.3.0-alpha10")

                withVersion(Versions.apollo) {
                    library("com.apollographql.apollo3:apollo-runtime")
                    library("com.apollographql.apollo3:apollo-normalized-cache")
                    library("com.apollographql.apollo3:apollo-normalized-cache-sqlite")
                }

                library("com.fasterxml.jackson.core:jackson-databind:2.16.0-rc1")

                withVersion(Versions.accompanist) {
                    library("com.google.accompanist:accompanist-flowlayout")
                    library("com.google.accompanist:accompanist-navigation-animation")
                    library("com.google.accompanist:accompanist-pager-indicators")
                }

                library("com.google.android.gms:oss-licenses-plugin:0.10.6")
                library("com.google.android.gms:play-services-ads:22.4.0")
                library("com.google.android.gms:play-services-oss-licenses:17.0.1")
                library("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
                library("com.google.android.play:app-update:2.1.0")
                library("com.google.android.play:app-update-ktx:2.1.0")
                library("com.google.android.ump:user-messaging-platform:2.1.0")

                withVersion(Versions.dagger) {
                    library("com.google.dagger:hilt-android")
                    library("com.google.dagger:hilt-android-testing")
                }

                library("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.9.20-RC-1.0.13")
                library("com.google.net.cronet:cronet-okhttp:0.1.0")
                library("com.google.truth:truth:1.1.5")
                library("com.linkedin.dexmaker:dexmaker-mockito-inline-extended:2.28.3")
                library("com.neovisionaries:nv-i18n:1.29")
                library("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:7.6.0")
                library(
                    "com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0",
                    alias = "androidyoutubeplayer"
                )
                library("com.rometools:rome:2.1.0")
                library("com.squareup:javapoet:1.13.0")
                library("com.squareup.leakcanary:leakcanary-android:2.12")
                library("com.squareup.leakcanary:leakcanary-android-release:2.12")
                library("com.squareup.leakcanary:leakcanary-object-watcher-android:2.12")
                library("com.squareup.moshi:moshi-kotlin:1.15.0")
                library("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
                library(
                    "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11",
                    prefix = "okhttp3"
                )
                library("com.unity3d.ads:unity-ads:4.9.1")
                library("de.charlex.compose:html-text:1.5.0")
                library("io.coil-kt:coil-compose:2.4.0")
                library("io.github.hoc081098:FlowExt:0.7.2")
                library("io.github.java-diff-utils:java-diff-utils:4.12")

                withVersion(Versions.ktor) {
                    library("io.ktor:ktor-server-core-jvm")
                    library("io.ktor:ktor-server-tests-jvm")
                }

                library("it.skrape:skrapeit:1.3.0-alpha.1")
                library("junit:junit:4.13.2")
                library("org.apache.commons:commons-compress:1.24.0")
                library("org.apache.commons:commons-csv:1.10.0")
                library("org.awaitility:awaitility:4.2.0")
                library("org.chromium.net:cronet-embedded:113.5672.61")
                library("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:1.9.20-RC")
                library("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.6")

                withVersion(Versions.kotlin.coroutines) {
                    library("org.jetbrains.kotlinx:kotlinx-coroutines-android")
                    library("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                    library("org.jetbrains.kotlinx:kotlinx-coroutines-test")
                }

                library("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0-RC")

                withVersion(Versions.junit.jupiter) {
                    library("org.junit.jupiter:junit-jupiter-api")
                    library("org.junit.jupiter:junit-jupiter-engine")
                    library("org.junit.jupiter:junit-jupiter-params")
                }

                library("org.mockito.kotlin:mockito-kotlin:5.1.0")

                withVersion(Versions.mockito) {
                    library("org.mockito:mockito-android")
                    library("org.mockito:mockito-core")
                }

                library("systems.manifold:manifold-graphql-rt:2023.1.29")

                prefix("androidx") {
                    library("androidx.browser:browser:1.7.0-beta01")
                    library("androidx.security:security-crypto:1.1.0-alpha06")
                    library("androidx.tracing:tracing:1.3.0-alpha02")
                }

                prefix("compose") {
                    withVersion(Versions.compose) {
                        library("androidx.compose.animation:animation")
                        library("androidx.compose.material:material-icons-core")
                        library("androidx.compose.material:material-icons-extended")
                        library("androidx.compose.material:material")
                        library("androidx.compose.runtime:runtime")
                        library("androidx.compose.ui:ui-test-junit4")
                        library("androidx.compose.ui:ui-test-manifest")
                        library("androidx.compose.ui:ui-tooling-preview")
                        library("androidx.compose.ui:ui-tooling")
                        library("androidx.compose.ui:ui")
                    }
                    library("org.jetbrains.compose.runtime:runtime:1.5.10-rc02")
                }

                prefix("junit5") {
                    library("de.mannodermaus.junit5:android-test-core:1.4.0-SNAPSHOT")
                    library("de.mannodermaus.junit5:android-test-runner:1.4.0-SNAPSHOT")
                    library("de.mannodermaus.junit5:android-test-compose:1.0.0-SNAPSHOT")
                }

                prefix("markwon") {
                    withVersion(Versions.markwon) {
                        library("io.noties.markwon:core")
                        library("io.noties.markwon:editor")
                        library("io.noties.markwon:ext-strikethrough")
                        library("io.noties.markwon:ext-tables")
                        library("io.noties.markwon:html")
                        library("io.noties.markwon:image-coil")
                        library("io.noties.markwon:linkify")
                        library("io.noties.markwon:simple-ext")
                    }
                }
            }
        }
    }


// Utilities

data class Prefix(val prefix: String, val builder: VersionCatalogBuilder) {
    fun VersionCatalogBuilder.library(gav: String) = library(gav = gav, prefix = prefix)
}

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

data class Version(val version: String, val prefix: String? = null) {
    fun VersionCatalogBuilder.library(ga: String) = library(gav = "$ga:$version", prefix = prefix)
}

fun Prefix.withVersion(version: String, block: Version.() -> Unit) =
    Version(version, prefix).block()

fun withVersion(version: String, block: Version.() -> Unit) = Version(version).block()
