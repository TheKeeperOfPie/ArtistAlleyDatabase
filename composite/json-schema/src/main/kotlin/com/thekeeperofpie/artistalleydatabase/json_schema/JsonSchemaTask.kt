package com.thekeeperofpie.artistalleydatabase.json_schema

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.thekeeperofpie.artistalleydatabase.json_schema.models.JsonSchema
import com.thekeeperofpie.artistalleydatabase.json_schema.models.JsonSchemaType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.util.prefixIfNot
import java.net.URL
import java.util.Locale

open class JsonSchemaTask : DefaultTask() {

    companion object {
        private val WHITESPACE_OR_UNDERLINE = Regex("[\\s_]")
    }

    @get:Input
    lateinit var extension: JsonSchemaExtension

    private val classCache = mutableMapOf<String, ClassWrapper>()

    @TaskAction
    fun run() {
        extension.generatedOutputDir.asFile.get().deleteRecursively()
        extension.urls.get().forEach {
            parseSchema(it, null)
        }
        extension.urlsCustomNames.get().forEach { (url, customName) ->
            parseSchema(url, customName)
        }

        extension.generatedOutputDir.asFileTree
            .filter { it.extension == "kt" }
            .forEach {
                // Strip redundant public modifiers
                it.writeText(
                    it.readText()
                        .replace("public val", "val")
                        .replace("public data class", "data class")
                )
            }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun json() = Json {
        isLenient = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(JsonSchemaType::class) {
                subclass(JsonSchemaType.Number::class)
                subclass(JsonSchemaType.StringType::class)
                subclass(JsonSchemaType.Array::class)
                subclass(JsonSchemaType.Object::class)
                defaultDeserializer { JsonSchemaType.StringType.serializer() }
            }
        }
    }

    private fun fetchSchema(json: Json, url: String): JsonSchema {
        val realUrl = if (url.startsWith("http") && !url.startsWith("https")) {
            url.removePrefix("http").prefixIfNot("https")
        } else url
        return URL(realUrl).readText()
            .let<String, JsonSchema>(json::decodeFromString)
            .apply { this.url = url }
    }

    private fun parseSchema(url: String, customName: String?) {
        val json = json()
        val schema = fetchSchema(json, url)
        buildClassFromProperties(
            json = json,
            schema = schema,
            className = customName ?: schema.name.toClassName(),
            comment = schema.title,
            properties = schema.properties,
            patternProperties = emptyMap()
        )
    }

    private fun resolveRef(
        json: Json,
        schema: JsonSchema,
        name: String,
        jsonElement: JsonElement
    ): ClassWrapper? {
        val ref: String? = jsonElement.jsonObject["\$ref"]?.jsonPrimitive?.contentOrNull
        if (ref != null) {
            val (refSchema, definitionName, type) = resolveRefType(json, schema, ref) ?: return null
            return resolveClass(json, refSchema, definitionName, type)
        } else {
            val type: JsonSchemaType = json.decodeFromJsonElement(jsonElement)
            return resolveClass(json, schema, name, type)
        }
    }

    private fun resolveRefType(
        json: Json,
        schema: JsonSchema,
        ref: String
    ): Triple<JsonSchema, String, JsonSchemaType>? {
        if (ref.startsWith("#/definitions/") || ref.startsWith("#definitions/")) {
            val definitionName = ref.substringAfter("definitions/")
            val type = schema.definitions[definitionName] ?: return null
            return Triple(schema, definitionName, type)
        } else {
            val path = ref.substringBefore("#")
            if (path.isBlank()) return null

            val refSchema = if (path.startsWith("http")) {
                fetchSchema(json, path)
            } else {
                fetchSchema(json, "${schema.url.substringBeforeLast("/")}/$path")
            }

            val definitionName = ref.substringAfter("definitions/")
            val type = refSchema.definitions[definitionName] ?: return null
            return Triple(refSchema, definitionName, type)
        }
    }

