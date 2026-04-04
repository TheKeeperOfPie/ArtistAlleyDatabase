plugins {
    id("library-web")
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "com.thekeeperofpie.artistalleydatabase.discord"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
