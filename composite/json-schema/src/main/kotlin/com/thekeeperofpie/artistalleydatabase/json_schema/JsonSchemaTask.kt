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
import com.thekeeperofpie.artistalleydatabase.composite.utils.Utils
import com.thekeeperofpie.artistalleydatabase.composite.utils.Utils.toClassName
import com.thekeeperofpie.artistalleydatabase.composite.utils.Utils.toPropertyName
import com.thekeeperofpie.artistalleydatabase.json_schema.models.JsonSchema
import com.thekeeperofpie.artistalleydatabase.json_schema.models.JsonSchemaType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.util.prefixIfNot
import java.net.URL

@CacheableTask
open class JsonSchemaTask : DefaultTask() {

    companion object {
        private val FORCE_NULLABLE_NAMES = listOf(
            "category"
        )
    }

    @get:Input
    lateinit var extension: JsonSchemaExtension

    private val classCache = mutableMapOf<String, ClassWrapper>()

    @TaskAction
    fun run() {
        extension.urls.get().forEach {
            parseSchema(it, null)
        }
        extension.urlsCustomNames.get().forEach { (url, customName) ->
            parseSchema(url, customName)
        }

        extension.generatedOutputDir.asFileTree
            .filter { it.extension == "kt" }
            .forEach(Utils::reformatKotlinSource)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun json() = Json {
        isLenient = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(JsonSchemaType::class) {
                subclass(JsonSchemaType.Number::class)
                subclass(JsonSchemaType.Integer::class)
                subclass(JsonSchemaType.StringType::class)
                subclass(JsonSchemaType.Array::class)
                subclass(JsonSchemaType.Object::class)
                defaultDeserializer { JsonSchemaType.Unknown.serializer() }
            }
        }
    }

    private fun fetchSchema(json: Json, url: String): JsonSchema {
        val realUrl = URL(
            if (url.startsWith("http") && !url.startsWith("https")) {
                url.removePrefix("http").prefixIfNot("https")
            } else url
        )
        val fileName = realUrl.path
        val cached = extension.schemaOutputDir.file(fileName).get().asFile
        return if (cached.exists()) {
            cached.readText()
        } else {
            cached.parentFile.mkdirs()
            realUrl.readText().also(cached::writeText)
        }
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
            required = schema.required,
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
                if (ref != null) return ClassWrapper.List(type.title, ref)

                val itemType: JsonSchemaType = json.decodeFromJsonElement(items)
                val itemClass =
                    resolveClass(json, schema, name, itemType) ?: ClassWrapper.StringType(
                        itemType.title
                    )
                return ClassWrapper.List(type.title, itemClass)
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

                if (type.anyOf.isNotEmpty()) {
                    return ClassWrapper.StringType(type.title)
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
                    required = type.required,
                    patternProperties = type.patternProperties,
                ).also { classCache[className] = it }
            }
            is JsonSchemaType.StringType -> return ClassWrapper.StringType(
                comment = type.title,
                nullable = FORCE_NULLABLE_NAMES.contains(name)
            )
            is JsonSchemaType.Number -> return ClassWrapper.Number(type.title)
            is JsonSchemaType.Integer -> return ClassWrapper.Integer(type.title)
            is JsonSchemaType.Unknown -> {
                if (type.anyOf.isNotEmpty()) {
                    return ClassWrapper.StringType(type.title)
                }
                return ClassWrapper.Map(
                    type.title,
                    ClassWrapper.StringType(""),
                    ClassWrapper.StringType("")
                )
            }
        }
    }

    private fun buildClassFromProperties(
        json: Json,
        schema: JsonSchema,
        className: String,
        comment: String,
        properties: Map<String, JsonElement>,
        required: List<String>,
        patternProperties: Map<String, JsonElement>,
    ): ClassWrapper {
        if (patternProperties.isNotEmpty()) {
            return if (patternProperties.filter { it.value is JsonObject }
                    .map { it.value.jsonObject }
                    .any { (it["type"] as? JsonPrimitive)?.content == "array" }
            ) {
                ClassWrapper.Map(
                    comment,
                    ClassWrapper.StringType(""),
                    ClassWrapper.Object("", ClassName("kotlinx.serialization.json", "JsonElement"))
                )
            } else {
                ClassWrapper.Map(
                    comment,
                    ClassWrapper.StringType(""),
                    ClassWrapper.StringType("")
                )
            }
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

        fun ClassWrapper.realTypeName(optional: Boolean) = when (this) {
            is ClassWrapper.List -> typeName
            is ClassWrapper.Map -> typeName
            is ClassWrapper.Number -> typeName.copy(nullable = optional)
            is ClassWrapper.Integer -> typeName.copy(nullable = optional)
            is ClassWrapper.Object -> typeName.copy(nullable = optional)
            is ClassWrapper.StringType -> typeName.copy(nullable = optional)
        }

        FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .addModifiers(KModifier.DATA)
                    .addAnnotation(Serializable::class)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .apply {
                                resolvedProperties.forEach { (name, classWrapper) ->
                                    val optional = !required.contains(name.serializedName)
                                    val realTypeName = classWrapper.realTypeName(optional)
                                    addParameter(
                                        ParameterSpec.builder(name.propertyName, realTypeName)
                                            .addKdoc(classWrapper.comment)
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

                                                if (optional) {
                                                    defaultValue(classWrapper.defaultValue)
                                                }
                                            }
                                            .build()
                                    )
                                }
                            }
                            .build()
                    )
                    .apply {
                        resolvedProperties.forEach { (name, classWrapper) ->
                            val optional = !required.contains(name.serializedName)
                            val realTypeName = classWrapper.realTypeName(optional)
                            addProperty(
                                PropertySpec.builder(name.propertyName, realTypeName)
                                    .addKdoc(classWrapper.comment)
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

    sealed class ClassWrapper {
        abstract val comment: String
        abstract val typeName: TypeName
        abstract val defaultValue: String

        data class List(
            override val comment: String,
            val itemType: ClassWrapper
        ) : ClassWrapper() {
            override val typeName = ClassName("kotlin.collections", "List")
                .parameterizedBy(itemType.typeName)
            override val defaultValue = "emptyList()"
        }

        data class Map(
            override val comment: String,
            val keyType: ClassWrapper,
            val valueType: ClassWrapper
        ) : ClassWrapper() {
            override val typeName = ClassName("kotlin.collections", "Map")
                .parameterizedBy(keyType.typeName, valueType.typeName)
            override val defaultValue = "emptyMap()"
        }

        data class StringType(
            override val comment: String,
            val nullable: Boolean = false
        ) : ClassWrapper() {
            override val typeName = ClassName("kotlin", "String").copy(nullable = nullable)
            override val defaultValue = "\"\""
        }

        data class Number(override val comment: String) : ClassWrapper() {
            override val typeName = ClassName("kotlin", "Float")
            override val defaultValue = "-1f"
        }

        data class Integer(override val comment: String) : ClassWrapper() {
            override val typeName = ClassName("kotlin", "Int")
            override val defaultValue = "-1"
        }

        data class Object(
            override val comment: String,
            val className: ClassName
        ) : ClassWrapper() {
            override val typeName = className
            override val defaultValue = "null"
        }
    }

    inner class PropertyWrapper(
        val serializedName: String,
    ) {
        val propertyName: String by lazy {
            extension.customPropertyNames.getting(serializedName).getOrElse(
                serializedName.toPropertyName()
            )
        }
    }
}