package com.thekeeperofpie.artistalleydatabase.raml

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A simple functional test for the 'com.thekeeperofpie.artistalleydatabase.raml' plugin.
 */
class RamlPluginFunctionalTest {
    @get:Rule val tempFolder = TemporaryFolder()

    private fun getProjectDir() = tempFolder.root
    private fun getBuildFile() = getProjectDir().resolve("build.gradle")
    private fun getSettingsFile() = getProjectDir().resolve("settings.gradle")

    @Test fun `can run task`() {
        // Setup the test build
        getSettingsFile().writeText("")
        getBuildFile().writeText("""
            plugins {
                id('com.thekeeperofpie.artistalleydatabase.raml')
            }
            """
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(getProjectDir())
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("Hello from plugin 'com.thekeeperofpie.artistalleydatabase.raml'"))
    }
}
