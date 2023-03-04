repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdkPreview = "UpsideDownCake"

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        testInstrumentationRunnerArguments["runnerBuilder"] =
            "de.mannodermaus.junit5.AndroidJUnit5Builder"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("_testFixtures") {
            matchingFallbacks += "debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    packaging {
        resources {
            merges += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES}"
            merges += "mozilla/public-suffix-list.txt"
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