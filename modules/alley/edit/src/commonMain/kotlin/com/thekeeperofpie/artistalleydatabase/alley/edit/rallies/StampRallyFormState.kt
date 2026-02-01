package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.thekeeperofpie.artistalleydatabase.alley.edit.EntryEditMetadata
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormUtils
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import kotlin.time.Clock

@Stable
class StampRallyFormState(
    val metadata: EntryEditMetadata = EntryEditMetadata(),
    val images: SnapshotStateList<EditImage> = SnapshotStateList(),
    val editorState: EditorState = EditorState(),
    val fandom: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val hostTable: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val stateTables: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val tables: SnapshotStateList<String> = SnapshotStateList(),
    val stateLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val links: SnapshotStateList<LinkModel> = SnapshotStateList(),
    val tableMin: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val totalCost: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val prize: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val prizeLimit: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val stateSeries: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val series: SnapshotStateList<SeriesInfo> = SnapshotStateList(),
    val stateMerch: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    val merch: SnapshotStateList<MerchInfo> = SnapshotStateList(),
    val notes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
) {

    object Saver : ComposeSaver<StampRallyFormState, List<Any?>> {
        override fun SaverScope.save(value: StampRallyFormState) = listOf(
            with(EntryEditMetadata.Saver) { save(value.metadata) },
            with(StateUtils.snapshotListJsonSaver<EditImage>()) { save(value.images) },
            with(EditorState.Saver) { save(value.editorState) },
            with(EntryForm2.SingleTextState.Saver) { save(value.fandom) },
            with(EntryForm2.SingleTextState.Saver) { save(value.hostTable) },
            with(EntryForm2.SingleTextState.Saver) { save(value.stateTables) },
            with(StateUtils.snapshotListJsonSaver<String>()) { save(value.tables) },
            with(EntryForm2.SingleTextState.Saver) { save(value.stateLinks) },
            with(StateUtils.snapshotListJsonSaver<LinkModel>()) { save(value.links) },
            with(EntryForm2.SingleTextState.Saver) { save(value.tableMin) },
            with(EntryForm2.SingleTextState.Saver) { save(value.totalCost) },
            with(EntryForm2.SingleTextState.Saver) { save(value.prize) },
            with(EntryForm2.SingleTextState.Saver) { save(value.prizeLimit) },
            with(EntryForm2.SingleTextState.Saver) { save(value.stateSeries) },
            with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) { save(value.series) },
            with(EntryForm2.SingleTextState.Saver) { save(value.stateMerch) },
            with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { save(value.merch) },
            with(EntryForm2.SingleTextState.Saver) { save(value.notes) },
        )

        @Suppress("UNCHECKED_CAST")
        override fun restore(value: List<Any?>) = StampRallyFormState(
            metadata = with(EntryEditMetadata.Saver) { restore(value[0] as List<Any?>) },
            images = with(StateUtils.snapshotListJsonSaver<EditImage>()) { restore(value[1] as String) },
            editorState = with(EditorState.Saver) { restore(value[2] as List<Any>) },
            fandom = with(EntryForm2.SingleTextState.Saver) { restore(value[3]!!) },
            hostTable = with(EntryForm2.SingleTextState.Saver) { restore(value[4]!!) },
            stateTables = with(EntryForm2.SingleTextState.Saver) { restore(value[5]!!) },
            tables = with(StateUtils.snapshotListJsonSaver<String>()) { restore(value[6] as String) },
            stateLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[7]!!) },
            links = with(StateUtils.snapshotListJsonSaver<LinkModel>()) { restore(value[8] as String) },
            tableMin = with(EntryForm2.SingleTextState.Saver) { restore(value[9]!!) },
            totalCost = with(EntryForm2.SingleTextState.Saver) { restore(value[10]!!) },
            prize = with(EntryForm2.SingleTextState.Saver) { restore(value[11]!!) },
            prizeLimit = with(EntryForm2.SingleTextState.Saver) { restore(value[12]!!) },
            stateSeries = with(EntryForm2.SingleTextState.Saver) { restore(value[13]!!) },
            series = with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) { restore(value[14] as String) },
            stateMerch = with(EntryForm2.SingleTextState.Saver) { restore(value[15]!!) },
            merch = with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { restore(value[16] as String) },
            notes = with(EntryForm2.SingleTextState.Saver) { restore(value[17]!!) },
        )
    }

    constructor(stampRallyId: String) : this() {
        editorState.id.value.setTextAndPlaceCursorAtEnd(stampRallyId)
    }

    fun applyDatabaseEntry(
        stampRally: StampRallyDatabaseEntry,
        seriesById: Map<String, SeriesInfo>,
        merchById: Map<String, MerchInfo>,
        mergeBehavior: FormMergeBehavior = FormMergeBehavior.IGNORE,
    ) = apply {
        editorState.applyValues(
            id = stampRally.id,
            editorNotes = stampRally.editorNotes,
            mergeBehavior = mergeBehavior,
        )

        FormUtils.applyValue(this.fandom, stampRally.fandom, mergeBehavior)
        FormUtils.applyValue(this.hostTable, stampRally.hostTable, mergeBehavior)
        FormUtils.applyValue(this.hostTable, stampRally.hostTable, mergeBehavior)
        FormUtils.applyValue(stateTables, this.tables, stampRally.tables, mergeBehavior)
        FormUtils.applyValue(
            stateLinks,
            this.links,
            stampRally.links.map(LinkModel.Companion::parse).sortedBy { it.logo },
            mergeBehavior,
        )

        // TODO: Use dropdown UI
        FormUtils.applyValue(
            this.tableMin,
            stampRally.tableMin?.serializedValue?.toString(),
            mergeBehavior,
        )
        FormUtils.applyValue(this.totalCost, stampRally.totalCost?.toString(), mergeBehavior)
        FormUtils.applyValue(this.prize, stampRally.prize, mergeBehavior)
        FormUtils.applyValue(this.prizeLimit, stampRally.prizeLimit?.toString(), mergeBehavior)

        val series = stampRally.series.map { seriesById[it] ?: SeriesInfo.fake(it) }
        FormUtils.applyValue(this.stateSeries, this.series, series, mergeBehavior)

        val merch = stampRally.merch.map { merchById[it] ?: MerchInfo.fake(it) }
        FormUtils.applyValue(this.stateMerch, this.merch, merch, mergeBehavior)

        FormUtils.applyValue(this.notes, stampRally.notes, mergeBehavior)

        metadata.lastEditor = stampRally.lastEditor
        metadata.lastEditTime = stampRally.lastEditTime
    }

    fun captureDatabaseEntry(dataYear: DataYear): Pair<List<EditImage>, StampRallyDatabaseEntry> {
        val (id, editorNotes) = editorState.captureValues()
        val links = links.toList().map { it.link }
            .plus(stateLinks.value.text.toString().takeIf { it.isNotBlank() })
            .filterNotNull()
            .distinct()
        val images = images.toList()
        return images to StampRallyDatabaseEntry(
            year = dataYear,
            id = id,
            fandom = fandom.value.text.toString(),
            hostTable = hostTable.value.text.toString(),
            tables = tables.toList(),
            links = links,
            tableMin = tableMin.value.text.toString().toIntOrNull()?.let(TableMin::parseFromValue),
            totalCost = totalCost.value.text.toString().toLongOrNull(),
            prize = prize.value.text.toString(),
            prizeLimit = prizeLimit.value.text.toString().toLongOrNull(),
            series = series.toList().map { it.id },
            merch = merch.toList().map { it.name },
            notes = notes.value.text.toString(),
            images = emptyList(),
            counter = 1,
            confirmed = false, // TODO: Is tracking confirmed still useful?
            editorNotes = editorNotes,
            lastEditor = null, // This is filled on the backend
            lastEditTime = Clock.System.now(),
        )
    }

    @Stable
    class EditorState(
        val id: EntryForm2.SingleTextState = EntryForm2.SingleTextState(
            initialLockState = EntryLockState.LOCKED,
        ),

        // TODO: Remove this field entirely
        // Intentionally prefixed with "editor" to avoid confusion with regular notes field
        val editorNotes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    ) {
        fun applyValues(
            id: String,
            editorNotes: String?,
            mergeBehavior: FormMergeBehavior,
        ) {
            this.id.value.setTextAndPlaceCursorAtEnd(id)
            FormUtils.applyValue(this.editorNotes, editorNotes, mergeBehavior)
        }

        fun captureValues() = InternalDatabaseValues(
            id = id.value.text.toString(),
            editorNotes = editorNotes.value.text.toString(),
        )

        data class InternalDatabaseValues(
            val id: String,
            val editorNotes: String?,
        )

        object Saver : ComposeSaver<EditorState, List<Any>> {
            override fun SaverScope.save(value: EditorState) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.id) },
                with(EntryForm2.SingleTextState.Saver) { save(value.editorNotes) },
            )

            override fun restore(value: List<Any>) = EditorState(
                id = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                editorNotes = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
            )
        }
    }
}
