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
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("assemble") })
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("packageDebugAndroidTest") })
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("packageReleaseAndroidTest") })
    dependsOn("buildHealth")
    finalizedBy(":app:licenseReleaseReport")
}

dependencyAnalysis {
    dependencies {
        bundle("kotlin-stdlib") {
            includeGroup("org.jetbrains.kotlin")
        }
        bundle("androidx-room") {
            includeGroup("androidx.room")
        }
    }
    issues {
        all {
            onAny {
                severity("fail")
            }
            ignoreKtx(true)
            onUsedTransitiveDependencies {
                severity("ignore")
            }
            onUnusedDependencies {
                exclude(
                    "androidx.compose.animation:animation",
                    "androidx.compose.material:material-icons-core",
                    "androidx.compose.material:material-icons-extended",
                    "androidx.compose.material:material",
                    "androidx.compose.material3:material3",
                    "androidx.compose.ui:ui",
                    "androidx.compose.ui:ui-tooling-preview",
                    "androidx.constraintlayout:constraintlayout-compose",
                    "androidx.media3:media3-exoplayer-dash",
                    "androidx.media3:media3-exoplayer-hls",
                    "androidx.media3:media3-exoplayer-rtsp",
                    "androidx.navigation:navigation-compose",
                    "androidx.test.ext:junit",
                    "androidx.test:runner",
                    "com.google.dagger:hilt-android",
                    "com.google.truth:truth",
                    "com.squareup.moshi:moshi-kotlin",
                    "de.mannodermaus.junit5:android-test-core",
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
                    ":modules:settings",
                    ":modules:test-utils",
                    ":modules:vgmdb",
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

        project(":modules:test-utils") {
            onRuntimeOnly {
                exclude(
                    "org.jetbrains.kotlinx:kotlinx-coroutines-android",
                )
            }
        }
    }
}
