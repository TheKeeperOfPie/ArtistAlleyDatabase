@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.gradle.kotlin.kotlin-dsl") version "4.0.2"
    kotlin("plugin.serialization") version "1.8.0"
}

kotlin {
    jvmToolchain(18)
    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(project(":utils"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest()
        }

        @Suppress("UNUSED_VARIABLE")
        val functionalTest by registering(JvmTestSuite::class) {
            useKotlinTest()

            dependencies {
                implementation(project)
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) } 
                }
            }
        }
    }
}

gradlePlugin {
    @Suppress("UNUSED_VARIABLE")
    val plugin by plugins.creating {
        id = "com.thekeeperofpie.artistalleydatabase.json_schema"
        implementationClass = "com.thekeeperofpie.artistalleydatabase.json_schema.JsonSchemaPlugin"
    }
}

gradlePlugin.testSourceSets(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
//    dependsOn(testing.suites.named("functionalTest"))
}
