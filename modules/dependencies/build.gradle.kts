plugins {
    id("module-library")

    // Gradle --scan
    id("com.gradle.build-scan") version "3.13.4" apply false
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.dependencies"
}

/**
 * This file stores dependencies for tasks run during development that are not easily captured
 * by generateVerificationMetadata. The explicit declarations here allow that task to generate
 * the checksums for these dependencies.
 *
 * The alternative is to ignore these specific groups in verification-metadata.xml, but until this
 * problem becomes too annoying, doing it this way is technically more correct.
 */
dependencies {
    // Android Studio instrumentation testing
    debugCompileOnly("com.android.tools.emulator:proto:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-device-provider-ddmlib:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-device-provider-gradle:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-additional-test-output:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-apk-installer:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-coverage:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-device-info:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-device-info-proto:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-emulator-control:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-logcat:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-retention:31.2.0-alpha12")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-result-listener-gradle:31.2.0-alpha12")
    debugCompileOnly("com.google.testing.platform:android-device-provider-local:0.0.8-alpha08")
    debugCompileOnly("com.google.testing.platform:android-driver-instrumentation:0.0.8-alpha08")
    debugCompileOnly("com.google.testing.platform:android-test-plugin:0.0.8-alpha08")
    debugCompileOnly("com.google.testing.platform:core:0.0.8-alpha08")
    debugCompileOnly("com.google.testing.platform:launcher:0.0.8-alpha08")

    // Gradle sync
    debugCompileOnly("org.codehaus.groovy:groovy:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-ant:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-astbuilder:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-console:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-datetime:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-dateutil:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-groovydoc:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-json:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-nio:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-sql:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-templates:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-test:3.0.17")
    debugCompileOnly("org.codehaus.groovy:groovy-xml:3.0.17")
}
