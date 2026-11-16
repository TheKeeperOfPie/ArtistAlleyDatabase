@file:JsModule("aws4fetch")

package com.thekeeperofpie.artistalleydatabase.alley.functions.aws4fetch

import org.w3c.fetch.Request
import kotlin.js.Promise

external class AwsClient {
    constructor(init: AwsClientInit = definedExternally)

    fun sign(request: Request, init: AwsParamsInit = definedExternally): Promise<Request>
}

external interface AwsParamsInit {
    var awsSign: AwsSignInit?
        get() = definedExternally
        set(value) = definedExternally
}

external interface AwsSignInit {
    var signQuery: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface AwsClientInit {
    val accessKeyId: String
    val secretAccessKey: String
}