    private fun resolveClass(
        json: Json,
        schema: JsonSchema,
        name: String,
        type: JsonSchemaType
    ): ClassWrapper? {
        when (type) {
            is JsonSchemaType.Array -> {
                val items = type.items?.jsonObject ?: return null
                val ref = resolveRef(json, schema, name, items)
                if (ref != null) return ClassWrapper.Array(type.title, ref)

                val itemType: JsonSchemaType = json.decodeFromJsonElement(items)
                val itemClass = resolveClass(json, schema, name, itemType) ?: ClassWrapper.String(
                    itemType.title
                )
                return ClassWrapper.Array(type.title, itemClass)
            }
            is JsonSchemaType.Object -> {
                var className = name.toClassName()
                if (className == schema.name) {
                    className += "_"
                }

                val cached = classCache[className]
                if (cached != null) {
                    return cached
                }

                val allOf = type.allOf
                val properties = if (allOf.isNotEmpty()) {
                    allOf.mapNotNull {
                        val ref = it.jsonObject["\$ref"]?.jsonPrimitive?.contentOrNull
                        val innerType = if (ref != null) {
                            resolveRefType(json, schema, ref)?.third
                        } else {
                            json.decodeFromJsonElement<JsonSchemaType>(it)
                        } as? JsonSchemaType.Object
                        innerType?.properties
                            ?.takeIf { innerType.patternProperties.isEmpty() }
                    }
                        .flatMap { it.entries }
                        .associate { it.toPair() }
                } else type.properties

                return buildClassFromProperties(
                    json = json,
                    schema = schema,
                    className = className,
                    comment = type.title,
                    properties = properties,
                    patternProperties = type.patternProperties,
                ).also { classCache[className] = it }
            }
            is JsonSchemaType.StringType -> return ClassWrapper.String(type.title)
            is JsonSchemaType.Number -> return ClassWrapper.Number(type.title)
        }
    }

    private fun buildClassFromProperties(
        json: Json,
        schema: JsonSchema,
        className: String,
        comment: String,
        properties: Map<String, JsonElement>,
        patternProperties: Map<String, JsonElement>,
    ): ClassWrapper {
        if (patternProperties.isNotEmpty()) {
            return ClassWrapper.Map(comment, ClassWrapper.String(""), ClassWrapper.String(""))
        }

        @Suppress("UNCHECKED_CAST")
        val resolvedProperties = properties
            .mapKeys { PropertyWrapper(it.key) }
            .mapValues { (name, element) ->
                resolveRef(json, schema, name.serializedName, element)
            }
            .filterValues { it != null } as Map<PropertyWrapper, ClassWrapper>

        val packageName = extension.modelPackageName.get()
        val directory = extension.generatedOutputDir.asFile.get()
        FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .apply {
                                resolvedProperties.forEach { (name, classSpec) ->
                                    addParameter(
                                        ParameterSpec.builder(
                                            name.propertyName,
                                            classSpec.typeName
                                        )
                                            .addKdoc(classSpec.comment)
                                            .apply {
                                                if (name.serializedName != name.propertyName) {
                                                    addAnnotation(
                                                        AnnotationSpec.builder(SerialName::class)
                                                            .addMember(
                                                                "value = %S",
                                                                name.serializedName
                                                            )
                                                            .build()
                                                    )
                                                }
                                            }
                                            .build()
                                    )
                                }
                            }
                            .build()
                    )
                    .apply {
                        resolvedProperties.forEach { (name, classSpec) ->
                            addProperty(
                                PropertySpec.builder(
                                    name.propertyName,
                                    classSpec.typeName
                                )
                                    .addKdoc(classSpec.comment)
                                    .initializer(name.propertyName)
                                    .build()
                            )
                        }
                    }
                    .build()
            )
            .build()
            .writeTo(directory)
        return ClassWrapper.Object(comment, ClassName(packageName, className))
    }

    private fun String.toClassName() = split(WHITESPACE_OR_UNDERLINE)
        .joinToString(separator = "", transform = String::capitalized)

    sealed class ClassWrapper {
        abstract val comment: kotlin.String
        abstract val typeName: TypeName

        data class Array(
            override val comment: kotlin.String,
            val itemType: ClassWrapper
        ) : ClassWrapper() {
            override val typeName = ClassName("kotlin.collections", "List")
                .parameterizedBy(itemType.typeName)
        }

        data class Map(
            override val comment: kotlin.String,
            val keyType: ClassWrapper,
            val valueType: ClassWrapper
        ) : ClassWrapper() {
            override val typeName = ClassName("kotlin.collections", "Map")
                .parameterizedBy(keyType.typeName, valueType.typeName)
        }

        data class String(override val comment: kotlin.String) : ClassWrapper() {
            override val typeName = ClassName("kotlin", "String")
        }

        data class Number(override val comment: kotlin.String) : ClassWrapper() {
            override val typeName = ClassName("kotlin", "Int")
        }

        data class Object(
            override val comment: kotlin.String,
            val className: ClassName
        ) : ClassWrapper() {
            override val typeName = className
        }
    }

    inner class PropertyWrapper(
        val serializedName: String,
    ) {
        val propertyName: String by lazy {
            extension.customPropertyNameMap.getting(serializedName).getOrElse(
                serializedName.split(WHITESPACE_OR_UNDERLINE)
                    .joinToString(separator = "", transform = String::capitalized)
                    .decapitalize(Locale.getDefault())
            )
        }
    }
}