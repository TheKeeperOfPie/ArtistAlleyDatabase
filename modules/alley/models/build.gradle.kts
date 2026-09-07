plugins {
    id("library-android")
    id("library-desktop")
    id("library-web")
}

kotlin {
    android { namespace = "com.thekeeperofpie.artistalleydatabase.alley.models" }

    sourceSets {
        val jvmMain = create("jvmMain") {
            dependsOn(commonMain.get())
        }
        androidMain { dependsOn(jvmMain) }
        desktopMain { dependsOn(jvmMain) }
        commonMain.dependencies {
            api("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.whyoleg.cryptography.core)
            implementation(libs.whyoleg.cryptography.provider.optimal)
            implementation(projects.modules.utilsBuildConfig)
        }
    }
}
