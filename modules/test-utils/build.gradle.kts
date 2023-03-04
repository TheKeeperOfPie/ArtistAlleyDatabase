plugins {
    id("module-library")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.test_utils"
}

dependencies {
    api("org.mockito:mockito-core:5.1.1")
    implementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}