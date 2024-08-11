plugins {
    id("library-android")
    id("library-compose")
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
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.markdown"
}
