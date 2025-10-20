plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.coil3.coil)
            implementation(libs.markwon.core)
            implementation(libs.markwon.ext.strikethrough)
            implementation(libs.markwon.ext.tables)
            implementation(libs.markwon.html)
            implementation(libs.markwon.linkify)
        }
        desktopMain.dependencies {
            implementation(libs.multiplatform.markdown.renderer.m3)
            implementation(libs.multiplatform.markdown.renderer.coil3)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.markdown"
    }
}
