plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

secrets {
    propertiesFileName = "secrets.properties"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.play"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:compose-utils"))
    api(project(":modules:monetization"))

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    api(libs.material3)

    implementation(libs.billing.ktx)

    implementation(libs.app.update.ktx)
}
