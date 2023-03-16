@file:Suppress("UnstableApiUsage")

plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    alias(libs.plugins.com.apollographql.apollo3)
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anilist"
}

val aniListSchemaFile = project.layout.buildDirectory.file("graphql/aniList.graphqls").get().asFile
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
    }
}

if (!aniListSchemaFile.exists()) {
    tasks.findByName("generateAniListApolloSources")!!
        .dependsOn("downloadAniListApolloSchemaFromIntrospection")
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:entry"))
    api(libs.apollo.runtime)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    kapt(kaptProcessors.dagger.hilt.compiler)
    kapt(kaptProcessors.androidx.hilt.compiler)
    implementation(libs.material.icons.core)
    implementation(libs.material.icons.extended)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    api(libs.moshi.kotlin)
    ksp(kspProcessors.moshi.kotlin.codegen)

    implementation(libs.okhttp3.logging.interceptor)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.test)
}