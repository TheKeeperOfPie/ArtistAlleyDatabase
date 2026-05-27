plugins {
    id("library-web")
    id("app.cash.sqldelight")
}

group = "com.thekeeperofpie.artistalleydatabase.alley.backend.data"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
            api(projects.modules.alley.data)
        }
    }
}

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.backend.data")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            generateAsync = true
            dependency(project(":modules:alley:data"))
        }
    }
}
