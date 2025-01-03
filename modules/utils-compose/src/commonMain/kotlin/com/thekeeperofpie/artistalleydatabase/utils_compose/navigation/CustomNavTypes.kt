package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.core.bundle.Bundle
import androidx.navigation.NavType
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object CustomNavTypes {

    val baseTypeMap: Map<KType, NavType<*>> = mapOf(
        typeOf<ImageState?>() to CustomNavTypes.SerializableType<ImageState>(),
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

        override fun parseValue(value: String) =
            if (value == "null") null else value.toBooleanStrictOrNull()
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

        override fun parseValue(value: String) =
            if (value == "null") null else value.toFloatOrNull()
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

        override fun parseValue(value: String) = if (value == "null") null else value.toIntOrNull()
    }

    class NullableEnumType<EnumType : Enum<*>>(
        private val fromName: (String) -> EnumType?,
    ) : NavType<EnumType?>(true) {
        override fun get(bundle: Bundle, key: String) =
            bundle.getString(key)?.let(fromName)

        override fun put(bundle: Bundle, key: String, value: EnumType?) {
            bundle.putString(key, value?.name)
        }

        override fun parseValue(value: String) = if (value == "null") {
            null
        } else {
            fromName(value)
        }

        override fun serializeAsValue(value: EnumType?) = value?.name?.let(::encode) ?: "null"
    }

    @OptIn(InternalSerializationApi::class)
    class SerializableType<Type : Any>(private val type: KClass<Type>) : NavType<Type?>(true) {
        companion object {
            inline operator fun <reified T : Any> invoke() =
                CustomNavTypes.SerializableType(T::class)
        }

        override val name: String = type.simpleName!!

        private val serializer by lazy { type.serializer() }

        override fun put(bundle: Bundle, key: String, value: Type?) {
            bundle.putString(key, value?.let { Json.encodeToString(serializer, it) })
        }

        override fun get(bundle: Bundle, key: String) = bundle.getString(key)?.let {
            Json.decodeFromString(serializer, it)
        }

        override fun parseValue(value: String) = if (value == "null") {
            null
        } else {
            Json.decodeFromString(serializer, value)
        }

        override fun serializeAsValue(value: Type?) = if (value == null) {
            "null"
        } else {
            encode(Json.encodeToString(serializer, value))
        }
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

        override val name: String = type.simpleName!!

        override fun put(bundle: Bundle, key: String, value: Type?) {
            bundle.putString(key, value?.let(toString))
        }

        override fun get(bundle: Bundle, key: String) = bundle.getString(key)?.let(fromString)

        override fun parseValue(value: String) = if (value == "null") null else fromString(value)

        override fun serializeAsValue(value: Type?) =
            value?.let { encode(toString(value)) } ?: "null"
    }

    //  Copied from internal navigation implementation

    /**
     * Encodes characters in the given string as '%'-escaped octets
     * using the UTF-8 scheme. Leaves letters ("A-Z", "a-z"), numbers
     * ("0-9"), and unreserved characters ("_-!.~'()*") intact. Encodes
     * all other characters.
     *
     * @param s string to encode
     * @param allow set of additional characters to allow in the encoded form,
     *  null if no characters should be skipped
     * @return an encoded version of s suitable for use as a URI component
     */
    private fun encode(s: String, allow: String? = null): String {

        // Lazily-initialized buffers.
        var encoded: StringBuilder? = null

        val oldLength = s.length

        // This loop alternates between copying over allowed characters and
        // encoding in chunks. This results in fewer method calls and
        // allocations than encoding one character at a time.
        var current = 0
        while (current < oldLength) {
            // Start in "copying" mode where we copy over allowed chars.

            // Find the next character which needs to be encoded.

            var nextToEncode = current
            while (nextToEncode < oldLength
                && isAllowed(s[nextToEncode], allow)
            ) {
                nextToEncode++
            }

            // If there's nothing more to encode...
            if (nextToEncode == oldLength) {
                if (current == 0) {
                    // We didn't need to encode anything!
                    return s
                } else {
                    // Presumably, we've already done some encoding.
                    encoded!!.append(s, current, oldLength)
                    return encoded.toString()
                }
            }

            if (encoded == null) {
                encoded = StringBuilder()
            }

            if (nextToEncode > current) {
                // Append allowed characters leading up to this point.
                encoded.append(s, current, nextToEncode)
            } else {
                // assert nextToEncode == current
            }

            // Switch to "encoding" mode.

            // Find the next allowed character.
            current = nextToEncode
            var nextAllowed = current + 1
            while (nextAllowed < oldLength
                && !isAllowed(s[nextAllowed], allow)
            ) {
                nextAllowed++
            }

            // Convert the substring to bytes and encode the bytes as
            // '%'-escaped octets.
            val bytes: ByteArray = s.encodeToByteArray(current, nextAllowed)
            val bytesLength = bytes.size
            for (i in 0 until bytesLength) {
                encoded.append('%')
                encoded.append(HEX_DIGITS[(bytes[i].toInt() and 0xf0) shr 4])
                encoded.append(HEX_DIGITS[bytes[i].toInt() and 0xf])
            }

            current = nextAllowed
        }

        // Encoded could still be null at this point if s is empty.
        return encoded?.toString() ?: s
    }

    private fun isAllowed(c: Char, allow: String?): Boolean {
        return (c in 'A'..'Z')
                || (c in 'a'..'z')
                || (c in '0'..'9')
                || "_-!.~'()*".indexOf(c) != -1
                || (allow != null && allow.indexOf(c) != -1)
    }

    private val HEX_DIGITS = "0123456789ABCDEF".toCharArray()
}
