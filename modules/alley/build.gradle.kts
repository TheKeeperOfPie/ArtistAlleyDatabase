import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.util.Properties

plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
    alias(libs.plugins.com.codingfeline.buildkonfig)
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvm") {
                withAndroidTarget()
                withJvm()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.modules.entry)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)

            // TODO: This import doesn't work since 1.8.0-alpha01 isn't published for this artifact
//            implementation(compose.material3AdaptiveNavigationSuite)
            implementation("org.jetbrains.compose.material3:material3-adaptive-navigation-suite:1.7.3")
            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsCompose.navigation.compose)
        }
        val jvmMain by getting {
            dependencies {
                implementation(projects.modules.utilsRoom)
                api(libs.room.paging)
                implementation(libs.commons.csv)
                runtimeOnly(libs.room.runtime)
            }
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

dependencies {
    add("kspAndroid", kspProcessors.room.compiler)
    add("kspDesktop", kspProcessors.room.compiler)
}

val inputsTask = tasks.register<ArtistAlleyProcessInputsTask>("processArtistAlleyInputs")

compose.resources {
    publicResClass = true
    customDirectory("commonMain", inputsTask.map { it.outputResources.get() })
}
