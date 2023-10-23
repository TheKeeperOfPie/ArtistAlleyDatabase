@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
    kotlin("android")
    id("android-test-library")
}

android {
    compileSdkPreview = "UpsideDownCake"

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner =
            "com.thekeeperofpie.artistalleydatabase.test_utils.CustomAndroidJUnitRunner"
        project.file("consumer-rules.pro")
            .takeIf(File::exists)
            ?.let { consumerProguardFiles(it) }

        testInstrumentationRunnerArguments["runnerBuilder"] =
            "com.thekeeperofpie.artistalleydatabase.test_utils.AndroidJUnitBuilder"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true

            proguardFiles(
                *listOfNotNull(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    project.file("proguard-rules.pro")
                        .takeIf(File::exists)
                ).toTypedArray()
            )
        }

        create("_testFixtures") {
            matchingFallbacks += "debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xcontext-receivers"
    }
    packaging {
        resources {
            merges += "/META-INF/*"
            merges += "mozilla/public-suffix-list.txt"

            // Can happen if an archive was built incrementally and accidentally published as-is
            excludes += "**/previous-compilation-data.bin"

            // Kotlin coroutines test
            pickFirsts += "win32-x86-64/attach_hotspot_windows.dll"
            pickFirsts += "win32-x86/attach_hotspot_windows.dll"

            // Unknown
            pickFirsts += "META-INF/licenses/ASM"

            // Mockito inline
            pickFirsts += "mockito-extensions/org.mockito.plugins.MockMaker"
        }
    }
    testFixtures {
        enable = true
    }

    sourceSets.findByName("_testFixtures")!!.kotlin
        .srcDir("${project.projectDir}/src/testFixtures/kotlin")
}

kotlin {
    jvmToolchain(18)
    sourceSets.all {
        languageSettings {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

/**
 * Temporary hack to support Kotlin testFixtures as it's currently
 * [unsupported](https://issuetracker.google.com/issues/139438142)
 * ```kotlin
 * dependencies {
 *    // Include the original testFixtures feature to support IDE syntax highlighting
 *    testCompileOnly(testFixtures(project(":module")))
 *    // Then use the actual configuration to link the runtime compiled Kotlin classes
 *    testImplementation(project(":module", "_testFixtures"))
 * }
 * ```
 */
val testFixturesJar = tasks.create<Jar>("testFixturesJar") {
    dependsOn("compile_testFixturesKotlin")
    from("$buildDir/tmp/kotlin-classes/_testFixtures")
    include("**/test/*.*")
    outputs.cacheIf { true }
}

configurations.create("_testFixtures")
artifacts {
    add("_testFixtures", testFixturesJar)
}

configurations.all {
    resolutionStrategy.capabilitiesResolution {
        all {
            // Choose the real runtime variant (testImplementation from explanation above)
            select(candidates.find { it.variantName == "_testFixtures" } ?: candidates.first())
        }
    }
}

// The KSP jvmTarget isn't set correctly, so fix it up here
afterEvaluate {
    tasks.withType(KotlinCompile::class).forEach {
        it.kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs += "-Xcontext-receivers"
        }
    }
}
