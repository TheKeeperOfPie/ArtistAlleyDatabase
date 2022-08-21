@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("org.gradle.kotlin.kotlin-dsl") version "2.4.1"
    kotlin("plugin.serialization") version "1.7.10"
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

dependencies {
    implementation(project(":json-schema"))
    implementation(project(":utils"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("com.charleskorn.kaml:kaml:0.47.0")
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
        id = "com.thekeeperofpie.artistalleydatabase.raml"
        implementationClass = "com.thekeeperofpie.artistalleydatabase.raml.RamlPlugin"
    }
}

gradlePlugin.testSourceSets(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
//    dependsOn(testing.suites.named("functionalTest"))
}
