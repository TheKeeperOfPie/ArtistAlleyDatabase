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
    val secretsFile = rootProject.projectDir.resolve("secrets.properties")
    if (secretsFile.exists()) {
        load(secretsFile.reader())
    }
}

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.secrets"

    defaultConfigs {
        properties.forEach {
            buildConfigField(FieldSpec.Type.STRING, it.key.toString(), it.value.toString())
        }
    }
}
