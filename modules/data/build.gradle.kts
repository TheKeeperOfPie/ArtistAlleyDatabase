plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.data"
}

kotlin{
    sourceSets{
        commonMain.dependencies {
            api(project(":modules:anilist"))
            api(project(":modules:entry"))
            api(project(":modules:utils"))
            implementation(libs.kermit)
        }
    }
}
