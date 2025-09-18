import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    id("library-android")
    id("library-desktop")
    id("library-web")
    alias(libs.plugins.com.codingfeline.buildkonfig)
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.secrets"
    }
}

val properties = Properties().apply {
    load(rootProject.projectDir.resolve("secrets.properties").reader())
}

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.secrets"

    defaultConfigs {
        properties.forEach {
            buildConfigField(FieldSpec.Type.STRING, it.key.toString(), it.value.toString())
        }
    }
}
