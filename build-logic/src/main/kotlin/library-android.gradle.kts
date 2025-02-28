@file:Suppress("UnstableApiUsage")

import org.gradle.internal.extensions.stdlib.capitalized
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

        project.file("consumer-rules.pro")
            .takeIf(File::exists)
            ?.let { consumerProguardFiles(it) }
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
                "libs.kotlinx.coroutines.test",
            ).forEach(::implementation)
        }
    }
}

// https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/912
androidComponents {
    beforeVariants(selector().all()) { variant ->
        val name = variant.name.capitalized()
        val resourceTask = "generateActualResourceCollectorsForAndroidMain"
        tasks.configureEach {
            if (this.name == "explodeCodeSource$name" && tasks.findByName(resourceTask) != null) {
                dependsOn(resourceTask)
            }
        }
    }
}
