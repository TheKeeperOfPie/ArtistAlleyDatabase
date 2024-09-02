plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.cds"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    api(project(":modules:browse"))
    api(project(":modules:data"))
    api(project(":modules:entry"))
    api(project(":modules:musical-artists"))
    implementation(project(":modules:utils"))
    api(project(":modules:vgmdb"))

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.paging.compose)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.coil3.coil.compose)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    androidTestImplementation(libs.androidx.junit.test)
}
