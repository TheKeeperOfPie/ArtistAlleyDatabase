buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.42")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.8.2.1")
    }
}

plugins {
    id("com.android.application") version "8.0.0-alpha07" apply false
    id("com.android.library") version "8.0.0-alpha07" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20-Beta" apply false
    id("org.jetbrains.kotlin.jvm") version "1.7.20-Beta" apply false
    id("com.autonomousapps.dependency-analysis") version "1.13.1"
}

tasks.register("copyGitHooks", Copy::class) {
    from(File(rootProject.rootDir, "scripts/git/pre-commit"))
    into(File(rootProject.rootDir, ".git/hooks"))
    fileMode = 777
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
                    "androidx.compose.ui:ui-tooling-preview",
                    "com.google.dagger:hilt-android",
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
                    "androidx.lifecycle:lifecycle-runtime-ktx",
                    "com.squareup.moshi:moshi-kotlin",
                )
            }
        }

        project(":modules:anilist") {
            onUnusedDependencies {
                exclude(
                    "com.squareup.moshi:moshi-kotlin",
                )
            }
        }

        project(":modules:art") {
            onUnusedDependencies {
                exclude(
                    "com.squareup.moshi:moshi-kotlin",
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

        project(":modules:cds") {
            onUnusedDependencies {
                exclude(
                    "com.squareup.moshi:moshi-kotlin",
                )
            }
        }
    }
}