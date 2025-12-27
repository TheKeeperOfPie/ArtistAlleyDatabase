package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Stable
class ArtistFormState(
    val metadata: Metadata = Metadata(),
    val images: SnapshotStateList<EditImage> = SnapshotStateList(),
    val links: SnapshotStateList<LinkModel> = SnapshotStateList(),
    val storeLinks: SnapshotStateList<LinkModel> = SnapshotStateList(),
    val catalogLinks: SnapshotStateList<String> = SnapshotStateList(),
    val commissions: SnapshotStateList<CommissionModel> = SnapshotStateList(),
    val series: SeriesState = SeriesState(),
    val merch: MerchState = MerchState(),
    val textState: TextState = TextState(),
) {
    companion object {
        fun <T> applyValue(
            state: EntryForm2.SingleTextState,
            list: SnapshotStateList<T>,
            value: List<T>,
            force: Boolean,
        ) {
            if (value.isNotEmpty() || force) {
                list.replaceAll(value)
            }
            if (value.isNotEmpty()) {
                state.lockState = EntryLockState.LOCKED
            } else if (value.isEmpty()) {
                state.lockState = EntryLockState.UNLOCKED
            }
        }

        fun applyValue(
            state: EntryForm2.SingleTextState,
            value: String?,
            force: Boolean,
        ) {
            val valueOrEmpty = value.orEmpty()
            if (valueOrEmpty.isNotBlank() || force) {
                state.value.setTextAndPlaceCursorAtEnd(valueOrEmpty)
            }
            if (valueOrEmpty.isNotBlank()) {
                state.lockState = EntryLockState.LOCKED
            } else if (valueOrEmpty.isEmpty()) {
                state.lockState = EntryLockState.UNLOCKED
            }
        }
    }

    object Saver : ComposeSaver<ArtistFormState, List<Any?>> {
        override fun SaverScope.save(value: ArtistFormState) = listOf(
            with(Metadata.Saver) { save(value.metadata) },
            with(StateUtils.snapshotListJsonSaver<EditImage>()) { save(value.images) },
            with(StateUtils.snapshotListJsonSaver<LinkModel>()) { save(value.links) },
            with(StateUtils.snapshotListJsonSaver<LinkModel>()) { save(value.storeLinks) },
            with(StateUtils.snapshotListJsonSaver<String>()) { save(value.catalogLinks) },
            with(StateUtils.snapshotListJsonSaver<CommissionModel>()) { save(value.commissions) },
            with(SeriesState.Saver) { save(value.series) },
            with(MerchState.Saver) { save(value.merch) },
            with(TextState.Saver) { save(value.textState) },
        )

        @Suppress("UNCHECKED_CAST")
        override fun restore(value: List<Any?>) = ArtistFormState(
            metadata = with(Metadata.Saver) { restore(value[0] as List<Any?>) },
            images = with(StateUtils.snapshotListJsonSaver<EditImage>()) { restore(value[1] as String) }!!,
            links = with(StateUtils.snapshotListJsonSaver<LinkModel>()) { restore(value[2] as String) }!!,
            storeLinks = with(StateUtils.snapshotListJsonSaver<LinkModel>()) { restore(value[3] as String) }!!,
            catalogLinks = with(StateUtils.snapshotListJsonSaver<String>()) { restore(value[4] as String) }!!,
            commissions = with(StateUtils.snapshotListJsonSaver<CommissionModel>()) { restore(value[5] as String) }!!,
            series = with(SeriesState.Saver) { restore(value[4] as List<Any?>) },
            merch = with(MerchState.Saver) { restore(value[5] as List<Any?>) },
            textState = with(TextState.Saver) { restore(value[10] as List<Any>) }
        )
    }

    fun applyDatabaseEntry(
        artist: ArtistDatabaseEntry,
        seriesById: Map<String, SeriesInfo>,
        merchById: Map<String, MerchInfo>,
        force: Boolean = false,
    ) = apply {
        val links = artist.links.map(LinkModel.Companion::parse).sortedBy { it.logo }
        val storeLinks = artist.storeLinks.map(LinkModel.Companion::parse).sortedBy { it.logo }
        val commissions = artist.commissions.map(CommissionModel.Companion::parse)

        val seriesInferred =
            artist.seriesInferred.map { seriesById[it] ?: SeriesInfo.Companion.fake(it) }
        val seriesConfirmed =
            artist.seriesConfirmed.map { seriesById[it] ?: SeriesInfo.Companion.fake(it) }
        val merchInferred =
            artist.merchInferred.map { merchById[it] ?: MerchInfo.Companion.fake(it) }
        val merchConfirmed =
            artist.merchConfirmed.map { merchById[it] ?: MerchInfo.Companion.fake(it) }

        val status = artist.status
        textState.status.selectedIndex = ArtistStatus.entries.indexOf(status)

        textState.id.value.setTextAndPlaceCursorAtEnd(artist.id)
        textState.id.lockState = EntryLockState.LOCKED

        applyValue(textState.booth, artist.booth, force)
        applyValue(textState.name, artist.name, force)
        applyValue(textState.summary, artist.summary, force)
        applyValue(textState.notes, artist.notes, force)
        applyValue(textState.editorNotes, artist.editorNotes, force)

        applyValue(textState.links, this.links, links, force)
        applyValue(textState.storeLinks, this.storeLinks, storeLinks, force)
        applyValue(textState.catalogLinks, this.catalogLinks, artist.catalogLinks, force)
        applyValue(textState.commissions, this.commissions, commissions, force)

        series.applyValue(inferred = seriesInferred, confirmed = seriesConfirmed, force = force)
        merch.applyValue(inferred = merchInferred, confirmed = merchConfirmed, force = force)

        metadata.lastEditor = artist.lastEditor
        metadata.lastEditTime = artist.lastEditTime
    }

    fun captureDatabaseEntry(dataYear: DataYear): Pair<List<EditImage>, ArtistDatabaseEntry.Impl> {
        val id = Uuid.Companion.parse(textState.id.value.text.toString())
        val status = ArtistStatus.entries[textState.status.selectedIndex]

        val booth = textState.booth.value.text.toString()
        val name = textState.name.value.text.toString()
        val summary = textState.summary.value.text.toString()

        // TODO: Include pending value?
        val links = links.toList().map { it.link }
            .plus(textState.links.value.text.toString().takeIf { it.isNotBlank() })
            .filterNotNull()
            .distinct()
        val storeLinks = storeLinks.toList().map { it.link }
            .plus(textState.storeLinks.value.text.toString().takeIf { it.isNotBlank() })
            .filterNotNull()
            .distinct()
        val catalogLinks = catalogLinks.toList()
            .plus(textState.catalogLinks.value.text.toString().takeIf { it.isNotBlank() })
            .filterNotNull()
            .distinct()

        val notes = textState.notes.value.text.toString()
        val editorNotes = textState.editorNotes.value.text.toString()
        val commissions = commissions.toList().map { it.serializedValue }
            .plus(textState.commissions.value.text.toString().takeIf { it.isNotBlank() })
            .filterNotNull()
            .distinct()
        val seriesInferred = series.inferred.toList().map { it.id }
        val seriesConfirmed = series.confirmed.toList().map { it.id }
        val merchInferred = merch.inferred.toList().map { it.name }
        val merchConfirmed = merch.confirmed.toList().map { it.name }

        val images = images.toList()
        return images to ArtistDatabaseEntry.Impl(
            year = dataYear,
            id = id.toString(),
            status = status,
            booth = booth,
            name = name,
            summary = summary,
            links = links,
            storeLinks = storeLinks,
            catalogLinks = catalogLinks,
            driveLink = null,
            notes = notes,
            commissions = commissions,
            seriesInferred = seriesInferred,
            seriesConfirmed = seriesConfirmed,
            merchInferred = merchInferred,
            merchConfirmed = merchConfirmed,
            images = emptyList(),
            counter = 1,
            editorNotes = editorNotes,
            lastEditor = null, // This is filled on the backend
            lastEditTime = Clock.System.now(),
        )
    }

    @Stable
    class Metadata(
        lastEditor: String? = null,
        lastEditTime: Instant? = null,
    ) {
        var lastEditor by mutableStateOf(lastEditor)
        var lastEditTime by mutableStateOf(lastEditTime)

        object Saver : ComposeSaver<Metadata, List<Any?>> {
            override fun SaverScope.save(value: Metadata) = listOf(
                value.lastEditor,
                value.lastEditTime?.toString()
            )

            override fun restore(value: List<Any?>): Metadata {
                val (lastEditor, lastEditTime) = value
                return Metadata(
                    lastEditor = lastEditor as String?,
                    lastEditTime = (lastEditTime as? String?)?.let(Instant.Companion::parseOrNull)
                )
            }
        }
    }

    @Stable
    class TextState(
        val id: EntryForm2.SingleTextState = EntryForm2.SingleTextState(
            initialLockState = EntryLockState.LOCKED,
        ),
        val status: EntryForm2.DropdownState = EntryForm2.DropdownState(),
        val booth: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val name: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val summary: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val links: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val storeLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val catalogLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val commissions: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val notes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val editorNotes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    ) {
        object Saver : ComposeSaver<TextState, List<Any>> {
            override fun SaverScope.save(value: TextState) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.id) },
                with(EntryForm2.DropdownState.Saver) { save(value.status) },
                with(EntryForm2.SingleTextState.Saver) { save(value.booth) },
                with(EntryForm2.SingleTextState.Saver) { save(value.name) },
                with(EntryForm2.SingleTextState.Saver) { save(value.summary) },
                with(EntryForm2.SingleTextState.Saver) { save(value.links) },
                with(EntryForm2.SingleTextState.Saver) { save(value.storeLinks) },
                with(EntryForm2.SingleTextState.Saver) { save(value.catalogLinks) },
                with(EntryForm2.SingleTextState.Saver) { save(value.commissions) },
                with(EntryForm2.SingleTextState.Saver) { save(value.notes) },
                with(EntryForm2.SingleTextState.Saver) { save(value.editorNotes) },
            )

            override fun restore(value: List<Any>) = TextState(
                id = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                status = with(EntryForm2.DropdownState.Saver) { restore(value[1]) },
                booth = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
                name = with(EntryForm2.SingleTextState.Saver) { restore(value[3]) },
                summary = with(EntryForm2.SingleTextState.Saver) { restore(value[4]) },
                links = with(EntryForm2.SingleTextState.Saver) { restore(value[5]) },
                storeLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[6]) },
                catalogLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[7]) },
                commissions = with(EntryForm2.SingleTextState.Saver) { restore(value[8]) },
                notes = with(EntryForm2.SingleTextState.Saver) { restore(value[9]) },
                editorNotes = with(EntryForm2.SingleTextState.Saver) { restore(value[10]) },
            )
        }
    }

    @Stable
    class SeriesState(
        val stateInferred: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val stateConfirmed: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val inferred: SnapshotStateList<SeriesInfo> = SnapshotStateList(),
        val confirmed: SnapshotStateList<SeriesInfo> = SnapshotStateList(),
    ) {
        fun applyValue(
            inferred: List<SeriesInfo>,
            confirmed: List<SeriesInfo>,
            force: Boolean,
        ) {
            applyValue(this.stateInferred, this.inferred, inferred, force)
            applyValue(this.stateConfirmed, this.confirmed, confirmed, force)
        }

        fun captureInferredAndConfirmed(): Pair<List<String>, List<String>> =
            inferred.toList().map { it.id } to confirmed.toList().map { it.id }

        object Saver : ComposeSaver<SeriesState, List<Any?>> {
            override fun SaverScope.save(value: SeriesState) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.stateInferred) },
                with(EntryForm2.SingleTextState.Saver) { save(value.stateConfirmed) },
                with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) { save(value.inferred) },
                with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) { save(value.confirmed) },
            )

            override fun restore(value: List<Any?>) = SeriesState(
                stateInferred = with(EntryForm2.SingleTextState.Saver) { restore(value[0] as Any) },
                stateConfirmed = with(EntryForm2.SingleTextState.Saver) { restore(value[1] as Any) },
                inferred = with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) {
                    restore(value[2] as String)
                }!!,
                confirmed = with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) {
                    restore(value[3] as String)
                }!!,
            )
        }
    }

    @Stable
    class MerchState(
        val stateInferred: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val stateConfirmed: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val inferred: SnapshotStateList<MerchInfo> = SnapshotStateList(),
        val confirmed: SnapshotStateList<MerchInfo> = SnapshotStateList(),
    ) {
        fun applyValue(
            inferred: List<MerchInfo>,
            confirmed: List<MerchInfo>,
            force: Boolean,
        ) {
            applyValue(this.stateInferred, this.inferred, inferred, force)
            applyValue(this.stateConfirmed, this.confirmed, confirmed, force)
        }

        fun captureInferredAndConfirmed(): Pair<List<String>, List<String>> =
            inferred.toList().map { it.name } to confirmed.toList().map { it.name }

        object Saver : ComposeSaver<MerchState, List<Any?>> {
            override fun SaverScope.save(value: MerchState) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.stateInferred) },
                with(EntryForm2.SingleTextState.Saver) { save(value.stateConfirmed) },
                with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { save(value.inferred) },
                with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { save(value.confirmed) },
            )

            override fun restore(value: List<Any?>) = MerchState(
                stateInferred = with(EntryForm2.SingleTextState.Saver) { restore(value[0] as Any) },
                stateConfirmed = with(EntryForm2.SingleTextState.Saver) { restore(value[1] as Any) },
                inferred = with(StateUtils.snapshotListJsonSaver<MerchInfo>()) {
                    restore(value[2] as String)
                }!!,
                confirmed = with(StateUtils.snapshotListJsonSaver<MerchInfo>()) {
                    restore(value[3] as String)
                }!!,
            )
        }
    }
}
