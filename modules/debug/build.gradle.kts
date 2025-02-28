plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(projects.modules.utilsNetwork)

            implementation(libs.apollo.runtime)
            implementation(libs.graphql.java)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.jsontree)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.debug"
}
