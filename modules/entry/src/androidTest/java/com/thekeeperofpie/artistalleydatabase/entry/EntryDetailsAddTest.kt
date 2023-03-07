package com.thekeeperofpie.artistalleydatabase.entry

import androidx.navigation.NavHostController
import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.test_utils.atLeast
import com.thekeeperofpie.artistalleydatabase.test_utils.during
import com.thekeeperofpie.artistalleydatabase.test_utils.mockStrict
import com.thekeeperofpie.artistalleydatabase.test_utils.untilCalled
import com.thekeeperofpie.artistalleydatabase.test_utils.whenever
import com.thekeeperofpie.artistalleydatabase.test_utils.withDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class EntryDetailsAddTest {

    companion object {

        @JvmStatic
        @BeforeAll
        fun enableTestDispatchers() {
            // TODO: Move this to a place that's called once in the test process
            CustomDispatchers.enable()
        }
    }

    @Test
    fun addEmpty_clickSave_success() = runTest {
        val scope = TestScope()
        val navHostController = mockNavHostController()
        val viewModel = TestViewModel().apply { initialize(emptyList()) }
        await().until { !viewModel.sectionsLoading }

        viewModel.runSaveAndWait(scope, navHostController, success = true) {
            onClickSave(navHostController)
        }

        assertThat(viewModel.output).containsExactly(TestEntry())
        verify(navHostController).popBackStack()
    }

    @Test
    fun addEmpty_clickSave_error() = runTest {
        val scope = TestScope()
        val navHostController = mockNavHostController()
        val viewModel = TestViewModel(hasError = true).apply { initialize(emptyList()) }
        await().until { !viewModel.sectionsLoading }

        viewModel.runSaveAndWait(scope, navHostController, success = false) {
            onClickSave(navHostController)
        }

        assertThat(viewModel.output).isEmpty()
        verify(navHostController, never()).popBackStack()
    }

    @Test
    fun addEmpty_longClickSave_success() = runTest {
        val scope = TestScope()
        val navHostController = mockNavHostController()
        val viewModel = TestViewModel().apply { initialize(emptyList()) }
        await().until { !viewModel.sectionsLoading }

        viewModel.runSaveAndWait(scope, navHostController, success = true) {
            onLongClickSave(navHostController)
        }

        assertThat(viewModel.output).containsExactly(TestEntry(skipIgnoreableErrors = true))
        verify(navHostController).popBackStack()
    }

    @Test
    fun addEmpty_longClickSave_error() = runTest {
        val scope = TestScope()
        val navHostController = mockNavHostController()
        val viewModel = TestViewModel(hasError = true).apply { initialize(emptyList()) }
        await().until { !viewModel.sectionsLoading }

        viewModel.runSaveAndWait(scope, navHostController, success = true) {
            onLongClickSave(navHostController)
        }

        assertThat(viewModel.output).containsExactly(TestEntry(skipIgnoreableErrors = true))
        verify(navHostController).popBackStack()
    }

    private fun mockNavHostController() = mockStrict<NavHostController> {
        whenever(popBackStack()) { true }
    }

    private fun EntryDetailsViewModel<*, *>.waitForSaveResult(
        navHostController: NavHostController,
        success: Boolean,
    ) {
        if (success) {
            await().untilCalled(navHostController, NavHostController::popBackStack)
            await().atLeast(1.seconds).during(1.seconds).until { saving }
        } else {
            await().until { !saving }
        }
    }

    private suspend fun EntryDetailsViewModel<*, *>.runSaveAndWait(
        testScope: TestScope,
        navHostController: NavHostController,
        success: Boolean,
        block: EntryDetailsViewModel<*, *>.() -> Unit
    ) {
        // Swap the main Dispatcher, run the block, pausing main execution,
        // wait for saving indicator, unblock the main execution, and assert success/failure
        testScope.withDispatchers {
            block()
            await().until { saving }
            testScope.advanceUntilIdle()
        }

        waitForSaveResult(navHostController, success)
    }
}
