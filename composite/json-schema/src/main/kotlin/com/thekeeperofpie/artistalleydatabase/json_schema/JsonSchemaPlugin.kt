package com.thekeeperofpie.artistalleydatabase.json_schema

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

@Suppress("unused")
class JsonSchemaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.configurations.getByName("api").dependencies += project.dependencies
            .create("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

        project.extensions.create<JsonSchemaExtension>(JsonSchemaExtension.NAME, project)
        val task = project.tasks.register<JsonSchemaTask>("generateJsonSchemaClasses") {
            group = "build"
            extension = project.the()
        }
        project.tasks.findByName("compileKotlin")!!.dependsOn(task)

        project.afterEvaluate {
            val generatedSrcDir = project.the<JsonSchemaExtension>().generatedOutputDir.get().asFile
            project.kotlinExtension.sourceSets.getByName("main")
                .kotlin.srcDir(generatedSrcDir)
            project.extensions.findByType<IdeaModel>()
                ?.module?.generatedSourceDirs
                ?.add(generatedSrcDir)
        }
    }
}