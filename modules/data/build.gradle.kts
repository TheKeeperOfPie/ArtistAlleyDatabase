plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.data"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    api(project(":modules:entry"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.test)
}
