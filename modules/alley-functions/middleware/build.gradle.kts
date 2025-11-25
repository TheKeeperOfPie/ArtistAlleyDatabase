import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.com.codingfeline.buildkonfig)
}

group = "com.thekeeperofpie.artistalleydatabase.alley.functions.middleware"

kotlin {
    js {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.secrets)
            implementation(npm("@cloudflare/pages-plugin-cloudflare-access", "1.0.5"))
        }
    }
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
        target.set("es2015")
    }
}

val properties = Properties().apply {
    val secretsFile = projectDir.resolve("secrets.properties")
    if (secretsFile.exists()) {
        load(secretsFile.reader())
    }
}

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.alley.functions.middleware.secrets"

    defaultConfigs {
        properties.forEach {
            buildConfigField(FieldSpec.Type.STRING, it.key.toString(), it.value.toString())
        }
    }
}

val distribution: NamedDomainObjectProvider<Configuration> by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(distribution.name, tasks.named("jsProductionExecutableCompileSync"))
}
