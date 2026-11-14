plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
    id("app.cash.sqldelight")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.paging.common)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(projects.modules.alley.data)
            implementation(projects.modules.alley.user)
            implementation(projects.modules.utils)
        }
    }
}

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.database")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            generateAsync = true
            dependency(projects.modules.alley.data)
            dependency(projects.modules.alley.user)
        }
    }
}
