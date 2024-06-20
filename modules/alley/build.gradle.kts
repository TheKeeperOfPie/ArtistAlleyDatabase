plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.alley"
}

secrets {
    propertiesFileName = project.file("secrets.properties").absolutePath
}

dependencies {
    implementation(project(":modules:anilist"))
    api(project(":modules:compose-utils"))
    api(project(":modules:data"))
    api(project(":modules:entry"))

    implementation(libs.kotlinx.serialization.json)

    implementation("androidx.navigation:navigation-compose:2.8.0-beta03")

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    implementation(libs.lifecycle.viewmodel.compose)

    api(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    runtimeOnly(libs.compose.ui.tooling)
    implementation(libs.accompanist.flowlayout)

    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material3.adaptive.navigation.suite)
    implementation(libs.paging.compose)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    implementation(libs.commons.csv)
    implementation(libs.accompanist.pager.indicators)
}
