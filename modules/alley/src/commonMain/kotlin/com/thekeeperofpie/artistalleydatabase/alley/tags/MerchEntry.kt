package com.thekeeperofpie.artistalleydatabase.alley.tags

expect class MerchEntry {
    val name: String
    val notes: String?
    constructor(name: String, notes: String?)
}
