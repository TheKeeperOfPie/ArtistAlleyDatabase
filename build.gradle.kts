import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

plugins {
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.com.google.devtools.ksp).apply(false)
    alias(libs.plugins.com.jaredsburrows.license).apply(false)
    alias(libs.plugins.org.jetbrains.compose).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization).apply(false)
    id(libs.plugins.com.android.application.get().pluginId).apply(false)
    id(libs.plugins.com.android.library.get().pluginId).apply(false)
    id(libs.plugins.com.autonomousapps.dependency.analysis.get().pluginId).apply(false)
    id(libs.plugins.org.jetbrains.kotlin.android.get().pluginId).apply(false)
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

    // TODO: Re-enable test-utils once it's fixed
    dependsOn(subprojects.filter { it.name != "test-utils" }
        .mapNotNull { it.tasks.findByName("assembleDebug") })
    dependsOn("dependencyUpdates")
    dependsOn("buildHealth")
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
