plugins {
    id("library-kotlin")
}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {
            api(libs.apollo.compiler)
        }
    }
}
