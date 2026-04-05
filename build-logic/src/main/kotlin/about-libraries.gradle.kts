import com.mikepenz.aboutlibraries.plugin.AboutLibrariesExtension

plugins {
    id("library-compose")
    id("com.mikepenz.aboutlibraries.plugin")
}

// Doesn't attach the AboutLibrariesProvider which is needed in source code
project.configure<AboutLibrariesExtension> {
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
        prettyPrint = true
    }
    license {
        strictMode = com.mikepenz.aboutlibraries.plugin.StrictMode.FAIL
        allowedLicenses.addAll("Apache-2.0", "BSD-2-Clause", "BSD-3-Clause", "MIT")
        allowedLicensesMap = mapOf(
            "EPL-1.0" to listOf("junit"),
            "Eclipse Public License - v 1.0" to listOf("ch.qos.logback"),
            "GNU Lesser General Public License" to listOf("ch.qos.logback"),
            "LGPL-2.1-or-later" to listOf("net.java.dev.jna"),
            "Unicode-3.0" to listOf("com.ibm.icu"),
        )
    }
}
