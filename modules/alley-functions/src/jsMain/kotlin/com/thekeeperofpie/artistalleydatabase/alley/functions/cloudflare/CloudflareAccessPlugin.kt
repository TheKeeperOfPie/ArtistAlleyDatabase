package com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare

external interface CloudflareAccessPlugin {
    val JWT: CloudflareAccessJwt
}

external interface CloudflareAccessJwt {
    val payload: CloudflareAccessJwtPayload?
}

external interface CloudflareAccessJwtPayload {
    val email: String?
}
