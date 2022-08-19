package com.thekeeperofpie.artistalleydatabase.json_schema

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the

@Suppress("unused")
class JsonSchemaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create<JsonSchemaExtension>(JsonSchemaExtension.NAME, project)
        val task = project.tasks.register<JsonSchemaTask>("generateJsonSchemaClasses") {
            group = "build"
            extension = project.the()
        }
        project.tasks.findByName("compileKotlin")!!.dependsOn(task)
    }
}