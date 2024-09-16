package com.thekeeperofpie.artistalleydatabase.apollo

import com.apollographql.apollo3.compiler.ApolloCompilerPlugin
import com.apollographql.apollo3.compiler.Transform
import com.apollographql.apollo3.compiler.codegen.kotlin.KotlinOutput
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName

class CustomPlugin : ApolloCompilerPlugin {

    companion object {
        // TODO: These are special cased because they accept Any in the constructor,
        //  and editing that to work correctly seems very difficult
        private val EXCLUDED = setOf(
            "ForumThreadCommentQuery",
            "ForumThread_CommentsQuery",
            "HomeMangaQuery",
            "MediaAdvancedSearchQuery",
            "MediaDetailsQuery",
            "MediaListEntryQuery",
        )
    }

    override fun kotlinOutputTransform() = chain(
        DefaultValuesTransform,
        ComposeImmutableTransform,
        KotlinXSerializationTransform,
        OptionalSerializerTransform,
    )

    private fun <T> chain(vararg transforms: Transform<T>) = object : Transform<T> {
        override fun transform(input: T) = transforms.fold(input) { acc, transform ->
            transform.transform(acc)
        }
    }

    object ComposeImmutableTransform : Transform<KotlinOutput> {
        private val annotation = ClassName("androidx.compose.runtime", "Immutable")

        override fun transform(input: KotlinOutput) = KotlinOutput(
            fileSpecs = input.fileSpecs.map {
                it.toBuilder()
                    .apply {
                        members.replaceAll {
                            val typeSpec = it as? TypeSpec ?: return@replaceAll it
                            if (EXCLUDED.contains(typeSpec.name)) return@replaceAll it
                            typeSpec.toBuilder()
                                .addAnnotation(annotation)
                                .build()
                        }
                    }
                    .build()
            },
            codegenMetadata = input.codegenMetadata,
        )
    }

    object KotlinXSerializationTransform : Transform<KotlinOutput> {
        private val mutation = ClassName("com.apollographql.apollo3.api", "Mutation")
        private val annotation = ClassName("kotlinx.serialization", "Serializable")
        private val any = ClassName("kotlin", "Any")
        private val anyNullable = any.copy(nullable = true)
        private val int = ClassName("kotlin", "Int")

        override fun transform(input: KotlinOutput) = KotlinOutput(
            fileSpecs = input.fileSpecs.map {
                it.toBuilder()
                    .apply {
                        members.replaceAll {
                            val typeSpec = it as? TypeSpec ?: return@replaceAll it
                            if (EXCLUDED.contains(typeSpec.name)) return@replaceAll it
                            if (typeSpec.superinterfaces
                                    .any { (it.key as? ParameterizedTypeName)?.rawType == mutation }
                            ) {
                                return@replaceAll it
                            }
                            if (typeSpec.kind == TypeSpec.Kind.INTERFACE) {
                                return@replaceAll it
                            }

                            addToInner(typeSpec)
                                .toBuilder()
                                .addAnnotation(annotation)
                                .build()
                        }
                    }
                    .build()
            },
            codegenMetadata = input.codegenMetadata,
        )

        private fun addToInner(typeSpec: TypeSpec): TypeSpec {
            val newInnerTypeSpecs = typeSpec.typeSpecs.map {
                if (it.kind == TypeSpec.Kind.OBJECT) return@map it
                addToInner(it)
                    .toBuilder()
                    .addAnnotation(annotation)
                    .build()
            }
            return typeSpec.toBuilder()
                .apply {
                    typeSpecs.clear()
                    typeSpecs += newInnerTypeSpecs
                }
                .build()
        }
    }

    object OptionalSerializerTransform : Transform<KotlinOutput> {

        private val annotation = ClassName("kotlinx.serialization", "Serializable")
        private val optionalType = ClassName("com.apollographql.apollo3.api", "Optional")
        private val optionalSerializer =
            ClassName("com.thekeeperofpie.artistalleydatabase.apollo.utils", "OptionalSerializer")
        private val fuzzyDateInput = ClassName("com.anilist.type", "FuzzyDateInput")
        private val any = ClassName("kotlin", "Any")
        private val anyNullable = any.copy(nullable = true)

        override fun transform(input: KotlinOutput) = KotlinOutput(
            fileSpecs = input.fileSpecs.map {
                it.toBuilder()
                    .apply {
                        members.replaceAll {
                            val typeSpec = it as? TypeSpec ?: return@replaceAll it
                            if (EXCLUDED.contains(typeSpec.name)) return@replaceAll it
                            val newPropertySpecs = typeSpec.propertySpecs.map mapProperties@{
                                val typeName = it.type as? ParameterizedTypeName
                                    ?: return@mapProperties it
                                if (typeName.rawType != optionalType) {
                                    return@mapProperties it.toBuilder()
                                        .addKdoc("Not optional")
                                        .build()
                                }

                                val typeArgument = typeName.typeArguments.singleOrNull()
                                val newTypeName = if (typeArgument == any
                                    || typeArgument == anyNullable
                                ) {
                                    typeName.copy(
                                        typeArguments = listOf(
                                            TypeVariableName(
                                                fuzzyDateInput.canonicalName
                                            ).copy(nullable = typeArgument.isNullable)
                                        )
                                    )
                                } else {
                                    typeName
                                }
                                it.toBuilder(type = newTypeName)
                                    .addAnnotation(
                                        AnnotationSpec.Companion.builder(annotation)
                                            .addMember("%T::class", optionalSerializer)
                                            .build()
                                    )
                                    .apply {
                                    }
                                    .build()
                            }
                            val newConstructor = typeSpec.primaryConstructor
                                ?.takeIf { typeSpec.modifiers.contains(KModifier.DATA) }
                                ?.let {
                                    it.toBuilder()
                                        .apply {
                                            parameters.replaceAll replaceParameters@{
                                                val typeName = it.type as? ParameterizedTypeName
                                                    ?: return@replaceParameters it
                                                val typeArgument =
                                                    typeName.typeArguments.singleOrNull()
                                                if (typeArgument != any
                                                    && typeArgument != anyNullable
                                                ) {
                                                    return@replaceParameters it
                                                }

                                                it.toBuilder(
                                                    type = typeName.copy(
                                                        typeArguments = listOf(
                                                            TypeVariableName(
                                                                fuzzyDateInput.canonicalName
                                                            ).copy(nullable = typeArgument.isNullable)
                                                        )
                                                    )
                                                ).build()
                                            }
                                        }
                                        .build()
                                }
                            typeSpec.toBuilder()
                                .apply {
                                    if (newConstructor != null) {
                                        primaryConstructor(newConstructor)
                                    }
                                    propertySpecs.clear()
                                    propertySpecs += newPropertySpecs
                                }
                                .build()
                        }
                    }
                    .build()
            },
            codegenMetadata = input.codegenMetadata,
        )
    }

    object DefaultValuesTransform : Transform<KotlinOutput> {

        override fun transform(input: KotlinOutput) = KotlinOutput(
            fileSpecs = input.fileSpecs.map {
                it.toBuilder()
                    .apply {
                        members.replaceAll {
                            (it as? TypeSpec)?.addDefaultValueToNullableProperties() ?: it
                        }
                    }
                    .build()
            },
            codegenMetadata = input.codegenMetadata,
        )

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
}
