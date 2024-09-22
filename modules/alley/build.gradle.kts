plugins {
    id("compose-library")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.alley"
}

secrets {
    propertiesFileName = project.file("secrets.properties").absolutePath
}

dependencies {
    implementation(projects.modules.anilist)
    api(projects.modules.data)
    api(projects.modules.entry)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.navigation.compose)

    implementation(libs.lifecycle.viewmodel.compose)

    api(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    runtimeOnly(libs.compose.ui.tooling)
    implementation(libs.accompanist.flowlayout)

    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)
    implementation(libs.compose.material.icons.extended)
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
