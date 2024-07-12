plugins {
    id("jvm-library")
    alias(libs.plugins.com.apollographql.apollo3.external)
}

// Need to manually remove some types, as this downloads the default type definitions,
// which will conflict with codegen
val aniListSchemaFile: File = project.file("src/main/graphql/anilist/schema.graphqls")
apollo {
    service("aniList") {
        packageName.set("com.anilist")
        schemaFiles.from(aniListSchemaFile)
        introspection {
            endpointUrl.set("https://graphql.anilist.co")
            schemaFile.set(aniListSchemaFile)
        }
        codegenModels.set("responseBased")
        decapitalizeFields.set(true)

        plugin(project(":modules:apollo"))
    }
}

if (!aniListSchemaFile.exists()) {
    tasks.findByName("generateAniListApolloSources")!!
        .dependsOn("downloadAniListApolloSchemaFromIntrospection")
}

dependencies {
    api(libs.apollo.runtime)
    implementation(libs.jetBrainsCompose.runtime)
}
