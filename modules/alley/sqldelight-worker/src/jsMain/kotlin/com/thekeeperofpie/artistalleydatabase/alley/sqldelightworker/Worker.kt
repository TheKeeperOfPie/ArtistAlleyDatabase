package com.thekeeperofpie.artistalleydatabase.alley.sqldelightworker

external interface WorkerSuccessResponse {
    var id: dynamic
    var results: dynamic
}

fun WorkerSuccessResponse(id: dynamic, results: dynamic): WorkerSuccessResponse {
    val o = js("{}").unsafeCast<WorkerSuccessResponse>()
    o.id = id
    o.results = results
    return o
}

external interface WorkerExecResults {
    var values: dynamic
}

fun WorkerExecResults(values: dynamic): WorkerExecResults {
    val o = js("{}").unsafeCast<WorkerExecResults>()
    o.values = values
    return o
}

external interface WorkerErrorResponse {
    var id: dynamic
    var error: dynamic
}

fun WorkerErrorResponse(id: dynamic, error: dynamic): WorkerErrorResponse {
    val o = js("{}").unsafeCast<WorkerErrorResponse>()
    o.id = id
    o.error = error
    return o
}
