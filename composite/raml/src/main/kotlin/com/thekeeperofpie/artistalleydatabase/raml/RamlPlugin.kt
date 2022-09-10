package com.thekeeperofpie.artistalleydatabase.raml

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

@Suppress("unused")
class RamlPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.configurations.getByName("api").dependencies.apply {
            this += project.dependencies
                .create("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
            this += project.dependencies.project(":modules:web-infra")
        }

        project.pluginManager.apply("com.thekeeperofpie.artistalleydatabase.json_schema")

        project.extensions.create<RamlExtension>(RamlExtension.NAME, project)
        val task = project.tasks.register<RamlTask>("generateRamlClasses") {
            group = "build"
            dependsOn += "generateJsonSchemaClasses"
            extension = project.the()
            jsonSchemaExtension = project.the()
        }

        project.tasks.apply {
            findByName("assemble")?.dependsOn(task)
            findByName("compileDebugKotlin")?.dependsOn(task)
            findByName("compileReleaseKotlin")?.dependsOn(task)
        }

        project.afterEvaluate {
            val generatedSrcDir = project.the<RamlExtension>().generatedOutputDir.get().asFile
            project.kotlinExtension.sourceSets.getByName("main")
                .kotlin.srcDir(generatedSrcDir)
            project.extensions.findByType<IdeaModel>()
                ?.module?.generatedSourceDirs
                ?.add(generatedSrcDir)
        }
    }
}