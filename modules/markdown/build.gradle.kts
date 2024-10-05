plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.multiplatform.markdown.renderer.m3)
            implementation(libs.multiplatform.markdown.renderer.coil3)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.coil3.coil)
            implementation(libs.markwon.core)
            implementation(libs.markwon.ext.strikethrough)
            implementation(libs.markwon.ext.tables)
            implementation(libs.markwon.html)
            implementation(libs.markwon.linkify)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.markdown"
}
