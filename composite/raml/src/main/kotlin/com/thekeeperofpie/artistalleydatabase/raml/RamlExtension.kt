package com.thekeeperofpie.artistalleydatabase.raml

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.property

abstract class RamlExtension(project: Project) {

    companion object {
        internal val NAME = this::class.qualifiedName!!
    }

    @get:Input
    val baseUrl = project.objects.property<String>()

    @get:Input
    val modelPackageName = project.objects.property<String>()

    @get:OutputDirectory
    val generatedOutputDir: DirectoryProperty = project.objects.directoryProperty()

    init {
        generatedOutputDir.convention(
            project.layout.buildDirectory.dir("generated/source/raml"))
        modelPackageName.convention("com.thekeeperofpie.artistalleydatabase.raml.generated")
    }
}