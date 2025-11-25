import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    kotlin("jvm") version "2.3.0-Beta2"
    `kotlin-dsl`
    alias(libs.plugins.app.cash.sqldelight).version("2.1.0")
}

// Enable Enum.entries support
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_1
        languageVersion = KotlinVersion.KOTLIN_2_1
    }
}

sqldelight {
    databases {
        create("BuildLogicDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.build_logic")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.1.0")
            srcDirs(
                project.layout.projectDirectory
                    .dir("../modules/alley/src/commonMain/sqldelight"),
                project.layout.projectDirectory
                    .dir("../modules/alley/data/src/commonMain/sqldelight"),
                project.layout.projectDirectory
                    .dir("../modules/alley/user/src/commonMain/sqldelight"),
                project.layout.projectDirectory
                    .dir("src/main/sqldelight"),
            )
        }
    }
}

dependencies {
    implementation("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
    implementation(libs.burst.gradle.plugin)
    implementation(libs.com.google.devtools.ksp.gradle.plugin)
    implementation(libs.commons.csv)
    implementation(libs.commons.io)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.dev.zacsweers.metro.gradle.plugin)
    implementation(libs.gradle)
    implementation(libs.jetBrainsCompose.compose.gradle.plugin)
    implementation(libs.kotlinx.io.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.org.jetbrains.kotlin.android.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.serialization.gradle.plugin)
    implementation(libs.scrimage.core)
    implementation(libs.scrimage.webp)
    implementation(libs.sqldelight.sqlite.driver)
    implementation(libs.webp.imageio)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
        optIn.add("kotlin.time.ExperimentalTime")
    }
}
