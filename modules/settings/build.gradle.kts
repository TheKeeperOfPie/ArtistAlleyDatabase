plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

secrets {
    propertiesFileName = "secrets.properties"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.settings"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anime"))
    api(project(":modules:art"))
    api(project(":modules:cds"))
    api(project(":modules:monetization"))

    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    api(libs.androidx.security.crypto)

    api(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)

    api(libs.work.runtime)
    api(libs.work.runtime.ktx)

    testCompileOnly(testFixtures(project(":modules:art")))
    testImplementation(project(":modules:art", "_testFixtures"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    androidTestImplementation(project(":modules:test-utils"))
    androidTestCompileOnly(testFixtures(project(":modules:art")))
    androidTestImplementation(project(":modules:art", "_testFixtures"))
    androidTestImplementation(libs.androidx.junit.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.junit5.android.test.core)
    androidTestRuntimeOnly(libs.junit5.android.test.runner)
}
