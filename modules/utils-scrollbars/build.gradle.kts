plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-web")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.composeunstyled.scrollbars)
            implementation(projects.modules.utilsCompose)
        }
    }
}
