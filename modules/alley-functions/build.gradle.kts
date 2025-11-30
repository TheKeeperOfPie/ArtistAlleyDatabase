import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("app.cash.sqldelight")
}

group = "com.thekeeperofpie.artistalleydatabase.alley.functions"

kotlin {
    js {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.sqldelight.coroutines.extensions)

            implementation(projects.modules.alley.data)
            implementation(projects.modules.alley.models)
            implementation(npm("@cloudflare/pages-plugin-cloudflare-access", "1.0.5"))
        }
    }
}

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.functions")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.1.0")
            generateAsync = true
            dependency(project(":modules:alley:data"))
        }
    }
}

val distribution: NamedDomainObjectProvider<Configuration> by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = false
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
        target.set("es2015")
    }
}

artifacts {
    add(distribution.name, tasks.named("jsProductionExecutableCompileSync"))
}
