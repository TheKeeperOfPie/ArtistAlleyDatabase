@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.apollographql.apollo3") version "3.7.4"
    id("com.google.devtools.ksp") version "1.8.0-1.0.9"
    kotlin("plugin.serialization") version "1.8.0"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anilist"
    compileSdk = 33

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
}

kotlin {
    jvmToolchain(18)
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

tasks.register("downloadAniListSchema") {
    if (!aniListSchemaFile.exists()) {
        finalizedBy("downloadAniListApolloSchemaFromIntrospection")
    }
}

tasks["generateAniListApolloSources"].dependsOn("downloadAniListSchema")

dependencies {
    api("com.apollographql.apollo3:apollo-runtime:3.7.4")
    api(project(":modules:android-utils"))
    api(project(":modules:entry"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-compiler:2.44.2")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.compose.material:material-icons-core:1.4.0-alpha05")
    implementation("androidx.compose.material:material-icons-extended:1.4.0-alpha05")

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