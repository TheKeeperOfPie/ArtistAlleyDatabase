plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:utils"))
            implementation(project(":modules:utils-compose"))
            implementation(project(":modules:utils-network"))

            implementation(libs.apollo.runtime)
            implementation(libs.graphql.java)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.jsontree)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.debug"
}
