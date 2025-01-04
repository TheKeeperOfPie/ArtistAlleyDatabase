package com.thekeeperofpie.artistalleydatabase.alley.tags

expect class SeriesEntry {
    val name: String
    val notes: String?

    constructor(name: String, notes: String?)
}
