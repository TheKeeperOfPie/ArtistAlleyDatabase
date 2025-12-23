package com.thekeeperofpie.artistalleydatabase.alley.models

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA384
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.json.Json

object AlleyCryptography {

    const val ACCESS_KEY_PARAM = "accessKey"
    const val SIGNATURE_HEADER_KEY = "X-CustomSignature"

    suspend fun generate(): AlleyCryptographyKeys {
        val ecdsa = CryptographyProvider.Default.get(ECDSA)
        val keyPair = ecdsa.keyPairGenerator(EC.Curve.P384).generateKey()
        val publicKey = keyPair.publicKey
            .encodeToByteString(EC.PublicKey.Format.DER)
            .toHexString()

        val privateKey = keyPair.privateKey
            .encodeToByteString(EC.PrivateKey.Format.DER)
            .toHexString()

        return AlleyCryptographyKeys(publicKey, privateKey)
    }

    suspend inline fun <reified T> signRequest(
        privateKey: String,
        payload: T,
    ): String {
        val ecdsa = CryptographyProvider.Default.get(ECDSA)
        val publicKey = ecdsa.privateKeyDecoder(EC.Curve.P384)
            .decodeFromByteString(EC.PrivateKey.Format.DER, privateKey.hexToByteString())
        return publicKey.signatureGenerator(SHA384, ECDSA.SignatureFormat.RAW)
            .generateSignature(Json.encodeToString<T>(payload).encodeToByteString())
            .toHexString()
    }

    suspend inline fun <reified T> verifySignature(
        publicKey: String,
        signature: String,
        payload: T,
    ): Boolean {
        val ecdsa = CryptographyProvider.Default.get(ECDSA)
        val publicKey = ecdsa.publicKeyDecoder(EC.Curve.P384)
            .decodeFromByteString(EC.PublicKey.Format.DER, publicKey.hexToByteString())
        return publicKey.signatureVerifier(SHA384, ECDSA.SignatureFormat.RAW)
            .tryVerifySignature(
                data = Json.encodeToString<T>(payload).encodeToByteString(),
                signature = signature.hexToByteString(),
            )
    }

    suspend fun generateOneTimeEncryptionKeys(): AlleyCryptographyKeys {
        val rsaOaep = CryptographyProvider.Default.get(RSA.OAEP)
        val keys = rsaOaep.keyPairGenerator(digest = SHA384).generateKey()
        return AlleyCryptographyKeys(
            publicKey = keys.publicKey.encodeToByteString(RSA.PublicKey.Format.DER)
                .toHexString(),
            privateKey = keys.privateKey.encodeToByteString(RSA.PrivateKey.Format.DER)
                .toHexString(),
        )
    }

    suspend fun oneTimeEncrypt(publicKey: String, payload: String): String =
        CryptographyProvider.Default.get(RSA.OAEP)
            .publicKeyDecoder(SHA384)
            .decodeFromByteString(
                format = RSA.PublicKey.Format.DER,
                byteString = publicKey.hexToByteString(),
            )
            .encryptor()
            .encrypt(payload.encodeToByteString())
            .toHexString()

    suspend fun oneTimeDecrypt(privateKey: String, payload: String): String =
        CryptographyProvider.Default.get(RSA.OAEP)
            .privateKeyDecoder(SHA384)
            .decodeFromByteString(
                format = RSA.PrivateKey.Format.DER,
                byteString = privateKey.hexToByteString(),
            )
            .decryptor()
            // TODO: Figure out why this is quoted
            .decrypt(payload.removeSurrounding("\"").hexToByteString())
            .decodeToString()
}
