plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.art"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    api(project(":modules:browse"))
    api(project(":modules:compose-utils"))
    api(project(":modules:data"))
    api(project(":modules:entry"))

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.navigation.compose)

    implementation(libs.core.ktx)
    implementation(libs.compose.ui)
    api(libs.compose.ui.tooling.preview)
    implementation(libs.material3)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    implementation(libs.paging.compose)

    api(libs.moshi.kotlin)
    ksp(kspProcessors.moshi.kotlin.codegen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.test)
}
