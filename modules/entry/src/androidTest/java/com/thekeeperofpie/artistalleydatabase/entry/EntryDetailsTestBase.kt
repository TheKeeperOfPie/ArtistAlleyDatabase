package com.thekeeperofpie.artistalleydatabase.entry

import android.net.Uri
import androidx.navigation.NavHostController
import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.test_utils.HiltInjectExtension
import com.thekeeperofpie.artistalleydatabase.test_utils.TestBase
import com.thekeeperofpie.artistalleydatabase.test_utils.atLeast
import com.thekeeperofpie.artistalleydatabase.test_utils.during
import com.thekeeperofpie.artistalleydatabase.test_utils.mockStrict
import com.thekeeperofpie.artistalleydatabase.test_utils.untilCalled
import com.thekeeperofpie.artistalleydatabase.test_utils.whenever
import com.thekeeperofpie.artistalleydatabase.test_utils.withDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ExtendWith(HiltInjectExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
abstract class EntryDetailsTestBase : TestBase() {

    @Inject
    lateinit var appJson: AppJson

    @TempDir
    lateinit var testDir: File

    protected val navHostController by delegate {
        mockStrict<NavHostController> {
            whenever(navigateUp()) { true }
        }
    }

    internal fun testViewModel(
        hasError: Boolean = false,
        cropUri: Uri? = null,
        vararg existingEntries: TestEntry,
    ) = TestViewModel(
        hasError = hasError,
        cropUri = cropUri,
        entries = existingEntries.associateBy { it.id }.toMutableMap(),
        testDirectory = testDir,
        appJson = appJson,
    )

    protected suspend fun EntryDetailsViewModel<*, *>.runSaveAndWait(
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

        if (success) {
            await().untilCalled(navHostController, NavHostController::navigateUp)
            await().atLeast(1.seconds).during(1.seconds).until { saving }
        } else {
            await().until { !saving }
        }
    }

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
