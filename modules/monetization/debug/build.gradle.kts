plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization.debug"
}

dependencies {
    api(project(":modules:compose-utils"))
    api(project(":modules:monetization"))

    implementation(libs.hilt.android)
    kapt(kaptProcessors.dagger.hilt.compiler)
    kapt(kaptProcessors.androidx.hilt.compiler)
}
