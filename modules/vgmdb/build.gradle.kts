@file:Suppress("UnstableApiUsage")

import com.thekeeperofpie.artistalleydatabase.json_schema.JsonSchemaExtension
import com.thekeeperofpie.artistalleydatabase.raml.RamlExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.7.20-Beta-1.0.6"
    kotlin("plugin.serialization") version "1.7.20-Beta"
    id("com.thekeeperofpie.artistalleydatabase.raml")
    id("de.mannodermaus.android-junit5")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.vgmdb"
    compileSdk = 33

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        testInstrumentationRunnerArguments["runnerBuilder"] =
            "de.mannodermaus.junit5.AndroidJUnit5Builder"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packagingOptions {
        resources {
            merges += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation(project(":modules:android-utils"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-compiler:2.43.2")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.room:room-runtime:2.5.0-alpha03")
    ksp("androidx.room:room-compiler:2.5.0-alpha03")
    implementation("androidx.room:room-ktx:2.5.0-alpha03")
    testImplementation("androidx.room:room-testing:2.5.0-alpha03")
    implementation("androidx.room:room-paging:2.5.0-alpha03")

    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    implementation("it.skrape:skrapeit:1.2.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("com.google.truth:truth:1.1.3")

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
}

sourceSets {
    forEach {
        if (it !is KotlinSourceSet) return@forEach
        it.kotlin {
            srcDir(project.layout.buildDirectory.file("generated/source/jsonSchemaModels"))
            srcDir(project.layout.buildDirectory.file("generated/source/raml"))
        }
    }
}

configure<JsonSchemaExtension> {
    urls.set(
        listOf(
            "https://vgmdb.info/schema/artist.json",
            "https://vgmdb.info/schema/album.json",
            "https://vgmdb.info/schema/artist.json",
            "https://vgmdb.info/schema/event.json",
            "https://vgmdb.info/schema/org.json",
            "https://vgmdb.info/schema/product.json",
            "https://vgmdb.info/schema/release.json",
            "https://vgmdb.info/schema/search.json",
            "https://vgmdb.info/schema/sellers.json",
            "https://vgmdb.info/schema/recent.json",
        )
    )
    urlsCustomNames.set(
        mapOf(
            "https://vgmdb.info/schema/albumlist.json" to "AlbumList",
            "https://vgmdb.info/schema/artistlist.json" to "ArtistList",
            "https://vgmdb.info/schema/eventlist.json" to "EventList",
            "https://vgmdb.info/schema/orglist.json" to "OrgList",
            "https://vgmdb.info/schema/productlist.json" to "ProductList",
        )
    )
    @Suppress("SpellCheckingInspection")
    customPropertyNames.set(
        mapOf(
            "deathdate" to "deathDate",
            "albumlist" to "albumList",
            "artistlist" to "artistList",
            "productlist" to "productList",
            "orglist" to "orgList",
            "eventlist" to "eventList",
        )
    )
}

configure<RamlExtension> {
    baseUrl.set("https://vgmdb.info")
}