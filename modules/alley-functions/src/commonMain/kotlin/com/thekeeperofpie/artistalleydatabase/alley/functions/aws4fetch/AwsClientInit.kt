package com.thekeeperofpie.artistalleydatabase.alley.functions.aws4fetch

import com.thekeeperofpie.artistalleydatabase.alley.functions.Env

@Suppress("NOTHING_TO_INLINE")
inline fun AwsClientInit(accessKeyId: String, secretAccessKey: String): AwsClientInit {
    val value = js("({})")
    value["accessKeyId"] = accessKeyId
    value["secretAccessKey"] = secretAccessKey
    return value
}

internal fun awsClient(env: Env) = AwsClient(
    AwsClientInit(
        accessKeyId = env.IMAGES_ACCESS_KEY_ID,
        secretAccessKey = env.IMAGES_SECRET_ACCESS_KEY_ID
    )
)

@Suppress("NOTHING_TO_INLINE")
inline fun AwsParamsInit(awsSignInit: AwsSignInit): AwsParamsInit {
    val value = js("({})")
    value["aws"] = awsSignInit
    return value
}

@Suppress("NOTHING_TO_INLINE")
inline fun AwsSignInit(signQuery: Boolean): AwsSignInit {
    val value = js("({})")
    value["signQuery"] = signQuery
    return value
}
