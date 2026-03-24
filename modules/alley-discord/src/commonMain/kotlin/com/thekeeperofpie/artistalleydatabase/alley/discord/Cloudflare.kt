package com.thekeeperofpie.artistalleydatabase.alley.discord

import org.w3c.fetch.Request
import org.w3c.fetch.Response
import kotlin.js.Promise

external fun fetch(request: Request): Promise<Response>
