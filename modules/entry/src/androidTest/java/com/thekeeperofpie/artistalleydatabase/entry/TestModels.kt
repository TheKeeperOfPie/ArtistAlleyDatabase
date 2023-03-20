package com.thekeeperofpie.artistalleydatabase.entry

data class TestModel(val id: String?, var data: String? = null)
data class TestEntry(val id: String, val data: String? = null, val skipIgnoreableErrors: Boolean = false)