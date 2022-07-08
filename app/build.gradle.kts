plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp") version "1.7.0-1.0.6"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.apollographql.apollo3") version "3.3.2"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase"
    compileSdk = 32

    defaultConfig {
        applicationId = "com.thekeeperofpie.artistalleydatabase"
        minSdk = 31
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard/"
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0-dev-k1.7.0-53370d83bb1"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val aniListSchemaFile = file("src/main/graphql/anilist/schema.graphqls")
apollo {
    service("aniList") {
        packageName.set("com.anilist")
        introspection {
            endpointUrl.set("https://graphql.anilist.co")
            schemaFile.set(aniListSchemaFile)
        }
    }
}

if (!aniListSchemaFile.exists()) {
    tasks["generateAniListApolloSources"].dependsOn("downloadAniListApolloSchemaFromIntrospection")
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    implementation("androidx.navigation:navigation-compose:2.5.0")

    implementation("com.google.dagger:hilt-android:2.42")
    kapt("com.google.dagger:hilt-compiler:2.42")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")

    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0")
    implementation("androidx.activity:activity-compose:1.5.0")
    implementation("androidx.compose.ui:ui:1.3.0-alpha01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.0-alpha01")
    implementation("androidx.compose.material:material-icons-core:1.3.0-alpha01")
    implementation("androidx.compose.material:material-icons-extended:1.3.0-alpha01")
    implementation("androidx.compose.material3:material3:1.0.0-alpha14")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    implementation("androidx.paging:paging-runtime:3.2.0-alpha01")
    implementation("androidx.paging:paging-compose:1.0.0-alpha15")

    implementation("androidx.room:room-runtime:2.5.0-alpha02")
    ksp("androidx.room:room-compiler:2.5.0-alpha02")
    implementation("androidx.room:room-ktx:2.5.0-alpha02")
    testImplementation("androidx.room:room-testing:2.5.0-alpha02")
    implementation("androidx.room:room-paging:2.5.0-alpha02")

    implementation("io.coil-kt:coil:2.1.0")
    implementation("io.coil-kt:coil-compose:2.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.3.0-alpha01")
    debugImplementation("androidx.compose.ui:ui-tooling:1.3.0-alpha01")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.3.0-alpha01")

    implementation("com.mxalbert.sharedelements:shared-elements:0.1.0-SNAPSHOT")

    implementation("androidx.work:work-runtime:2.7.1")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    androidTestImplementation("androidx.work:work-testing:2.7.1")

    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    implementation("com.google.accompanist:accompanist-drawablepainter:0.23.1")
    implementation("com.google.accompanist:accompanist-pager:0.24.10-beta")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.24.10-beta")

    implementation("com.apollographql.apollo3:apollo-runtime:3.3.2")
}