import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.com.apollographql.apollo3.external)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(18)
    sourceSets.all {
        languageSettings {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

afterEvaluate {
    tasks.withType(KotlinCompile::class).forEach {
        it.kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xcontext-receivers"
        }
    }
}

// Need to manually remove some types, as this downloads the default type definitions,
// which will conflict with codegen
val aniListSchemaFile: File = project.file("src/main/graphql/anilist/schema.graphqls")
apollo {
    service("aniList") {
        packageName.set("com.anilist")
        schemaFiles.from(aniListSchemaFile)
        introspection {
            endpointUrl.set("https://graphql.anilist.co")
            schemaFile.set(aniListSchemaFile)
        }
        codegenModels.set("responseBased")
        decapitalizeFields.set(true)

        plugin(project(":modules:apollo"))
    }
}

if (!aniListSchemaFile.exists()) {
    tasks.findByName("generateAniListApolloSources")!!
        .dependsOn("downloadAniListApolloSchemaFromIntrospection")
}

dependencies {
    api(libs.apollo.runtime)
    implementation(libs.jetBrainsCompose.runtime)
}
