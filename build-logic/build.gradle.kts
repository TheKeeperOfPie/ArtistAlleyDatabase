

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots")
}

plugins {
    kotlin("jvm") version "2.4.10"
    `kotlin-dsl`
    alias(libs.plugins.app.cash.sqldelight).version("2.2.1")
    alias(libs.plugins.org.jetbrains.compose)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.compose)
}

sqldelight {
    databases {
        create("BuildLogicEditDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.build_logic.edit")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            srcDirs(
                project.layout.projectDirectory
                    .dir("../modules/alley/src/commonMain/sqldelight"),
                project.layout.projectDirectory
                    .dir("../modules/alley/data/src/commonMain/sqldelight"),
                project.layout.projectDirectory
                    .dir("../modules/alley/user/src/commonMain/sqldelight"),
                project.layout.projectDirectory
                    .dir("src/main/sqldelight/edit"),
            )
        }
        create("BuildLogicFormDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.build_logic.form")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            srcDirs(
                project.layout.projectDirectory
                    .dir("../modules/alley/form/data/src/commonMain/sqldelight"),
                project.layout.projectDirectory
                    .dir("src/main/sqldelight/form"),
            )
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.androidx.annotation)
    implementation(libs.apache.commons.csv)
    implementation(libs.apache.commons.io)
    implementation(libs.burst.gradle.plugin)
    implementation(libs.com.google.devtools.ksp.gradle.plugin)
    implementation(libs.com.mikepenz.aboutlibraries.plugin.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.dev.zacsweers.metro.gradle.plugin)
    implementation(libs.gradle)
    implementation(libs.jetBrainsCompose.compose.gradle.plugin)
    implementation(libs.jetBrainsCompose.ui)
    implementation(libs.jsoup)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.io.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ksoup.network)
    implementation(libs.material.kolor.palette.core)
    implementation(libs.org.jetbrains.kotlin.android.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.serialization.gradle.plugin)
    implementation(libs.scrimage.core)
    implementation(libs.scrimage.webp)
    implementation(libs.shared)
    implementation(libs.sqldelight.sqlite.driver)
    implementation(libs.uri.kmp)
    implementation(libs.webp.imageio)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        optIn.addAll(
            "kotlin.time.ExperimentalTime",
            "kotlin.uuid.ExperimentalUuidApi",
            "kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}
