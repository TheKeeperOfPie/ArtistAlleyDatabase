import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.com.google.dagger.hilt.android) apply false
    alias(libs.plugins.com.autonomousapps.dependency.analysis)
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.org.barfuin.gradle.taskinfo)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = true
    outputFormatter = "html"
}

tasks.register("copyGitHooks", Copy::class) {
    from(File(rootProject.rootDir, "scripts/git/pre-commit"))
    into(File(rootProject.rootDir, ".git/hooks"))
    fileMode = 777
}

val verificationMetadataFile = File(rootProject.rootDir, "gradle/verification-metadata.xml")
tasks.register("recopyVerificationMetadata") {
    verificationMetadataFile.resolveSibling("verification-metadata-base.xml")
        .copyTo(verificationMetadataFile, overwrite = true)
}

tasks.register("generateVerificationMetadata") {
    dependsOn("recopyVerificationMetadata")
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("assembleDebug") })
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("packageDebugAndroidTest") })
    dependsOn("buildHealth")
    finalizedBy(":app:licenseReleaseReport")
}

dependencyAnalysis {
    structure {
        bundle("kotlin-stdlib") {
            includeGroup("org.jetbrains.kotlin")
        }
        bundle("androidx-room") {
            includeGroup("androidx.room")
        }
        ignoreKtx(true)
    }
    issues {
        all {
            onAny {
                severity("fail")
            }
            onUsedTransitiveDependencies {
                severity("ignore")
            }
            onIncorrectConfiguration {
                exclude(
                    ":modules:network-utils",
                    "com.apollographql.apollo3:apollo-normalized-cache-sqlite",
                    "io.coil-kt:coil-compose",
                    "org.jetbrains.kotlin:kotlin-stdlib",
                )
            }
            onRuntimeOnly {
                exclude(
                    "com.linkedin.dexmaker:dexmaker-mockito-inline-extended",
                    ":modules:test-utils",
                )
            }
            onUnusedDependencies {
                exclude(
                    "androidx.compose.animation:animation",
                    "androidx.compose.material:material-icons-core",
                    "androidx.compose.material:material-icons-extended",
                    "androidx.compose.material:material",
                    "androidx.compose.material3:material3",
                    "androidx.compose.ui:ui",
                    "androidx.compose.ui:ui-test-junit4",
                    "androidx.compose.ui:ui-test-manifest",
                    "androidx.compose.ui:ui-tooling-preview",
                    "androidx.constraintlayout:constraintlayout-compose",
                    "androidx.media3:media3-exoplayer-dash",
                    "androidx.media3:media3-exoplayer-hls",
                    "androidx.media3:media3-exoplayer-rtsp",
                    "androidx.navigation:navigation-compose",
                    "androidx.paging:paging-compose",
                    "androidx.test.ext:junit",
                    "androidx.test:runner",
                    "com.android.billingclient:billing-ktx",
                    "com.apollographql.apollo3:apollo-normalized-cache-sqlite",
                    "com.google.dagger:hilt-android",
                    "com.google.truth:truth",
                    "com.squareup.leakcanary:leakcanary-android",
                    "com.squareup.moshi:moshi-kotlin",
                    "de.mannodermaus.junit5:android-test-core",
                    "de.mannodermaus.junit5:android-test-runner",
                    "io.coil-kt:coil-compose",
                    "org.jetbrains.kotlin:kotlin-stdlib",
                    "org.jetbrains.kotlinx:kotlinx-serialization-json",
                    "org.mockito:mockito-core",
                    "org.mockito:mockito-android",
                    "org.mockito.kotlin:mockito-kotlin",

                    // This isn't detected properly, not sure why
                    "com.google.accompanist:accompanist-flowlayout",

                    // Exclude the list of modules as the plugin doesn't
                    // play well with the Kotlin testFixtures hack
                    ":modules:alley",
                    ":modules:alley-app",
                    ":modules:android-utils",
                    ":modules:anilist",
                    ":modules:anilist-data",
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
                    ":modules:monetization:admob",
                    ":modules:monetization:debug",
                    ":modules:monetization:unity",
                    ":modules:musical-artists",
                    ":modules:network-utils",
                    ":modules:play",
                    ":modules:settings",
                    ":modules:test-utils",
                    ":modules:vgmdb",

                    // Testing
                    "com.google.dagger:hilt-android-testing",
                    "org.junit.jupiter:junit-jupiter-api",
                    "com.linkedin.dexmaker:dexmaker-mockito-inline-extended",
                )
            }
            onUnusedAnnotationProcessors {
                exclude(
                    "androidx.hilt:hilt-compiler",
                    "com.google.dagger:hilt-compiler",
                )
            }
        }

        project(":app") {
            onUnusedDependencies {
                exclude(
                    "androidx.compose.ui:ui-tooling",
                    "androidx.core:core-ktx",
                    "androidx.lifecycle:lifecycle-runtime-ktx",
                    "io.coil-kt:coil-compose",
                )
            }
        }

        project(":modules:anilist-data") {
            onUnusedDependencies {
                exclude(
                    "com.apollographql.apollo3:apollo-runtime",
                    "org.jetbrains.compose.runtime:runtime",
                )
            }
        }

        project(":modules:anime") {
            onUnusedDependencies {
                exclude(
                    "androidx.tracing:tracing",
                )
            }
        }

        project(":modules:browse") {
            onUnusedDependencies {
                exclude(
                    "androidx.navigation:navigation-compose",
                )
            }
        }

        project(":modules:compose-utils") {
            onUnusedDependencies {
                exclude(
                    "io.coil-kt:coil-compose",
                )
            }
        }

        project(":modules:server") {
            onUnusedDependencies {
                exclude(
                    "io.ktor:ktor-server-tests-jvm",
                    "systems.manifold:manifold-graphql-rt",
                )
            }
        }

        project(":modules:test-utils") {
            onUnusedDependencies {
                exclude(
                    "io.ktor:ktor-server-tests-jvm",
                )
            }
            onRuntimeOnly {
                exclude(
                    "org.jetbrains.kotlinx:kotlinx-coroutines-android",
                )
            }
        }
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            if (project.findProperty("composeCompilerReports") == "true") {
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                            project.layout.buildDirectory.asFile.get().absolutePath +
                            "/compose_compiler"
                )
            }
            if (project.findProperty("composeCompilerMetrics") == "true") {
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                            project.layout.buildDirectory.asFile.get().absolutePath +
                            "/compose_compiler"
                )
            }
            freeCompilerArgs += listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:experimentalStrongSkipping=true",

                // TODO: Figure out why build infers 1.5.4 when 1.5.8 is declared
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true",
            )
        }
    }
}
