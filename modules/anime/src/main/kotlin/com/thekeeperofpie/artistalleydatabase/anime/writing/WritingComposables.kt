@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.writing

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.R
import kotlinx.coroutines.launch

@Composable
fun WritingReplyPanelScaffold(
    refreshEvent: Long,
    committing: Boolean,
    onClickSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    sheetState: SheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    ),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState,
        snackbarHostState = snackbarHostState,
    ),
    @StringRes sendButtonTextRes: Int = R.string.anime_writing_send_button,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var replyValue by rememberSaveable(refreshEvent) { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(committing) {
        if (!committing) {
            keyboardController?.hide()
            sheetState.hide()
        }
    }

    BackHandler(
        enabled = sheetState.targetValue != SheetValue.Hidden && !WindowInsets.isImeVisible
    ) {
        scope.launch { sheetState.hide() }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            WritingSheetContent(
                committing = committing,
                value = { replyValue },
                onValueChange = { replyValue = it },
                onClickSend = { onClickSend(replyValue) },
                sendButtonTextRes = sendButtonTextRes,
            )
        },
        topBar = topBar,
        modifier = modifier,
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { scope.launch { sheetState.expand() } }) {
                    Icon(
                        Icons.Filled.Reply,
                        contentDescription = stringResource(
                            R.string.anime_writing_reply_fab_content_description
                        ),
                    )
                }
            },
            // Ignore bottom padding so FAB is linked to bottom
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(top = it.calculateTopPadding())),
            content = content,
        )
    }
}


@Composable
fun WritingSheetContent(
    committing: Boolean,
    value: () -> String,
    onValueChange: (String) -> Unit,
    onClickSend: () -> Unit,
    @StringRes sendButtonTextRes: Int = R.string.anime_writing_send_button,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = value(),
            onValueChange = onValueChange,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
        Divider()
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(IntrinsicSize.Min)
        ) {
            Box(contentAlignment = Alignment.Center) {
                val replyingAlpha by animateFloatAsState(
                    targetValue = if (committing) 1f else 0f,
                    label = "Write committing crossfade",
                )
                TextButton(
                    onClick = {
                        if (value().isNotBlank()) {
                            onClickSend()
                        }
                    },
                    modifier = Modifier.alpha(1f - replyingAlpha)
                ) {
                    Text(text = stringResource(sendButtonTextRes))
                }
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxHeight()
                        .alpha(replyingAlpha)
                )
            }
        }
    }
}
