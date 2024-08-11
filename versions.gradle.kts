@file:Suppress("UnstableApiUsage")

import Versions_gradle.Versions.androidx.activity
import Versions_gradle.Versions.androidx.annotation
import Versions_gradle.Versions.androidx.browser
import Versions_gradle.Versions.androidx.hilt
import Versions_gradle.Versions.androidx.lifecycle
import Versions_gradle.Versions.androidx.media3
import Versions_gradle.Versions.androidx.navigation
import Versions_gradle.Versions.androidx.paging
import Versions_gradle.Versions.androidx.palette
import Versions_gradle.Versions.androidx.room
import Versions_gradle.Versions.androidx.securityCrypto
import Versions_gradle.Versions.androidx.testExt
import Versions_gradle.Versions.androidx.testRunner
import Versions_gradle.Versions.androidx.tracing
import Versions_gradle.Versions.androidx.work
import Versions_gradle.Versions.apache.commonsCompress
import Versions_gradle.Versions.apache.commonsCsv
import Versions_gradle.Versions.compose.runtime
import Versions_gradle.Versions.compose.runtimeTracing
import Versions_gradle.Versions.google.appUpdate
import Versions_gradle.Versions.google.billing
import Versions_gradle.Versions.google.cronetOkHttp
import Versions_gradle.Versions.google.ossLicensesPlugin
import Versions_gradle.Versions.google.playServicesAds
import Versions_gradle.Versions.google.playServicesCronet
import Versions_gradle.Versions.google.playServicesOssLicenses
import Versions_gradle.Versions.google.secretsPlugin
import Versions_gradle.Versions.google.truth
import Versions_gradle.Versions.google.userMessagingPlatform
import Versions_gradle.Versions.junit.four
import Versions_gradle.Versions.junit.jupiter
import Versions_gradle.Versions.junit.jupiterAndroid
import Versions_gradle.Versions.kotlin.coroutines
import Versions_gradle.Versions.kotlin.datetime
import Versions_gradle.Versions.kotlin.ksp
import Versions_gradle.Versions.kotlin.serialization
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.initialization.resolve.DependencyResolutionManagement
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.kotlin.dsl.maven

object Versions {
    const val accompanist = "0.35.1-alpha"

    object android {
        const val gradle = "8.7.0-alpha03"
    }

    object androidx {
        const val activity = "1.9.0"
        const val annotation = "1.9.0-alpha01"
        const val browser = "1.8.0"
        const val core = "1.15.0-alpha01"
        const val hilt = "1.2.0"
        const val lifecycle = "2.8.4"
        const val media3 = "1.4.0"
        const val navigation = "2.8.0-beta06"
        const val paging = "3.3.1"
        const val palette = "1.0.0"
        const val room = "2.7.0-alpha05"
        const val securityCrypto = "1.1.0-alpha06"
        const val testExt = "1.2.1"
        const val testRunner = "1.6.1"
        const val tracing = "1.3.0-alpha02"
        const val work = "2.10.0-alpha02"
    }

    const val androidyoutubeplayer = "12.1.0"

    object apache {
        const val commonsCompress = "1.26.2"
        const val commonsCsv = "1.11.0"
    }

    const val apollo = "4.0.0-beta.7"
    const val awaitility = "4.2.1"
    const val benasher44Uuid = "0.8.4"
    const val bigNum = "0.3.10"
    const val coil = "3.0.0-alpha09"

    object compose {
        const val core = "1.7.0-beta06"
        const val plugin = "1.7.0-alpha01"
        const val runtime = "1.7.0-alpha01"
        const val runtimeTracing = "1.0.0-beta01"
    }

    const val cronetEmbedded = "119.6045.31"
    const val dexmakerInline = "2.28.3"
    const val flowExt = "1.0.0-RC"

    object google {
        const val appUpdate = "2.1.0"
        const val billing = "7.0.0"
        const val cronetOkHttp = "0.1.0"
        const val dagger = "2.51.1"
        const val ossLicensesPlugin = "0.10.6"
        const val playServicesAds = "23.2.0"
        const val playServicesCronet = "18.1.0"
        const val playServicesOssLicenses = "17.1.0"
        const val truth = "1.4.4"
        const val secretsPlugin = "2.0.1"
        const val userMessagingPlatform = "3.0.0"
    }

