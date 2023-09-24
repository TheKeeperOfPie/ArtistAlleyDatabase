import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.io.ktor.plugin)
    alias(libs.plugins.com.netflix.dgs.codegen)
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

application {
    mainClass.set("com.thekeeperofpie.artistalleydatabase.server.AniListServerKt")
}

afterEvaluate {
    tasks.withType(KotlinCompile::class).forEach {
        it.kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xcontext-receivers"
        }
    }
}

tasks.withType<com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask> {
    schemaPaths += "${projectDir}/src/main/resources"
    packageName = "com.anilist.server.api.model"
    typeMapping += mapOf(
        "CountryCode" to "String",
        "Json" to "String",
        "FuzzyDateInt" to "String",
    )
    language = "kotlin"
    generateClientv2 = true
    generateKotlinNullableClasses = true
    generateKotlinClosureProjections = true
    generateDataTypes = true
}

dependencies {
    implementation(libs.ktor.server.core.jvm)

    implementation(platform(libs.graphql.dgs.platform.dependencies))
    implementation(libs.jackson.databind)

    implementation(libs.manifold.graphql.rt)

    testImplementation(project(":modules:anilist-data"))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.truth)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
