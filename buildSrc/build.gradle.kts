plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "4.5.0"
    id("org.gradle.kotlin.kotlin-dsl.precompiled-script-plugins") version "4.5.0"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.com.google.devtools.ksp.gradle.plugin)
    // TODO: Remove this variant of the plugin
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.compose.gradle.plugin)
    implementation(libs.gradle)
    implementation(libs.javapoet)
    implementation(libs.kotlinpoet)
    implementation(libs.org.jetbrains.kotlin.android.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.parcelize.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.serialization.gradle.plugin)
    implementation(libs.oss.licenses.plugin)
    implementation(libs.secrets.gradle.plugin)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}