    const val graphQlJava = "22.1"
    const val htmlText = "1.6.0"
    const val jackson = "2.17.2"
    const val javaDiffUtils = "4.12"
    const val javaPoet = "1.13.0"
    const val jsonTree = "2.2.0"

    object junit {
        const val four = "4.13.2"
        const val jupiter = "5.11.0-M2"
        const val jupiterAndroid = "1.5.0"
    }

    object kotlin {
        const val core = "2.0.10-RC"
        const val coroutines = "1.9.0-RC"
        const val ksp = "2.0.10-RC-1.0.23"
        const val serialization = "1.7.1"
        const val datetime = "0.6.0"
    }

    const val kotlinPoet = "1.15.3"

    // TODO: Versions after don't let changing Dispatcher and breaks instrumentation tests
    const val ktor = "2.3.4"
    const val leakCanary = "3.0-alpha-8"
    const val rome = "2.1.0"
    const val manifoldGraphql = "2024.1.28"
    const val markwon = "4.6.2"
    const val material3 = "1.3.0-beta05"
    const val mockito = "5.12.0"
    const val mockitoKotlin = "5.4.0"
    const val molecule = "2.0.0"
    const val moshi = "1.15.1"
    const val neovisionariesInternationalization = "1.29"
    const val netflixDgs = "9.0.4"
    const val okhttp = "5.0.0-alpha.14"
    const val skrapeIt = "1.3.0-alpha.1"
    const val unityAds = "4.10.0"
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
                with(Versions.androidx) {
                    library("androidx.hilt:hilt-compiler:$hilt", prefix = "androidx")
                    library("androidx.room:room-compiler:$room")
                }
                withVersion(Versions.google.dagger) {
                    library("com.google.dagger:hilt-compiler")
                    library("com.google.dagger:hilt-android-compiler")
                }
                library("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
            }

