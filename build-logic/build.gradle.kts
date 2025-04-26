import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    `kotlin-dsl`
    alias(libs.plugins.app.cash.sqldelight).version("2.0.2")
}

// Enable Enum.entries support
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_1_9
        languageVersion = KotlinVersion.KOTLIN_1_9
    }
}

sqldelight {
    databases {
        create("BuildLogicDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.build_logic")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.0.2")
            srcDirs(
                project.layout.projectDirectory
                    .dir("../modules/alley/src/commonMain/sqldelight"),
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
    implementation(libs.commons.io)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.gradle)
    implementation(libs.javapoet)
    implementation(libs.jetBrainsCompose.compose.gradle.plugin)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinx.io.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.org.jetbrains.kotlin.android.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.parcelize.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.serialization.gradle.plugin)
    implementation(libs.webp.imageio)
    // TODO: Replace with catalog version once officially published
    implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}
