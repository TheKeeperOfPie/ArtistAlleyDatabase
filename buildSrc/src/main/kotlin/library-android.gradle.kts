@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
plugins {
    id("library-kotlin")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    compileSdk = 35
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

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
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget = JvmTarget.JVM_18
        }
    }

    sourceSets {
        getByName("androidInstrumentedTest").dependencies {
            implementation(project(":modules:test-utils"))
            implementation(project(":modules:utils-network"))

            libs.find(
                "libs.androidx.junit.test",
                "libs.androidx.test.runner",
                "libs.dexmaker.mockito.inline.extended",
                "libs.junit.jupiter.api",
                "libs.junit5.android.test.core",
                "libs.kotlinx.coroutines.test",
            ).forEach(::implementation)

            libs.find(
                "libs.junit.jupiter.engine",
                "libs.junit5.android.test.runner",
            ).forEach(::runtimeOnly)
        }
//        androidInstrumentedTest.dependencies {
//        androidTest.dependencies {
//        }
    }
}
