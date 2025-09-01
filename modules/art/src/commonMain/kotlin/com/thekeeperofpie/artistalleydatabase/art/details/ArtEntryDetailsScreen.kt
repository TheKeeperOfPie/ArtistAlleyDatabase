package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.art.generated.resources.Res
import artistalleydatabase.modules.art.generated.resources.art_entry_series_header
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryComponent
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderInSharedTransitionScopeOverlay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.pluralStringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
object ArtEntryDetailsScreen {

    @Composable
    operator fun invoke(artEntryComponent: ArtEntryComponent) {
        val viewModel = viewModel { artEntryComponent.artEntryDetailsViewModel2() }
        val scaffoldState = rememberBottomSheetScaffoldState()
        BottomSheetScaffold(sheetContent = {
            BottomSheet(viewModel, scaffoldState.bottomSheetState)
        }) {
            Box(Modifier.background(Color.Blue).fillMaxSize()) {}
        }
    }


    @Composable
    private fun ColumnScope.BottomSheet(
        viewModel: ArtEntryDetailsViewModel2,
        bottomSheetState: SheetState,
    ) {
        val scrollState = rememberScrollState()
        val bottomSheetTargetValue = bottomSheetState.targetValue
        LaunchedEffect(bottomSheetTargetValue) {
            if (bottomSheetTargetValue == SheetValue.PartiallyExpanded) {
                scrollState.animateScrollTo(0)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                .animateEnterExit(
                    enter = slideInVertically { it * 2 },
                    // TODO: Exit doesn't work
                    exit = slideOutVertically { it * 2 },
                )
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(scrollState)
        ) {
            val state = rememberSaveable(saver = State.Saver) { State() }
            EntryForm2 {
                MultiTextSection(
                    state = state.series,
                    headerText = {
                        Text(
                            pluralStringResource(
                                Res.plurals.art_entry_series_header,
                                state.series.content.size,
                            )
                        )
                    },
                    focusRequester = remember { FocusRequester() },
                    trailingIcon = { /* TODO */ null },
                    entryPredictions = viewModel::series,
                    onNavigate = { /* TODO */ },
                    onFocusChanged = { /* TODO */ },
                )
            }
        }
    }

    @Stable
    class State(
        val series: EntryFormSection.MultiText = EntryFormSection.MultiText(),
    ) {
        companion object {
            @Serializable
            data class SavedState(
                val series: EntryFormSection.MultiText.SavedState,
            )

            val Saver = Saver<State, String>(
                save = {
                    Json.encodeToString(SavedState(it.series.toSavedState()))
                },
                restore = {
                    val savedState = Json.decodeFromString<SavedState>(it)
                    State(savedState.series.toMultiText())
                },
            )
        }
    }
}
