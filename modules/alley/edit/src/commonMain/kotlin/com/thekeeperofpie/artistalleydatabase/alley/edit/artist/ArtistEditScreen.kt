package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_summary
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

object ArtistEditScreen {

    @Composable
    operator fun invoke(
        route: AlleyEditDestination.ArtistEdit,
        graph: ArtistAlleyEditGraph,
        onClickBack: () -> Unit,
        viewModel: ArtistEditViewModel = viewModel {
            graph.artistEditViewModelFactory.create(route, createSavedStateHandle())
        },
    ) {
        ArtistEditScreen(
            artistId = route.artistId,
            state = viewModel.state,
            onClickBack = onClickBack,
        )
    }

    @Composable
    operator fun invoke(artistId: Uuid, state: State, onClickBack: () -> Unit) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Editing ${state.name.value.text.ifBlank { artistId }}") },
                        navigationIcon = { ArrowBackIconButton(onClick = onClickBack) }
                    )
                },
                modifier = Modifier.widthIn(max = 960.dp)
            ) {
                EntryForm2(
                    modifier = Modifier
                        .padding(it)
                        .verticalScroll(rememberScrollState())
                ) {
                    SingleTextSection(state.booth, Res.string.alley_artist_edit_booth)
                    SingleTextSection(state.name, Res.string.alley_artist_edit_name)
                    SingleTextSection(state.summary, Res.string.alley_artist_edit_summary)
                    MultiTextSection(
                        state = state.links,
                        headerText = {
                            Text(stringResource(Res.string.alley_artist_edit_links))
                        },
                        items = remember { SnapshotStateList() },
                        onItemCommitted = { },
                        removeLastItem = { null },
                    )
                    MultiTextSection(
                        state = state.storeLinks,
                        headerText = {
                            Text(stringResource(Res.string.alley_artist_edit_store_links))
                        },
                        items = remember { SnapshotStateList() },
                        onItemCommitted = { },
                        removeLastItem = { null },
                    )
                    MultiTextSection(
                        state = state.catalogLinks,
                        headerText = {
                            Text(stringResource(Res.string.alley_artist_edit_catalog_links))
                        },
                        items = remember { SnapshotStateList() },
                        onItemCommitted = { },
                        removeLastItem = { null },
                    )
                    MultiTextSection(
                        state = state.commissions,
                        headerText = {
                            Text(stringResource(Res.string.alley_artist_edit_commissions))
                        },
                        items = remember { SnapshotStateList() },
                        onItemCommitted = { },
                        removeLastItem = { null },
                    )
                    LongTextSection(
                        state.notes,
                        headerText = {
                            Text(stringResource(Res.string.alley_artist_edit_notes))
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun EntryFormScope.SingleTextSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
    ) {
        SingleTextSection(state = state, headerText = { Text(stringResource(title)) })
    }

    @Stable
    class State(
        val booth: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val name: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val summary: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val links: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
        val storeLinks: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
        val catalogLinks: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
        val commissions: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
        val notes: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
    ) {
        object Saver : ComposeSaver<State, List<Any>> {
            override fun SaverScope.save(value: State) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.booth) },
                with(EntryForm2.SingleTextState.Saver) { save(value.name) },
                with(EntryForm2.SingleTextState.Saver) { save(value.summary) },
                with(EntryForm2.PendingTextState.Saver) { save(value.links) },
                with(EntryForm2.PendingTextState.Saver) { save(value.storeLinks) },
                with(EntryForm2.PendingTextState.Saver) { save(value.catalogLinks) },
                with(EntryForm2.PendingTextState.Saver) { save(value.commissions) },
                with(EntryForm2.PendingTextState.Saver) { save(value.notes) },
            )

            override fun restore(value: List<Any>) = State(
                booth = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) }!!,
                name = with(EntryForm2.SingleTextState.Saver) { restore(value[1]) }!!,
                summary = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) }!!,
                links = with(EntryForm2.PendingTextState.Saver) { restore(value[3]) }!!,
                storeLinks = with(EntryForm2.PendingTextState.Saver) { restore(value[4]) }!!,
                catalogLinks = with(EntryForm2.PendingTextState.Saver) { restore(value[5]) }!!,
                commissions = with(EntryForm2.PendingTextState.Saver) { restore(value[6]) }!!,
                notes = with(EntryForm2.PendingTextState.Saver) { restore(value[7]) }!!,
            )
        }
    }
}
