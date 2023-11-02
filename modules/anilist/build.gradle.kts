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
    namespace = "com.thekeeperofpie.artistalleydatabase.anilist"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist-data"))
    implementation(project(":modules:compose-utils"))
    api(project(":modules:entry"))
    api(project(":modules:network-utils"))

    implementation(libs.apollo.runtime)
    implementation(libs.apollo.normalized.cache)
    implementation(libs.apollo.normalized.cache.sqlite)

    implementation(libs.kotlinx.serialization.json)
    api(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.activity.compose)
    implementation(libs.androidx.browser)
    api(libs.androidx.security.crypto)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    api(libs.moshi.kotlin)
    ksp(kspProcessors.moshi.kotlin.codegen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.test)
}
