import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-room")
    alias(libs.plugins.com.codingfeline.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.anilist)
            api(projects.modules.data)
            api(projects.modules.entry)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(projects.modules.utilsRoom)

            // TODO: This import doesn't work since 1.8.0-alpha01 isn't published for this artifact
//            implementation(compose.material3AdaptiveNavigationSuite)
            implementation("org.jetbrains.compose.material3:material3-adaptive-navigation-suite:1.7.3")
            implementation(libs.coil3.coil.compose)
            implementation(libs.coil3.coil.compose)
            implementation(libs.commons.csv)
            implementation(libs.jetBrainsCompose.navigation.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.alley"
}

val properties = Properties().apply {
    load(projectDir.resolve("secrets.properties").reader())
}

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.alley.secrets"

    defaultConfigs {
        properties.forEach {
            buildConfigField(FieldSpec.Type.STRING, it.key.toString(), it.value.toString())
        }
    }
}
