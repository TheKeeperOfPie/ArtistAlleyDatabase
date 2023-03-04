plugins {
    id("module-library")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.web_infra"
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}