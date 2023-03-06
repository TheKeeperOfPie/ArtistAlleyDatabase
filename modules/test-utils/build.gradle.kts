plugins {
    id("module-library")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.test_utils"
}

dependencies {
    api("org.mockito:mockito-core:5.1.1")
    api("org.mockito:mockito-android:5.1.1")
    implementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    api("org.awaitility:awaitility:4.2.0") {
        exclude("org.hamcrest", "hamcrest")
    }
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}