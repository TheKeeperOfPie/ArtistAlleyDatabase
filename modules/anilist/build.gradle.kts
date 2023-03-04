@file:Suppress("UnstableApiUsage")

plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.apollographql.apollo3") version "3.7.4"
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
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
    }
}

if (!aniListSchemaFile.exists()) {
    tasks.findByName("generateAniListApolloSources")!!
        .dependsOn("downloadAniListApolloSchemaFromIntrospection")
}

dependencies {
    api("com.apollographql.apollo3:apollo-runtime:3.7.4")
    api(project(":modules:android-utils"))
    api(project(":modules:entry"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-compiler:2.45")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.compose.material:material-icons-core:1.4.0-beta01")
    implementation("androidx.compose.material:material-icons-extended:1.4.0-beta01")

    runtimeOnly("androidx.room:room-runtime:2.5.0")
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    testImplementation("androidx.room:room-testing:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")

    api("com.squareup.moshi:moshi-kotlin:1.14.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}