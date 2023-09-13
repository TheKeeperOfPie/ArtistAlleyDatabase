import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.compiler.hooks.ApolloCompilerKotlinHooks.FileInfo
import com.apollographql.apollo3.compiler.hooks.DefaultApolloCompilerKotlinHooks
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.com.apollographql.apollo3.external)
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

secrets {
    propertiesFileName = "secrets.properties"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anilist"
}

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

        @Suppress("OPT_IN_USAGE")
        compilerKotlinHooks.set(listOf(DefaultValuesApolloHooks, ComposeStableApolloHooks))
    }
}

@ApolloExperimental
object ComposeStableApolloHooks : DefaultApolloCompilerKotlinHooks() {
    override val version = "ComposeStableApolloHooks.2"

    private val stableAnnotation = ClassName("androidx.compose.runtime", "Stable")

    override fun postProcessFiles(files: Collection<FileInfo>) = files.map {
        it.copy(fileSpec = it.fileSpec
            .toBuilder()
            .apply {
                members.replaceAll {
                    (it as? TypeSpec)?.addAnnotation() ?: it
                }
            }
            .build()
        )
    }

    private fun TypeSpec.addAnnotation() = toBuilder()
        .addAnnotation(stableAnnotation)
        .build()
}

@ApolloExperimental
object DefaultValuesApolloHooks : DefaultApolloCompilerKotlinHooks() {
    override val version = "DefaultValuesApolloHooks.0"

    override fun postProcessFiles(files: Collection<FileInfo>): Collection<FileInfo> {
        return files.map {
            it.copy(fileSpec = it.fileSpec
                .toBuilder()
                .apply {
                    members.replaceAll {
                        (it as? TypeSpec)?.addDefaultValueToNullableProperties() ?: it
                    }
                }
                .build()
            )
        }
    }

    private fun TypeSpec.addDefaultValueToNullableProperties(): TypeSpec {
        return toBuilder()
            .apply {
                if (modifiers.contains(KModifier.DATA)) {
                    primaryConstructor(
                        primaryConstructor!!.toBuilder()
                            .apply {
                                parameters.replaceAll { param ->
                                    val defaultValue = if (param.type.isNullable) {
                                        "null"
                                    } else when (param.type) {
                                        com.squareup.kotlinpoet.ANY -> "Unit"
                                        com.squareup.kotlinpoet.ARRAY -> "emptyArray()"
                                        com.squareup.kotlinpoet.UNIT -> "Unit"
                                        com.squareup.kotlinpoet.BOOLEAN -> "false"
                                        com.squareup.kotlinpoet.BYTE,
                                        com.squareup.kotlinpoet.SHORT,
                                        com.squareup.kotlinpoet.INT,
                                        -> "-1"
                                        com.squareup.kotlinpoet.LONG -> "-1L"
                                        com.squareup.kotlinpoet.CHAR -> "'-'"
                                        com.squareup.kotlinpoet.FLOAT -> "-1f"
                                        com.squareup.kotlinpoet.DOUBLE -> "-1.0"
                                        com.squareup.kotlinpoet.STRING,
                                        com.squareup.kotlinpoet.CHAR_SEQUENCE,
                                        -> "\"Default\""
                                        com.squareup.kotlinpoet.NOTHING -> "Nothing"
                                        com.squareup.kotlinpoet.NUMBER -> "-1"
                                        com.squareup.kotlinpoet.ITERABLE,
                                        com.squareup.kotlinpoet.COLLECTION,
                                        com.squareup.kotlinpoet.LIST,
                                        -> "emptyList()"
                                        com.squareup.kotlinpoet.SET -> "emptySet()"
                                        com.squareup.kotlinpoet.MAP -> "emptyMap()"
                                        com.squareup.kotlinpoet.MUTABLE_ITERABLE,
                                        com.squareup.kotlinpoet.MUTABLE_COLLECTION,
                                        com.squareup.kotlinpoet.MUTABLE_LIST,
                                        -> "mutableListOf()"
                                        com.squareup.kotlinpoet.MUTABLE_SET -> "mutableSetOf()"
                                        com.squareup.kotlinpoet.MUTABLE_MAP -> "mutableMapOf()"
                                        com.squareup.kotlinpoet.BOOLEAN_ARRAY -> "booleanArrayOf()"
                                        com.squareup.kotlinpoet.BYTE_ARRAY -> "byteArrayOf()"
                                        com.squareup.kotlinpoet.CHAR_ARRAY -> "charArrayOf()"
                                        com.squareup.kotlinpoet.SHORT_ARRAY -> "shortArrayOf()"
                                        com.squareup.kotlinpoet.INT_ARRAY -> "intArrayOf()"
                                        com.squareup.kotlinpoet.LONG_ARRAY -> "longArrayOf()"
                                        com.squareup.kotlinpoet.FLOAT_ARRAY -> "floatArrayOf()"
                                        com.squareup.kotlinpoet.DOUBLE_ARRAY -> "doubleArrayOf()"
                                        com.squareup.kotlinpoet.U_BYTE,
                                        com.squareup.kotlinpoet.U_SHORT,
                                        com.squareup.kotlinpoet.U_INT,
                                        com.squareup.kotlinpoet.U_LONG,
                                        -> "0u"
                                        com.squareup.kotlinpoet.U_BYTE_ARRAY -> "ubyteArrayOf()"
                                        com.squareup.kotlinpoet.U_SHORT_ARRAY -> "ushortArrayOf()"
                                        com.squareup.kotlinpoet.U_INT_ARRAY -> "uintArrayOf()"
                                        com.squareup.kotlinpoet.U_LONG_ARRAY -> "ulongArrayOf()"
                                        else -> return@replaceAll param
                                    }

                                    param.toBuilder()
                                        .defaultValue(CodeBlock.of(defaultValue))
                                        .build()
                                }
                            }
                            .build()
                    )
                }

                // Recurse on nested types
                typeSpecs.replaceAll { it.addDefaultValueToNullableProperties() }
            }
            .build()
    }
}

if (!aniListSchemaFile.exists()) {
    tasks.findByName("generateAniListApolloSources")!!
        .dependsOn("downloadAniListApolloSchemaFromIntrospection")
}

dependencies {
    api(project(":modules:android-utils"))
    implementation(project(":modules:compose-utils"))
    api(project(":modules:entry"))
    api(project(":modules:network-utils"))

    api(libs.apollo.runtime)
    api(libs.apollo.normalized.cache)
    api(libs.apollo.normalized.cache.sqlite)

    implementation(libs.kotlinx.serialization.json)
    api(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(kspProcessors.dagger.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)
    implementation(libs.material.icons.core)
    implementation(libs.material.icons.extended)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.activity.compose)
    implementation(libs.androidx.browser)
    api(libs.androidx.security.crypto)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    api(libs.moshi.kotlin)
    ksp(kspProcessors.moshi.kotlin.codegen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.test)
}
