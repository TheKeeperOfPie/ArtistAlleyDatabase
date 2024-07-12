plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "4.5.0"
    id("org.gradle.kotlin.kotlin-dsl.precompiled-script-plugins") version "4.5.0"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.gradle)
    implementation(libs.org.jetbrains.kotlin.android.gradle.plugin)
    implementation(libs.com.google.devtools.ksp.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.javapoet)
    implementation(libs.kotlinpoet)
    implementation(libs.oss.licenses.plugin)
    implementation(libs.secrets.gradle.plugin)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}
