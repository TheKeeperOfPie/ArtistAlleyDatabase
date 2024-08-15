plugins {
    id("module-library")

    // Gradle --scan
    id("com.gradle.build-scan") version "3.17.6" apply false
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
    val androidTools = "31.7.0-alpha06"
    debugCompileOnly("com.android.tools.emulator:proto:$androidTools")
    debugCompileOnly("com.android.tools.utp:utp-common:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-device-provider-ddmlib:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-device-provider-gradle:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-additional-test-output:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-apk-installer:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-coverage:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-device-info:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-device-info-proto:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-emulator-control:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-logcat:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-host-retention:$androidTools")
    debugCompileOnly("com.android.tools.utp:android-test-plugin-result-listener-gradle:$androidTools")

    val googleTesting = "0.0.9-alpha02"
    debugCompileOnly("com.google.testing.platform:android-device-provider-local:$googleTesting")
    debugCompileOnly("com.google.testing.platform:android-driver-instrumentation:$googleTesting")
    debugCompileOnly("com.google.testing.platform:android-test-plugin:$googleTesting")
    debugCompileOnly("com.google.testing.platform:core:$googleTesting")
    debugCompileOnly("com.google.testing.platform:launcher:$googleTesting")

    // Gradle sync
    val groovyVersion = "3.0.21"
    debugCompileOnly("org.codehaus.groovy:groovy:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-ant:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-astbuilder:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-console:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-datetime:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-dateutil:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-groovydoc:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-json:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-nio:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-sql:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-templates:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-test:$groovyVersion")
    debugCompileOnly("org.codehaus.groovy:groovy-xml:$groovyVersion")
}
