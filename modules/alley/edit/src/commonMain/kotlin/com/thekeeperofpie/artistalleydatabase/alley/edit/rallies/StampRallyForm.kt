package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.allCaps
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_fandom
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_host_table
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_id
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FieldRevertDialog
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormHeaderIconAndTitle
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.RevertDialogState
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ShowRevertIconButton
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.CustomIcons
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource

internal object StampRallyForm {

    @Composable
    operator fun invoke(
        state: StampRallyFormState,
        errorState: StampRallyErrorState,
        initialStampRally: () -> StampRallyDatabaseEntry?,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        val focusState = rememberFocusState(
            listOf(
                state.editorState.id,
                state.fandom,
                state.hostTable,
                state.stateTables,
                state.stateLinks,
                state.tableMin,
                state.totalCost,
                state.prize,
                state.prizeLimit,
                state.stateSeries,
                state.stateMerch,
                state.editorState.editorNotes,
            )
        )

        EntryForm2(focusState = focusState, modifier = modifier) {
            val scope = remember(this, initialStampRally) {
                object : StampRallyFormScope(this) {
                    override val initialStampRally: StampRallyDatabaseEntry?
                        get() = initialStampRally()
                }
            }

            with(scope) {
                IdSection(state.editorState.id, errorState.idErrorMessage)
                FandomSection(state.fandom)
                HostTableSection(state.hostTable)
            }
        }
    }
}

@LayoutScopeMarker
@Stable
private abstract class StampRallyFormScope(
    entryFormScope: EntryFormScope,
) : EntryFormScope by entryFormScope {
    abstract val initialStampRally: StampRallyDatabaseEntry?

    @Composable
    fun IdSection(
        state: EntryForm2.SingleTextState,
        errorText: (() -> String?)?,
    ) {
        val revertDialogState = rememberRevertDialogState(initialStampRally?.id)
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_stamp_rally_edit_id)) },
            outputTransformation = revertDialogState.outputTransformation,
            errorText = errorText,
            additionalHeaderActions = {
                with(this@StampRallyFormScope) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, Res.string.alley_edit_stamp_rally_edit_id)
    }

    @Composable
    fun FandomSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
    ) {
        val revertDialogState = rememberRevertDialogState(initialStampRally?.fandom)
        SingleTextSection(
            state = state,
            headerText = {
                FormHeaderIconAndTitle(
                    CustomIcons.TableSign,
                    Res.string.alley_edit_stamp_rally_edit_fandom
                )
            },
            outputTransformation = revertDialogState.outputTransformation,
            label = label,
            additionalHeaderActions = {
                with(this@StampRallyFormScope) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, Res.string.alley_edit_stamp_rally_edit_fandom)
    }

    @Composable
    fun HostTableSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
    ) {
        val revertDialogState = rememberRevertDialogState(initialStampRally?.hostTable)
        SingleTextSection(
            state = state,
            headerText = {
                FormHeaderIconAndTitle(
                    CustomIcons.TableSign,
                    Res.string.alley_edit_stamp_rally_edit_host_table
                )
            },
            inputTransformation = InputTransformation.maxLength(3).allCaps(Locale.current),
            outputTransformation = revertDialogState.outputTransformation,
            label = label,
            additionalHeaderActions = {
                with(this@StampRallyFormScope) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, Res.string.alley_edit_stamp_rally_edit_host_table)
    }

    @Composable
    private fun rememberRevertDialogState(initialValue: String?): RevertDialogState {
        val positiveColor = AlleyTheme.colorScheme.positive
        return remember(initialStampRally, initialValue) {
            RevertDialogState(positiveColor, initialStampRally, initialValue.orEmpty())
        }
    }
}
