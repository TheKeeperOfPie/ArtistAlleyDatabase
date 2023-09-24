package com.thekeeperofpie.artistalleydatabase.entry

import kotlinx.serialization.Serializable

data class TestModel(val id: String?, var data: String? = null)

@Serializable
data class TestEntry(val id: String, val data: String? = null, val skipIgnoreableErrors: Boolean = false)
