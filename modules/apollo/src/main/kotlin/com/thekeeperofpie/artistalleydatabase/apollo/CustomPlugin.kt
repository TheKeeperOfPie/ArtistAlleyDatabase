package com.thekeeperofpie.artistalleydatabase.apollo

import com.apollographql.apollo3.compiler.Plugin
import com.apollographql.apollo3.compiler.Transform
import com.apollographql.apollo3.compiler.codegen.kotlin.KotlinOutput
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

class CustomPlugin : Plugin {

    override fun kotlinOutputTransform() = chain(DefaultValuesTransform, ComposeStableTransform)

    private fun <T> chain(vararg transforms: Transform<T>) = object : Transform<T> {
        override fun transform(input: T) = transforms.fold(input) { acc, transform ->
            transform.transform(acc)
        }
    }

    object ComposeStableTransform : Transform<KotlinOutput> {

        private val stableAnnotation = ClassName("androidx.compose.runtime", "Stable")

        override fun transform(input: KotlinOutput) = KotlinOutput(
            fileSpecs = input.fileSpecs.map {
                it.toBuilder()
                    .apply {
                        members.replaceAll {
                            (it as? TypeSpec)?.addComposeStableAnnotation() ?: it
                        }
                    }
                    .build()
            },
            codegenMetadata = input.codegenMetadata,
        )

        private fun TypeSpec.addComposeStableAnnotation() = toBuilder()
            .addAnnotation(stableAnnotation)
            .build()
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
