package com.thekeeperofpie.artistalleydatabase.compose.navigation

import android.os.Bundle
import androidx.navigation.NavType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

val NavType.Companion.NullableBooleanType: NavType<Boolean?> get() = CustomNavTypes.NullableBooleanType
val NavType.Companion.NullableFloatType: NavType<Float?> get() = CustomNavTypes.NullableFloatType
val NavType.Companion.NullableIntType: NavType<Int?> get() = CustomNavTypes.NullableIntType

object CustomNavTypes {

    val baseTypeMap: Map<KType, NavType<*>> = mapOf(
        typeOf<Boolean?>() to NavType.NullableBooleanType,
        typeOf<Float?>() to NavType.NullableFloatType,
        typeOf<Int?>() to NavType.NullableIntType,
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

    class NullableEnumType<EnumType : Enum<*>>(private val type: Class<EnumType>) :
        NavType<EnumType?>(true) {
        override fun get(bundle: Bundle, key: String): EnumType? {
            val targetName = bundle.getString(key)
            return targetName?.let(::parseValue)
        }

        override fun parseValue(value: String) =
            type.enumConstants?.find { it.name.equals(value, ignoreCase = true) }

        override fun put(bundle: Bundle, key: String, value: EnumType?) {
            bundle.putString(key, value?.name)
        }
    }
}
