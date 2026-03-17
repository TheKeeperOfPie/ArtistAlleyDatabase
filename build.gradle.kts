import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.burst.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.androidx.room).apply(false)
    alias(libs.plugins.app.cash.burst).apply(false)
    alias(libs.plugins.app.cash.sqldelight).apply(false)
    alias(libs.plugins.com.google.devtools.ksp).apply(false)
    alias(libs.plugins.org.jetbrains.compose).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.compose).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization).apply(false)
    id(libs.plugins.com.android.application.get().pluginId).apply(false)
    id(libs.plugins.com.android.kotlin.multiplatform.library.get().pluginId).apply(false)
    id(libs.plugins.org.jetbrains.kotlin.android.get().pluginId).apply(false)

    alias(libs.plugins.com.github.ben.manes.versions)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = true
    outputFormatter = "html"
}

tasks.register("copyGitHooks", Copy::class) {
    from(File(rootProject.rootDir, "scripts/git/pre-commit"))
    into(File(rootProject.rootDir, ".git/hooks"))
    filePermissions {
        user {
            read = true
            execute = true
        }
    }
}

val verificationMetadataFile = File(rootProject.rootDir, "gradle/verification-metadata.xml")
tasks.register("recopyVerificationMetadata") {
    verificationMetadataFile.resolveSibling("verification-metadata-base.xml")
        .copyTo(verificationMetadataFile, overwrite = true)
}

tasks.register("generateVerificationMetadata") {
    dependsOn("recopyVerificationMetadata")
    dependsOn("help")
    dependsOn("dependencyUpdates")
//    dependsOn("createModuleGraph")
    // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/1185
//    dependsOn("buildHealth")
    finalizedBy(":app:licenseReleaseReport")
}

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }
            onUsedTransitiveDependencies {
                severity("ignore")
            }
        }
    }
}
