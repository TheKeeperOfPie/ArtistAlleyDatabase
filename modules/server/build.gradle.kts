plugins {
    id("library-kotlin")
    alias(libs.plugins.io.ktor.plugin)
    alias(libs.plugins.com.netflix.dgs.codegen)
}

kotlin {
    jvm("desktop")
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.server.core.jvm)

//            implementation(platform(libs.graphql.dgs.platform.dependencies))
            implementation(libs.jackson.databind)

            implementation(libs.manifold.graphql.rt)
        }
        commonTest.dependencies {
            implementation(projects.modules.anilist.data)
            implementation(libs.junit.jupiter.api)
            implementation(libs.ktor.server.test.host)
            implementation(libs.truth)
            runtimeOnly(libs.junit.jupiter.engine)
        }
    }
}

application {
    mainClass.set("com.thekeeperofpie.artistalleydatabase.server.AniListServerKt")
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
