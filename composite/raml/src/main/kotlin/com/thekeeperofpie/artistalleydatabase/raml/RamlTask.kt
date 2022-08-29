package com.thekeeperofpie.artistalleydatabase.raml

import com.charleskorn.kaml.Yaml
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.thekeeperofpie.artistalleydatabase.composite.utils.Utils
import com.thekeeperofpie.artistalleydatabase.composite.utils.Utils.toClassName
import com.thekeeperofpie.artistalleydatabase.composite.utils.Utils.toFunctionName
import com.thekeeperofpie.artistalleydatabase.composite.utils.Utils.toPropertyName
import com.thekeeperofpie.artistalleydatabase.json_schema.JsonSchemaExtension
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.util.prefixIfNot
import java.io.BufferedReader
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

@CacheableTask
open class RamlTask : DefaultTask() {

    @get:Input
    lateinit var extension: RamlExtension

    @get:Input
    lateinit var jsonSchemaExtension: JsonSchemaExtension

    @TaskAction
    fun run() {
        val raml = javaClass.classLoader.getResourceAsStream("api.raml")!!
            .use { it.bufferedReader().use(BufferedReader::readText) }
            .let(Yaml.default::parseToYamlNode)
            .let(::YamlWrapper)

        val baseUrl = extension.baseUrl.get()
        val className = raml["title"]!!.asString().toClassName()
        val classBuilder = TypeSpec.classBuilder(className)
            .addProperty(
                PropertySpec.builder(
                    "json",
                    Json::class,
                    KModifier.PRIVATE
                ).apply {
                    initializer(
                        CodeBlock.builder()
                            .beginControlFlow("Json")
                            .addStatement("isLenient = true")
                            .addStatement("ignoreUnknownKeys = true")
                            .endControlFlow()
                            .build()
                    )
                }
                    .build()
            )
            .addProperty(
                PropertySpec.builder(
                    "webApi",
                    ClassName("com.thekeeperofpie.artistalleydatabase.web_infra", "WebApi"),
                    KModifier.PRIVATE
                )
                    .initializer("WebApi(json, %S)", baseUrl)
                    .build()
            )

        val schemas = raml["schemas"]!!.items.associate {
            val (type, value) = it.entries.entries.first()
            val insecureUrl = value.innerNode.asString()
            val url = if (!insecureUrl.startsWith("https")) {
                insecureUrl.removePrefix("http").prefixIfNot("https")
            } else insecureUrl
            val customClassName = jsonSchemaExtension.urlsCustomNames.get()[url]
            val classSimpleName = customClassName ?: Utils.jsonUrlToClassName(url)
            type to ClassName(
                "com.thekeeperofpie.artistalleydatabase.json_schema.generated",
                classSimpleName
            )
        }

        val existingSignatures = mutableSetOf<List<*>>()

        raml.entries
            .filter { it.key.startsWith("/") }
            .forEach { (path, yaml) ->
                val comment = yaml["displayName"]?.asString()
                val get = yaml["get"] ?: return@forEach

                data class ParameterInfo(
                    val serializedName: String,
                    val propertyName: String,
                    val required: Boolean,
                    val description: String,
                    val type: KType,
                    val defaultValue: Any?,
                )

                fun typeAndDefaultValue(typeName: String?) = when (typeName) {
                    "string" -> {
                        String::class to null
                    }
                    "boolean" -> {
                        Boolean::class to false
                    }
                    "integer" -> {
                        Integer::class to null
                    }
                    else ->
                        throw IllegalArgumentException("Type name $typeName not supported")
                }

                fun FunSpec.Builder.addParameters(parameters: List<ParameterInfo>) {
                    parameters.forEach { (_, propertyName, required, description, type,
                                             defaultValue) ->
                        addParameter(
                            ParameterSpec.builder(propertyName, type.asTypeName())
                                .apply {
                                    if (description.isNotBlank()) {
                                        addKdoc("%L", description)
                                    }
                                    if (!required) {
                                        defaultValue("%L", defaultValue)
                                    }
                                }
                                .build()
                        )
                    }
                }

                val uriParameters = (yaml["uriParameters"]?.entries ?: emptyMap())
                    .entries.map { (name, value) ->
                        val required = value["required"]?.asBoolean() ?: false
                        val description = value["description"]?.asString().orEmpty()
                        val (type, defaultValue) = typeAndDefaultValue(value["type"]?.asString())
                        ParameterInfo(
                            serializedName = name,
                            propertyName = name.toPropertyName(),
                            required = required,
                            description = description,
                            type = type.starProjectedType.withNullability(!required),
                            defaultValue = defaultValue,
                        )
                    }

                val queryParameters = (get["queryParameters"]?.entries ?: emptyMap())
                    .entries.map { (name, value) ->
                        val required = value["required"]?.asBoolean() ?: false
                        val description = value["description"]?.asString().orEmpty()
                        val (type, defaultValue) = typeAndDefaultValue(value["type"]?.asString())
                        ParameterInfo(
                            serializedName = name,
                            propertyName = name.toPropertyName(),
                            required = required,
                            description = description,
                            type = type.starProjectedType.withNullability(!required),
                            defaultValue = defaultValue,
                        )
                    }

                val additionalParameters = mutableListOf<String>()
                fun FunSpec.Builder.buildMapOf(name: String, parameters: List<ParameterInfo>) {
                    if (parameters.isEmpty()) return
                    val statements = parameters.map { (serializedName, propertyName, _, _, type) ->
                        val toString = ".toString()"
                            .takeUnless {
                                type.isSubtypeOf(
                                    String::class.starProjectedType.withNullability(
                                        type.isMarkedNullable
                                    )
                                )
                            }
                            .orEmpty()

                        val nullableToken = "?"
                            .takeIf { toString.isNotEmpty() }
                            .takeIf { type.isMarkedNullable }
                            .orEmpty()

                        Triple(
                            "%S to %L$nullableToken$toString,",
                            serializedName,
                            propertyName
                        )
                    }

                    if (statements.size == 1) {
                        val statement = statements.single()
                        addStatement(
                            "val $name = mapOf(${statement.first.removeSuffix(",")})",
                            statement.second,
                            statement.third
                        )
                    } else {
                        addStatement("val $name = mapOf(")
                            .apply {
                                statements.forEach {
                                    addStatement(
                                        "  " + it.first,
                                        it.second,
                                        it.third
                                    )
                                }
                            }
                            .addStatement(")")
                    }

                    additionalParameters += "$name = $name"
                }

                val rawFunctionName = path.toFunctionName()
                val signature = listOf(
                    rawFunctionName,
                    uriParameters.map { it.type.withNullability(false) } +
                            queryParameters.map { it.type.withNullability(false) }
                )

                val realFunctionName =
                    jsonSchemaExtension.customPropertyNames.get()[rawFunctionName]
                        ?: if (!existingSignatures.add(signature)) {
                            rawFunctionName + (existingSignatures.count { it == signature } + 1)
                        } else rawFunctionName

                val funSpec = FunSpec.builder(realFunctionName)
                    .apply {
                        if (!comment.isNullOrBlank()) {
                            addKdoc(comment)
                        }
                    }
                    .addModifiers(KModifier.SUSPEND)

                funSpec.addParameters(uriParameters)
                funSpec.addParameters(queryParameters)
                funSpec.buildMapOf("uriParameters", uriParameters)
                funSpec.buildMapOf("queryParameters", queryParameters)

                val parameterString = additionalParameters
                    .takeIf { additionalParameters.isNotEmpty() }
                    ?.joinToString(prefix = ", ")
                    .orEmpty()

                val body = get["responses", "200", "body"] ?: return@forEach
                val jsonNode = body["application/json"]
                if (jsonNode == null) {
                    funSpec.addStatement("return webApi.getString(%S%L)", path, parameterString)
                } else {
                    val typeName = jsonNode["schema"]?.asString() ?: return@forEach
                    val type = schemas[typeName]!!
                    funSpec.returns(type)
                        .addStatement(
                            "return webApi.getObject<%T>(%S%L)",
                            type,
                            path,
                            parameterString
                        )
                }

                classBuilder.addFunction(funSpec.build())
            }

        FileSpec.builder("com.thekeeperofpie.artistalleydatabase.raml.generated", className)
            .addType(classBuilder.build())
            .build()
            .writeTo(extension.generatedOutputDir.asFile.get())

        extension.generatedOutputDir.asFileTree
            .filter { it.extension == "kt" }
            .forEach(Utils::reformatKotlinSource)
    }
}