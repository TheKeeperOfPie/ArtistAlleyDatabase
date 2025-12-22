package com.thekeeperofpie.artistalleydatabase.alley.models

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.SHA384
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.serialization.json.Json

data class AlleyCryptographyKeys(
    val publicKey: String,
    val privateKey: String,
) {
    companion object {
        suspend fun generate(): AlleyCryptographyKeys {
            val ecdsa = CryptographyProvider.Default.get(ECDSA)
            val keyPair = ecdsa.keyPairGenerator(EC.Curve.P384).generateKey()
            val publicKey = keyPair.publicKey
                .encodeToByteString(EC.PublicKey.Format.JWK)
                .decodeToString()

            val privateKey = keyPair.privateKey
                .encodeToByteString(EC.PrivateKey.Format.JWK)
                .decodeToString()

            return AlleyCryptographyKeys(publicKey, privateKey)
        }

        suspend inline fun <reified T> verifySignature(
            publicKey: String,
            signature: String,
            payload: T,
        ): Boolean {
            val ecdsa = CryptographyProvider.Default.get(ECDSA)
            val publicKey = ecdsa.publicKeyDecoder(EC.Curve.P384)
                .decodeFromByteString(EC.PublicKey.Format.JWK, publicKey.encodeToByteString())
            return publicKey.signatureVerifier(SHA384, ECDSA.SignatureFormat.RAW)
                .tryVerifySignature(
                    data = Json.encodeToString<T>(payload).encodeToByteString(),
                    signature = signature.encodeToByteString(),
                )
        }
    }
}
