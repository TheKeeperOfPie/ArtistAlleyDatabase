package com.thekeeperofpie.artistalleydatabase.entry

import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
abstract class EntryDetailsTestBase {

    private val component = EntryTestComponent::class.create()

    internal fun TestScope.testViewModel(
        hasError: Boolean = false,
        cropUri: String? = null,
        entryIds: List<EntryId> = emptyList(),
        vararg entries: TestEntry,
    ) = TestViewModel(
        appFileSystem = component.appFileSystem,
        hasError = hasError,
        cropUri = cropUri,
        entries = entries.associateBy { it.id }.toMutableMap(),
        json = component.json,
        cropController = component.cropController,
        customDispatchers = CustomDispatchers(UnconfinedTestDispatcher(testScheduler)),
    ).apply { initialize(entryIds) }

    protected fun assertModel(
        model: TestEntry,
        data: String? = null,
        skipIgnoreableErrors: Boolean = false
    ) {
        assertThat(model.id).isNotEmpty()
        assertThat(model.data).isEqualTo(data)
        assertThat(model.skipIgnoreableErrors).isEqualTo(skipIgnoreableErrors)
    }
}
