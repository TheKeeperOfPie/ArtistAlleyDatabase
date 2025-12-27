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
    val seriesInferred: SnapshotStateList<SeriesInfo> = SnapshotStateList(),
    val seriesConfirmed: SnapshotStateList<SeriesInfo> = SnapshotStateList(),
    val merchInferred: SnapshotStateList<MerchInfo> = SnapshotStateList(),
    val merchConfirmed: SnapshotStateList<MerchInfo> = SnapshotStateList(),
    val textState: TextState = TextState(),
) {

    object Saver : ComposeSaver<ArtistFormState, List<Any?>> {
        override fun SaverScope.save(value: ArtistFormState) = listOf(
            with(Metadata.Saver) { save(value.metadata) },
            with(StateUtils.snapshotListJsonSaver<EditImage>()) { save(value.images) },
            with(StateUtils.snapshotListJsonSaver<LinkModel>()) { save(value.links) },
            with(StateUtils.snapshotListJsonSaver<LinkModel>()) { save(value.storeLinks) },
            with(StateUtils.snapshotListJsonSaver<String>()) { save(value.catalogLinks) },
            with(StateUtils.snapshotListJsonSaver<CommissionModel>()) { save(value.commissions) },
            with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) { save(value.seriesInferred) },
            with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) { save(value.seriesConfirmed) },
            with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { save(value.merchInferred) },
            with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { save(value.merchConfirmed) },
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
            seriesInferred = with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) { restore(value[6] as String) }!!,
            seriesConfirmed = with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) { restore(value[7] as String) }!!,
            merchInferred = with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { restore(value[8] as String) }!!,
            merchConfirmed = with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { restore(value[9] as String) }!!,
            textState = with(TextState.Saver) { restore(value[10] as List<Any>) }
        )
    }

    companion object {
        fun empty() = ArtistFormState(
            metadata = Metadata(),
            images = SnapshotStateList(),
            links = SnapshotStateList(),
            storeLinks = SnapshotStateList(),
            catalogLinks = SnapshotStateList(),
            commissions = SnapshotStateList(),
            seriesInferred = SnapshotStateList(),
            seriesConfirmed = SnapshotStateList(),
            merchInferred = SnapshotStateList(),
            merchConfirmed = SnapshotStateList(),
            textState = TextState(),
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

        applyValue(textState.booth, status, artist.booth, force)
        applyValue(textState.name, status, artist.name, force)
        applyValue(textState.summary, status, artist.summary, force)
        applyValue(textState.notes, status, artist.notes, force)
        applyValue(textState.editorNotes, status, artist.editorNotes, force)

        applyValue(textState.links, this.links, status, links, force)
        applyValue(textState.storeLinks, this.storeLinks, status, storeLinks, force)
        applyValue(textState.catalogLinks, this.catalogLinks, status, artist.catalogLinks, force)
        applyValue(textState.commissions, this.commissions, status, commissions, force)

        applyValue(textState.seriesInferred, this.seriesInferred, status, seriesInferred, force)
        applyValue(textState.seriesConfirmed, this.seriesConfirmed, status, seriesConfirmed, force)
        applyValue(textState.merchInferred, this.merchInferred, status, merchInferred, force)
        applyValue(textState.merchConfirmed, this.merchConfirmed, status, merchConfirmed, force)

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
        val seriesInferred = seriesInferred.toList().map { it.id }
        val seriesConfirmed = seriesConfirmed.toList().map { it.id }
        val merchInferred = merchInferred.toList().map { it.name }
        val merchConfirmed = merchConfirmed.toList().map { it.name }

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

    private fun applyValue(
        state: EntryForm2.SingleTextState,
        status: ArtistStatus,
        value: String?,
        force: Boolean,
    ) {
        val valueOrEmpty = value.orEmpty()
        if (valueOrEmpty.isNotBlank() || force) {
            state.value.setTextAndPlaceCursorAtEnd(valueOrEmpty)
        }
        if (valueOrEmpty.isNotBlank() || status.shouldStartLocked) {
            state.lockState = EntryLockState.LOCKED
        } else if (valueOrEmpty.isEmpty()) {
            state.lockState = EntryLockState.UNLOCKED
        }
    }

    private fun <T> applyValue(
        state: EntryForm2.SingleTextState,
        list: SnapshotStateList<T>,
        status: ArtistStatus,
        value: List<T>,
        force: Boolean,
    ) {
        if (value.isNotEmpty() || force) {
            list.replaceAll(value)
        }
        if (value.isNotEmpty() || status.shouldStartLocked) {
            state.lockState = EntryLockState.LOCKED
        } else if (value.isEmpty()) {
            state.lockState = EntryLockState.UNLOCKED
        }
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
        val seriesInferred: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val seriesConfirmed: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val merchInferred: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val merchConfirmed: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
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
                with(EntryForm2.SingleTextState.Saver) { save(value.seriesInferred) },
                with(EntryForm2.SingleTextState.Saver) { save(value.seriesConfirmed) },
                with(EntryForm2.SingleTextState.Saver) { save(value.merchInferred) },
                with(EntryForm2.SingleTextState.Saver) { save(value.merchConfirmed) },
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
                seriesInferred = with(EntryForm2.SingleTextState.Saver) { restore(value[9]) },
                seriesConfirmed = with(EntryForm2.SingleTextState.Saver) { restore(value[10]) },
                merchInferred = with(EntryForm2.SingleTextState.Saver) { restore(value[11]) },
                merchConfirmed = with(EntryForm2.SingleTextState.Saver) { restore(value[12]) },
                notes = with(EntryForm2.SingleTextState.Saver) { restore(value[13]) },
                editorNotes = with(EntryForm2.SingleTextState.Saver) { restore(value[14]) },
            )
        }
    }
}
