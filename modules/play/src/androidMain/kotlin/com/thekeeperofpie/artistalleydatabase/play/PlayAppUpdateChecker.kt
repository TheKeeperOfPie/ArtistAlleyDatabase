package com.thekeeperofpie.artistalleydatabase.play

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import artistalleydatabase.modules.play.generated.resources.Res
import artistalleydatabase.modules.play.generated.resources.play_update_available
import artistalleydatabase.modules.play.generated.resources.play_update_available_button
import artistalleydatabase.modules.play.generated.resources.play_update_downloaded
import artistalleydatabase.modules.play.generated.resources.play_update_downloaded_button
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.requestCompleteUpdate
import com.google.android.play.core.ktx.requestUpdateFlow
import com.google.android.play.core.ktx.updatePriority
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@Inject
class PlayAppUpdateChecker(
    application: Application,
    private val activity: ComponentActivity,
) : AppUpdateChecker, DefaultLifecycleObserver {

    private val appUpdateManager = AppUpdateManagerFactory.create(application)

    private var state by mutableStateOf<AppUpdateResult>(AppUpdateResult.NotAvailable)

    // Available is tracked separately so that it can be cleared by the user to dismiss the message
    private var updateAvailable by mutableStateOf<AppUpdateResult.Available?>(null)

    init {
        activity.lifecycleScope.launch(CustomDispatchers.Main) {
            activity.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                appUpdateManager.requestUpdateFlow()
                    .flowOn(CustomDispatchers.IO)
                    .onEach { Log.d("UpdateDebug", "update state = $it") }
                    .catch { emit(AppUpdateResult.NotAvailable) }
                    .collectLatest { state = it }
            }
        }

        // Only prompt the user once per session
        activity.lifecycleScope.launch(CustomDispatchers.Main) {
            appUpdateManager.requestUpdateFlow()
                .filterIsInstance<AppUpdateResult.Available>()
                .take(1)
                .catch { /* ignore */ }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { updateAvailable = it }
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    override fun applySnackbarState(snackbarHostState: SnackbarHostState) {
        val availableMessage = stringResource(Res.string.play_update_available)
        val availableAction = stringResource(Res.string.play_update_available_button)
        val downloadedMessage = stringResource(Res.string.play_update_downloaded)
        val downloadedAction = stringResource(Res.string.play_update_downloaded_button)
        val updateAvailable = updateAvailable
        LaunchedEffect(state, updateAvailable) {
            if (state is AppUpdateResult.Downloaded) {
                val result = snackbarHostState.showSnackbar(
                    message = downloadedMessage,
                    actionLabel = downloadedAction,
                    duration = SnackbarDuration.Indefinite,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    activity.lifecycleScope.launch {
                        appUpdateManager.requestCompleteUpdate()
                    }
                }
            } else {
                if (updateAvailable != null) {
                    val isFlexibleUpdateAllowed = updateAvailable.updateInfo.isFlexibleUpdateAllowed
                    val isImmediateUpdateAllowed =
                        updateAvailable.updateInfo.isImmediateUpdateAllowed
                    if (isFlexibleUpdateAllowed || isImmediateUpdateAllowed) {
                        val result = snackbarHostState.showSnackbar(
                            message = availableMessage,
                            actionLabel = availableAction,
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            if (updateAvailable.updateInfo.updatePriority == 5
                                && isImmediateUpdateAllowed
                            ) {
                                updateAvailable.startImmediateUpdate(activity, 0)
                            } else if (isFlexibleUpdateAllowed) {
                                updateAvailable.startFlexibleUpdate(activity, 0)
                            } else {
                                updateAvailable.startImmediateUpdate(activity, 0)
                            }
                        }

                        this@PlayAppUpdateChecker.updateAvailable = null
                    }
                }
            }
        }
    }
}
