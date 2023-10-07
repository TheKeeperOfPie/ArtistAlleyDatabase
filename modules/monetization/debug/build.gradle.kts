plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization.debug"
}

dependencies {
    api(project(":modules:compose-utils"))
    api(project(":modules:monetization"))

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)
}
