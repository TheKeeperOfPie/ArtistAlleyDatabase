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
import Versions_gradle.Versions.compose.runtimeTracing
import Versions_gradle.Versions.composeMultiplatform.runtime
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
    const val accompanist = "0.35.2-beta"

    object android {
        const val gradle = "8.7.0-alpha09"
    }

    const val androidJunit5 = "1.11.0.0"

    object androidx {
        const val activity = "1.9.0"
        const val annotation = "1.9.0-alpha02"
        const val browser = "1.8.0"
        const val core = "1.15.0-alpha02"
        const val hilt = "1.2.0"
        const val lifecycle = "2.9.0-alpha01"
        const val media3 = "1.4.1"
        const val navigation = "2.8.0-rc01"
        const val paging = "3.3.2"
        const val palette = "1.0.0"
        const val room = "2.7.0-alpha07"
        const val securityCrypto = "1.1.0-alpha06"
        const val testExt = "1.2.1"
        const val testRunner = "1.6.2"
        const val tracing = "1.3.0-alpha02"
        const val work = "2.10.0-alpha02"
    }

    const val androidyoutubeplayer = "12.1.0"

    object apache {
        const val commonsCompress = "1.27.1"
        const val commonsCsv = "1.11.0"
    }

    const val apollo = "4.0.0-beta.7"
    const val awaitility = "4.2.2"
    const val barfuinTaskInfo = "2.2.0"
    const val benasher44Uuid = "0.8.4"
    const val benManesVersions = "0.51.0"
    const val bigNum = "0.3.10"
    const val coil = "3.0.0-alpha10"

    object compose {
        const val core = "1.7.0-rc01"
        const val runtimeTracing = "1.0.0-beta01"
    }

    object composeMultiplatform {
        object androidx {
            const val navigation = "2.8.0-alpha09"
        }
        const val plugin = "1.7.0-alpha03"
        const val runtime = "1.7.0-alpha03"
    }

    const val cronetEmbedded = "119.6045.31"

    const val dependencyAnalysis = "2.0.1"
    const val dexmakerInline = "2.28.4"
    const val diffUtils = "0.7.0"
    const val flowExt = "1.0.0-RC"
    const val fluidI18n = "0.13.0"

    object google {
        const val appUpdate = "2.1.0"
        const val billing = "7.0.0"
        const val cronetOkHttp = "0.1.0"
        const val dagger = "2.52"
        const val ossLicensesPlugin = "0.10.6"
        const val playServicesAds = "23.3.0"
        const val playServicesCronet = "18.1.0"
        const val playServicesOssLicenses = "17.1.0"
        const val truth = "1.4.4"
        const val secretsPlugin = "2.0.1"
        const val userMessagingPlatform = "3.0.0"
    }

    const val graphQlJava = "22.1"
    const val htmlText = "1.6.0"
    const val humanReadable = "1.9.0"
    const val jackson = "2.17.2"
    const val jaredsBurrowsLicense = "0.9.8"
    const val javaPoet = "1.13.0"
    const val jsonTree = "2.2.0"

    object junit {
        const val four = "4.13.2"
        const val jupiter = "5.11.0"
        const val jupiterAndroid = "1.5.0"
    }

    const val kermit = "2.0.4"

    object kotlin {
        const val core = "2.0.20"
        const val coroutines = "1.9.0-RC.2"
        const val datetime = "0.6.1"
        const val io = "0.5.3"
        const val ksp = "2.0.20-1.0.24"
        const val serialization = "1.7.1"
    }

    const val kotlinInject = "0.7.1"
    const val kotlinPoet = "1.15.3"

    // TODO: Versions after 2.3.4 don't support changing Dispatcher and break instrumentation tests
    const val ktor = "3.0.0-beta-2"
    const val ksoup = "0.1.6-alpha1"
    const val leakCanary = "3.0-alpha-8"
    const val manifoldGraphql = "2024.1.30"
    const val markwon = "4.6.2"
    const val material3 = "1.3.0-rc01"
    const val mockito = "5.13.0"
    const val mockitoKotlin = "5.4.0"
    const val molecule = "2.0.0"
    const val netflixDgs = "9.1.0"
    const val netflixDgsCodegen = "6.3.0"
    const val okhttp = "5.0.0-alpha.14"
    const val sekret = "2.0.0-alpha-07"
    const val skrapeIt = "1.3.0-alpha.1"
    const val unityAds = "4.12.2"
    const val uriKmp = "0.0.18"
    const val xmlUtil = "0.90.2-beta1"
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
                library("me.tatarka.inject:kotlin-inject-compiler-ksp:${Versions.kotlinInject}")
            }

            create("libs") {
                fun plugin(id: String) = plugin(id, id)
                plugin("com.android.library").version(Versions.android.gradle)
                plugin("com.apollographql.apollo3.external").version(Versions.apollo)
                plugin("com.autonomousapps.dependency-analysis").version(Versions.dependencyAnalysis)
                plugin("com.github.ben-manes.versions").version(Versions.benManesVersions)
                plugin("com.google.dagger.hilt.android").version(Versions.google.dagger)
                plugin("com.jaredsburrows.license").version(Versions.jaredsBurrowsLicense)
                plugin("com.netflix.dgs.codegen").version(Versions.netflixDgsCodegen)
                plugin("de.mannodermaus.android-junit5").version(Versions.androidJunit5)
                plugin("io.ktor.plugin").version(Versions.ktor)
                plugin("org.barfuin.gradle.taskinfo").version(Versions.barfuinTaskInfo)
                plugin("org.jetbrains.compose").version(Versions.composeMultiplatform.plugin)

                with(Versions.kotlin) {
                    plugin("org.jetbrains.kotlin.multiplatform").version(core)
                    plugin("org.jetbrains.kotlin.plugin.compose").version(core)
                    library("org.jetbrains.kotlin:kotlin-test:$core")
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
                        library("androidx.paging:paging-common")
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
                    library("com.apollographql.apollo3:apollo-engine-ktor")
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
                    library("io.ktor:ktor-client-core")
                    library("io.ktor:ktor-client-okhttp")
                    library("io.ktor:ktor-client-mock")
                    library("io.ktor:ktor-server-core-jvm")
                    library("io.ktor:ktor-server-test-host")
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
                    library("org.jetbrains.kotlinx:kotlinx-io-core:$io")
                    library("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization")
                    library("org.jetbrains.kotlinx:kotlinx-serialization-json-io:$serialization")
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
                    }
                }

                prefix("jetBrainsCompose") {
                    with(Versions.composeMultiplatform) {
                        library("org.jetbrains.compose:compose-gradle-plugin:$plugin")
                        library("org.jetbrains.compose.runtime:runtime:$runtime")

                        with(Versions.composeMultiplatform.androidx) {
                            library("org.jetbrains.androidx.navigation:navigation-compose:$navigation")
                        }
                    }
                }

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

                prefix("xmlutil") {
                    withVersion(Versions.xmlUtil) {
                        library("io.github.pdvrieze.xmlutil:serialization")
                        library("io.github.pdvrieze.xmlutil:serialization-jvm")
                        library("io.github.pdvrieze.xmlutil:serialization-android")
                    }
                }

                library("app.cash.molecule:molecule-runtime:${Versions.molecule}")
                library("co.touchlab:kermit:${Versions.kermit}")
                library("com.android.tools.build:gradle:${Versions.android.gradle}")
                library("com.benasher44:uuid:${Versions.benasher44Uuid}")
                library("com.eygraber:uri-kmp:${Versions.uriKmp}")
                library("com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}")
                library("com.fleeksoft.ksoup:ksoup:${Versions.ksoup}")
                library("com.graphql-java:graphql-java:${Versions.graphQlJava}")
                library("com.ionspin.kotlin:bignum:${Versions.bigNum}")
                library("com.linkedin.dexmaker:dexmaker-mockito-inline-extended:${Versions.dexmakerInline}")
                library("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${Versions.netflixDgs}")
                library(
                    "com.pierfrancescosoffritti.androidyoutubeplayer:core:${Versions.androidyoutubeplayer}",
                    alias = "androidyoutubeplayer"
                )
                library("com.sebastianneubauer.jsontree:jsontree:${Versions.jsonTree}")
                library("com.squareup:javapoet:${Versions.javaPoet}")
                library("com.squareup:kotlinpoet:${Versions.kotlinPoet}")
                library("com.unity3d.ads:unity-ads:${Versions.unityAds}")
                library("de.charlex.compose:html-text:${Versions.htmlText}")
                library("io.fluidsonic.country:fluid-country:${Versions.fluidI18n}")
                library("io.fluidsonic.i18n:fluid-i18n:${Versions.fluidI18n}")
                library("io.github.hoc081098:FlowExt:${Versions.flowExt}")
                library("io.github.petertrr:kotlin-multiplatform-diff:${Versions.diffUtils}")
                library("it.skrape:skrapeit:${Versions.skrapeIt}")
                library("me.tatarka.inject:kotlin-inject-runtime-kmp:${Versions.kotlinInject}")
                library("nl.jacobras:Human-Readable:${Versions.humanReadable}")
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
