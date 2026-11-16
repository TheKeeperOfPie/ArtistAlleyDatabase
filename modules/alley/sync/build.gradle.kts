plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    applyDefaultHierarchyTemplate()

    js {
        browser {
            testTask {
                // Convention disables because CMP can't run in JS, so re-enable
                enabled = true
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.alley)
            implementation(projects.modules.alley.database)
            implementation(projects.modules.alley.user)
        }
        desktopTest.dependencies {
            implementation(projects.modules.alleyFunctions)
            implementation(libs.kotest.assertions.core)
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}
