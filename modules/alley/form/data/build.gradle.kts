plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-web")
    id("app.cash.sqldelight")
}

group = "com.thekeeperofpie.artistalleydatabase.alley.form.data"

kotlin {
    compilerOptions {
        optIn.addAll(
            "kotlin.time.ExperimentalTime",
            "kotlin.uuid.ExperimentalUuidApi",
        )
    }

    android {
        namespace = "com.thekeeperofpie.artistalleydatabase.alley.form.data"
        compileSdk = 36
        minSdk = 28
    }

    sourceSets {
        commonMain.dependencies {
            api("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
            api(projects.modules.alley.data)
        }
    }
}

sqldelight {
    databases {
        create("AlleyFormDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.form.data")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            generateAsync = true
        }
    }
}
