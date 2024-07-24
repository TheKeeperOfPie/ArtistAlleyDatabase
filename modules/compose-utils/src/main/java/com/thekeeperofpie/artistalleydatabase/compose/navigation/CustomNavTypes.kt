package com.thekeeperofpie.artistalleydatabase.compose.navigation

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import com.thekeeperofpie.artistalleydatabase.compose.ImageState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKey
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object CustomNavTypes {

    val baseTypeMap: Map<KType, NavType<*>> = mapOf(
        typeOf<ImageState?>() to CustomNavTypes.SerializableParcelableType<ImageState>(),
        typeOf<SharedTransitionKey?>() to CustomNavTypes.StringValueType(
            SharedTransitionKey::key,
            SharedTransitionKey::deserialize,
        ),
        typeOf<Boolean?>() to CustomNavTypes.NullableBooleanType,
        typeOf<Float?>() to CustomNavTypes.NullableFloatType,
        typeOf<Int?>() to CustomNavTypes.NullableIntType,
    )

    object NullableBooleanType : NavType<Boolean?>(true) {
        override val name = "nullableBoolean"

        override fun put(bundle: Bundle, key: String, value: Boolean?) {
            if (value != null) {
                bundle.putBoolean(key, value)
            }
        }

        override fun get(bundle: Bundle, key: String) =
            if (bundle.containsKey(key)) bundle.getBoolean(key) else null

        override fun parseValue(value: String) = value.toBooleanStrictOrNull()
    }

    object NullableFloatType : NavType<Float?>(true) {
        override val name = "nullableFloat"

        override fun put(bundle: Bundle, key: String, value: Float?) {
            if (value != null) {
                bundle.putFloat(key, value)
            }
        }

        override fun get(bundle: Bundle, key: String) =
            if (bundle.containsKey(key)) bundle.getFloat(key) else null

        override fun parseValue(value: String) = value.toFloatOrNull()
    }

    object NullableIntType : NavType<Int?>(true) {
        override val name = "nullableInt"

        override fun put(bundle: Bundle, key: String, value: Int?) {
            if (value != null) {
                bundle.putInt(key, value)
            }
        }

        override fun get(bundle: Bundle, key: String) =
            if (bundle.containsKey(key)) bundle.getInt(key) else null

        override fun parseValue(value: String) = value.toIntOrNull()
    }

    class NullableEnumType<EnumType : Enum<*>>(private val type: KClass<EnumType>) :
        NavType<EnumType?>(true) {
        companion object {
            inline operator fun <reified T : Enum<*>> invoke() =
                CustomNavTypes.NullableEnumType(T::class)
        }

        override fun get(bundle: Bundle, key: String) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(key, type.java)
            } else {
                @Suppress("DEPRECATION", "UNCHECKED_CAST")
                bundle.getSerializable(key) as? EnumType
            }

        override fun put(bundle: Bundle, key: String, value: EnumType?) {
            bundle.putSerializable(key, value)
        }

        override fun parseValue(value: String) =
            type.java.enumConstants?.find { it.name.equals(value, ignoreCase = true) }

        override fun serializeAsValue(value: EnumType?) = Uri.encode(value?.name) ?: ""
    }

    @OptIn(InternalSerializationApi::class)
    class SerializableParcelableType<Type : Any>(private val type: KClass<Type>) :
        NavType<Type>(true) {
        companion object {
            inline operator fun <reified T : Any> invoke() =
                CustomNavTypes.SerializableParcelableType(T::class)
        }

        override val name: String = type.java.name

        override fun put(bundle: Bundle, key: String, value: Type) {
            bundle.putParcelable(key, value as Parcelable?)
        }

        override fun get(bundle: Bundle, key: String) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(key, type.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable(key) as? Type
            }

        override fun parseValue(value: String) = Json.decodeFromString(type.serializer(), value)

        override fun serializeAsValue(value: Type) = Uri.encode(Json.encodeToString(type.serializer(), value))!!
    }

    class StringValueType<Type : Any>(
        private val type: KClass<Type>,
        val toString: (Type) -> String,
        val fromString: (String) -> Type,
    ) : NavType<Type?>(true) {
        companion object {
            inline operator fun <reified T : Any> invoke(
                noinline toString: (T) -> String,
                noinline fromString: (String) -> T,
            ) = CustomNavTypes.StringValueType(T::class, toString, fromString)
        }
        override val name: String = type.java.name

        override fun put(bundle: Bundle, key: String, value: Type?) {
            bundle.putString(key, value?.let(toString))
        }

        override fun get(bundle: Bundle, key: String) = bundle.getString(key)?.let(fromString)

        override fun parseValue(value: String) = fromString(value)

        override fun serializeAsValue(value: Type?) = value?.let { Uri.encode(toString(value)) } ?: "null"
    }
}
