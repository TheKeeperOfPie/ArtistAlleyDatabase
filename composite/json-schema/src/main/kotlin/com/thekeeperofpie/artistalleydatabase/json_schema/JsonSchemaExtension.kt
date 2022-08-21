package com.thekeeperofpie.artistalleydatabase.json_schema

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

abstract class JsonSchemaExtension(project: Project) {

    companion object {
        internal val NAME = this::class.qualifiedName!!
    }

    @get:Input
    val urls = project.objects.listProperty<String>()

    @get:Input
    val urlsCustomNames = project.objects.mapProperty<String, String>()

    @get:Input
    val modelPackageName = project.objects.property<String>()

    @get:Input
    val customPropertyNames = project.objects.mapProperty<String, String>()

    @get:OutputDirectory
    val schemaOutputDir: DirectoryProperty = project.objects.directoryProperty()

    @get:OutputDirectory
    val generatedOutputDir: DirectoryProperty = project.objects.directoryProperty()

    init {
        schemaOutputDir.convention(
            project.layout.buildDirectory.dir("generated/source/jsonSchema"))
        generatedOutputDir.convention(
            project.layout.buildDirectory.dir("generated/source/jsonSchemaModels"))
        modelPackageName.convention("com.thekeeperofpie.artistalleydatabase.json_schema.generated")
    }
}