            create("libs") {
                fun plugin(id: String) = plugin(id, id)
                plugin("com.android.library").version(Versions.android.gradle)
                plugin("com.apollographql.apollo3.external").version(Versions.apollo)
                plugin("com.autonomousapps.dependency-analysis").version("1.32.0")
                plugin("com.github.ben-manes.versions").version("0.51.0")
                plugin("com.google.dagger.hilt.android").version(Versions.google.dagger)
                plugin("com.jaredsburrows.license").version("0.9.8")
                plugin("com.netflix.dgs.codegen").version("6.2.2")
                plugin("de.mannodermaus.android-junit5").version("1.10.2.0")
                plugin("io.ktor.plugin").version(Versions.ktor)
                plugin("org.barfuin.gradle.taskinfo").version("2.2.0")
                plugin("org.jetbrains.compose").version(Versions.compose.plugin)

                with(Versions.kotlin) {
                    plugin("org.jetbrains.kotlin.multiplatform").version(core)
                    plugin("org.jetbrains.kotlin.plugin.compose").version(core)
                    library("org.jetbrains.kotlin.plugin.parcelize:org.jetbrains.kotlin.plugin.parcelize.gradle.plugin:$core")
                    library("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:$core")
                }

                with(Versions.androidx) {
                    library("androidx.activity:activity-compose:$activity")
                    library("androidx.annotation:annotation:$annotation", prefix = "androidx")
                    library("androidx.browser:browser:$browser", prefix = "androidx")
                    library("androidx.core:core-ktx:$core", prefix = "androidx")

                    withVersion(hilt) {
                        library("androidx.hilt:hilt-navigation-compose")
                        library("androidx.hilt:hilt-work")
                    }

                    withVersion(lifecycle) {
                        library("androidx.lifecycle:lifecycle-livedata-ktx")
                        library("androidx.lifecycle:lifecycle-viewmodel-compose")
                        library("androidx.lifecycle:lifecycle-runtime-ktx")
                    }

                    withVersion(media3) {
                        library("androidx.media3:media3-datasource-okhttp")
                        library("androidx.media3:media3-exoplayer")
                        library("androidx.media3:media3-exoplayer-dash")
                        library("androidx.media3:media3-exoplayer-hls")
                        library("androidx.media3:media3-exoplayer-rtsp")
                        library("androidx.media3:media3-ui")
                    }

                    library("androidx.navigation:navigation-compose:$navigation")

                    withVersion(paging) {
                        library("androidx.paging:paging-compose")
                        library("androidx.paging:paging-runtime-ktx")
                    }

                    library("androidx.palette:palette-ktx:$palette")

                    withVersion(room) {
                        library("androidx.room:room-ktx")
                        library("androidx.room:room-paging")
                        library("androidx.room:room-runtime")
                        library("androidx.room:room-testing")
                    }

                    library(
                        "androidx.security:security-crypto:$securityCrypto",
                        prefix = "androidx"
                    )
                    library("androidx.tracing:tracing:$tracing", prefix = "androidx")

                    withVersion(work) {
                        library("androidx.work:work-runtime-ktx")
                        library("androidx.work:work-runtime")
                    }

                    library("androidx.test.ext:junit:$testExt", alias = "androidx.junit.test")
                    library("androidx.test:runner:$testRunner", alias = "androidx.test.runner")
                }

                withVersion(Versions.material3) {
                    library("androidx.compose.material3:material3")
                    library("androidx.compose.material3:material3-adaptive-navigation-suite")
                }

                withVersion(Versions.apollo) {
                    library("com.apollographql.apollo3:apollo-compiler")
                    library("com.apollographql.apollo3:apollo-runtime")
                    library("com.apollographql.apollo3:apollo-normalized-cache")
                    library("com.apollographql.apollo3:apollo-normalized-cache-sqlite")
                }

                withVersion(Versions.accompanist) {
                    library("com.google.accompanist:accompanist-flowlayout")
                    library("com.google.accompanist:accompanist-navigation-animation")
                    library("com.google.accompanist:accompanist-pager-indicators")
                }

                with(Versions.google) {
                    library("com.google.android.play:app-update-ktx:$appUpdate")
                    library("com.android.billingclient:billing-ktx:$billing")
                    library("com.google.net.cronet:cronet-okhttp:$cronetOkHttp")

                    withVersion(dagger) {
                        library("com.google.dagger:hilt-android")
                        library("com.google.dagger:hilt-android-testing")
                    }

                    library("com.google.android.gms:oss-licenses-plugin:$ossLicensesPlugin")
                    library("com.google.android.gms:play-services-ads:$playServicesAds")
                    library(
                        "com.google.android.gms:play-services-cronet:$playServicesCronet",
                        alias = "cronet.play",
                    )
                    library("com.google.android.gms:play-services-oss-licenses:$playServicesOssLicenses")
                    library("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:$secretsPlugin")
                    library("com.google.truth:truth:$truth")
                    library("com.google.android.ump:user-messaging-platform:$userMessagingPlatform")
                }

                withVersion(Versions.leakCanary) {
                    library("com.squareup.leakcanary:leakcanary-android")
                    library("com.squareup.leakcanary:leakcanary-android-release")
                    library("com.squareup.leakcanary:leakcanary-object-watcher-android")
                }

                withVersion(Versions.okhttp) {
                    library("com.squareup.okhttp3:okhttp")
                    library(
                        "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}",
                        prefix = "okhttp3"
                    )
                }

                prefix("coil3") {
                    withVersion(Versions.coil) {
                        library("io.coil-kt.coil3:coil")
                        library("io.coil-kt.coil3:coil-compose")
                        library("io.coil-kt.coil3:coil-network-okhttp")
                    }
                }

                withVersion(Versions.ktor) {
                    library("io.ktor:ktor-server-core-jvm")
                    library("io.ktor:ktor-server-tests-jvm")
                }

                with(Versions.apache) {
                    library("org.apache.commons:commons-compress:$commonsCompress")
                    library("org.apache.commons:commons-csv:$commonsCsv")
                }

                with(Versions.kotlin) {
                    withVersion(coroutines) {
                        library("org.jetbrains.kotlinx:kotlinx-coroutines-android")
                        library("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                        library("org.jetbrains.kotlinx:kotlinx-coroutines-swing")
                        library("org.jetbrains.kotlinx:kotlinx-coroutines-test")
                    }
                    library("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$ksp")
                    library("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:$core")
                    library("org.jetbrains.kotlin:compose-compiler-gradle-plugin:$core")
                    library("org.jetbrains.kotlinx:kotlinx-datetime:$datetime")
                    library("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization")
                }

                with(Versions.junit) {
                    withVersion(jupiter) {
                        library("org.junit.jupiter:junit-jupiter-api")
                        library("org.junit.jupiter:junit-jupiter-engine")
                        library("org.junit.jupiter:junit-jupiter-params")
                    }
                    library("junit:junit:$four")

                    prefix("junit5") {
                        withVersion(jupiterAndroid) {
                            library("de.mannodermaus.junit5:android-test-core")
                            library("de.mannodermaus.junit5:android-test-runner")
                            library("de.mannodermaus.junit5:android-test-compose")
                        }
                    }
                }

                withVersion(Versions.mockito) {
                    library("org.mockito:mockito-android")
                    library("org.mockito:mockito-core")
                }

                prefix("compose") {
                    with(Versions.compose) {
                        withVersion(core) {
                            library("androidx.compose.animation:animation")
                            library("androidx.compose.material:material-icons-core")
                            library("androidx.compose.material:material-icons-extended")
                            library("androidx.compose.runtime:runtime")
                            library("androidx.compose.ui:ui-test-junit4")
                            library("androidx.compose.ui:ui-test-manifest")
                            library("androidx.compose.ui:ui-tooling-preview")
                            library("androidx.compose.ui:ui-tooling")
                            library("androidx.compose.ui:ui")
                        }

                        library("androidx.compose.runtime:runtime-tracing:$runtimeTracing")
                        library(
                            "org.jetbrains.compose.runtime:runtime:$runtime",
                            prefix = "jetBrainsCompose"
                        )
                    }
                }
                library("org.jetbrains.compose:compose-gradle-plugin:${Versions.compose.plugin}")

                prefix("markwon") {
                    withVersion(Versions.markwon) {
                        library("io.noties.markwon:core")
                        library("io.noties.markwon:editor")
                        library("io.noties.markwon:ext-strikethrough")
                        library("io.noties.markwon:ext-tables")
                        library("io.noties.markwon:html")
                        library("io.noties.markwon:linkify")
                        library("io.noties.markwon:simple-ext")
                    }
                }

                library("app.cash.molecule:molecule-runtime:${Versions.molecule}")
                library("com.android.tools.build:gradle:${Versions.android.gradle}")
                library("com.benasher44:uuid:${Versions.benasher44Uuid}")
                library("com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}")
                library("com.graphql-java:graphql-java:${Versions.graphQlJava}")
                library("com.ionspin.kotlin:bignum:${Versions.bigNum}")
                library("com.linkedin.dexmaker:dexmaker-mockito-inline-extended:${Versions.dexmakerInline}")
                library("com.neovisionaries:nv-i18n:${Versions.neovisionariesInternationalization}")
                library("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${Versions.netflixDgs}")
                library(
                    "com.pierfrancescosoffritti.androidyoutubeplayer:core:${Versions.androidyoutubeplayer}",
                    alias = "androidyoutubeplayer"
                )
                library("com.rometools:rome:${Versions.rome}")
                library("com.sebastianneubauer.jsontree:jsontree:${Versions.jsonTree}")
                library("com.squareup.moshi:moshi-kotlin:${Versions.moshi}")
                library("com.squareup:javapoet:${Versions.javaPoet}")
                library("com.squareup:kotlinpoet:${Versions.kotlinPoet}")
                library("com.unity3d.ads:unity-ads:${Versions.unityAds}")
                library("de.charlex.compose:html-text:${Versions.htmlText}")
                library("io.github.hoc081098:FlowExt:${Versions.flowExt}")
                library("io.github.java-diff-utils:java-diff-utils:${Versions.javaDiffUtils}")
                library("it.skrape:skrapeit:${Versions.skrapeIt}")
                library("org.awaitility:awaitility:${Versions.awaitility}")
                library("org.chromium.net:cronet-embedded:${Versions.cronetEmbedded}")
                library("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")
                library("systems.manifold:manifold-graphql-rt:${Versions.manifoldGraphql}")
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
