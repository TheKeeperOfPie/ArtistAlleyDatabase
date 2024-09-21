repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.com.google.devtools.ksp.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.jetBrainsCompose.compose.gradle.plugin)
    implementation(libs.gradle)
    implementation(libs.gradle.license.plugin)
    implementation(libs.javapoet)
    implementation(libs.kotlinpoet)
    implementation(libs.org.jetbrains.kotlin.android.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.parcelize.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.serialization.gradle.plugin)
    implementation(libs.oss.licenses.plugin)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}
