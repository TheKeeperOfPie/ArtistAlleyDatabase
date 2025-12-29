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
    val editorState: EditorState = EditorState(),
    val info: InfoState = InfoState(),
    val links: LinksState = LinksState(),
    val series: SeriesState = SeriesState(),
    val merch: MerchState = MerchState(),
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
            with(EditorState.Saver) { save(value.editorState) },
            with(InfoState.Saver) { save(value.info) },
            with(LinksState.Saver) { save(value.links) },
            with(SeriesState.Saver) { save(value.series) },
            with(MerchState.Saver) { save(value.merch) },
        )

        @Suppress("UNCHECKED_CAST")
        override fun restore(value: List<Any?>) = ArtistFormState(
            metadata = with(Metadata.Saver) { restore(value[0] as List<Any?>) },
            images = with(StateUtils.snapshotListJsonSaver<EditImage>()) { restore(value[1] as String) },
            editorState = with(EditorState.Saver) { restore(value[2] as List<Any>) },
            info = with(InfoState.Saver) { restore(value[3] as List<Any>) },
            links = with(LinksState.Saver) { restore(value[4] as List<Any>) },
            series = with(SeriesState.Saver) { restore(value[5] as List<Any?>) },
            merch = with(MerchState.Saver) { restore(value[6] as List<Any>) },
        )
    }

    constructor(artistId: Uuid): this() {
        editorState.id.value.setTextAndPlaceCursorAtEnd(artistId.toString())
    }

    fun applyDatabaseEntry(
        artist: ArtistDatabaseEntry,
        seriesById: Map<String, SeriesInfo>,
        merchById: Map<String, MerchInfo>,
        force: Boolean = false,
    ) = apply {
        val status = artist.status
        editorState.applyValues(
            id = artist.id,
            status = status,
            editorNotes = artist.editorNotes,
            force = force,
        )

        info.applyValues(
            booth = artist.booth,
            name = artist.name,
            summary = artist.summary,
            notes = artist.notes,
            force = force,
        )

        links.applyRawValues(
            links = artist.links,
            storeLinks = artist.storeLinks,
            catalogLinks = artist.catalogLinks,
            commissions = artist.commissions,
            force = force,
        )

        series.applyValues(
            inferred = artist.seriesInferred,
            confirmed = artist.seriesConfirmed,
            seriesById = seriesById,
            force = force,
        )
        merch.applyValues(
            inferred = artist.merchInferred,
            confirmed = artist.merchConfirmed,
            merchById = merchById,
            force = force,
        )

        metadata.lastEditor = artist.lastEditor
        metadata.lastEditTime = artist.lastEditTime
    }

    fun captureDatabaseEntry(dataYear: DataYear): Pair<List<EditImage>, ArtistDatabaseEntry.Impl> {
        val (id, status, editorNotes) = editorState.captureValues()
        val (booth, name, summary, notes) = info.captureValues()
        val (links, storeLinks, catalogLinks, commissions) = links.captureValues()

        val (seriesInferred, seriesConfirmed) = series.captureValues()
        val (merchInferred, merchConfirmed) = merch.captureValues()

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
    class EditorState(
        val id: EntryForm2.SingleTextState = EntryForm2.SingleTextState(
            initialLockState = EntryLockState.LOCKED,
        ),
        val status: EntryForm2.DropdownState = EntryForm2.DropdownState(),

        // TODO: Remove this field entirely
        // Intentionally prefixed with "editor" to avoid confusion with regular notes field
        val editorNotes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    ) {
        fun applyValues(
            id: String,
            status: ArtistStatus,
            editorNotes: String?,
            force: Boolean,
        ) {
            this.id.value.setTextAndPlaceCursorAtEnd(id)
            this.status.selectedIndex = ArtistStatus.entries.indexOf(status)
            applyValue(this.editorNotes, editorNotes, force)
        }

        fun captureValues() = InternalDatabaseValues(
            id = Uuid.parse(id.value.text.toString()),
            status = ArtistStatus.entries[status.selectedIndex],
            editorNotes = editorNotes.value.text.toString(),
        )

        data class InternalDatabaseValues(
            val id: Uuid,
            val status: ArtistStatus,
            val editorNotes: String?,
        )

        object Saver : ComposeSaver<EditorState, List<Any>> {
            override fun SaverScope.save(value: EditorState) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.id) },
                with(EntryForm2.DropdownState.Saver) { save(value.status) },
                with(EntryForm2.SingleTextState.Saver) { save(value.editorNotes) },
            )

            override fun restore(value: List<Any>) = EditorState(
                id = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                status = with(EntryForm2.DropdownState.Saver) { restore(value[1]) },
                editorNotes = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
            )
        }
    }

    @Stable
    class InfoState(
        val booth: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val name: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val summary: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val notes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    ) {
        fun applyValues(
            booth: String?,
            name: String,
            summary: String?,
            notes: String?,
            force: Boolean,
        ) {
            applyValue(this.booth, booth, force)
            applyValue(this.name, name, force)
            applyValue(this.summary, summary, force)
            applyValue(this.notes, notes, force)
        }

        fun captureValues() = InfoDatabaseValues(
            booth = booth.value.text.toString(),
            name = name.value.text.toString(),
            summary = summary.value.text.toString(),
            notes = notes.value.text.toString(),
        )

        data class InfoDatabaseValues(
            val booth: String?,
            val name: String,
            val summary: String?,
            val notes: String?,
        )

        object Saver : ComposeSaver<InfoState, List<Any>> {
            override fun SaverScope.save(value: InfoState) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.booth) },
                with(EntryForm2.SingleTextState.Saver) { save(value.name) },
                with(EntryForm2.SingleTextState.Saver) { save(value.summary) },
                with(EntryForm2.SingleTextState.Saver) { save(value.notes) },
            )

            override fun restore(value: List<Any>) = InfoState(
                booth = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                name = with(EntryForm2.SingleTextState.Saver) { restore(value[1]) },
                summary = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
                notes = with(EntryForm2.SingleTextState.Saver) { restore(value[3]) },
            )
        }
    }

    @Stable
    class LinksState(
        val stateLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val stateStoreLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val stateCatalogLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val stateCommissions: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val links: SnapshotStateList<LinkModel> = SnapshotStateList(),
        val storeLinks: SnapshotStateList<LinkModel> = SnapshotStateList(),
        val catalogLinks: SnapshotStateList<String> = SnapshotStateList(),
        val commissions: SnapshotStateList<CommissionModel> = SnapshotStateList(),
    ) {
        fun applyValues(
            links: List<LinkModel>,
            storeLinks: List<LinkModel>,
            catalogLinks: List<String>,
            commissions: List<CommissionModel>,
            force: Boolean,
        ) {
            applyValue(stateLinks, this.links, links, force)
            applyValue(stateStoreLinks, this.storeLinks, storeLinks, force)
            applyValue(stateCatalogLinks, this.catalogLinks, catalogLinks, force)
            applyValue(stateCommissions, this.commissions, commissions, force)
        }

        fun applyRawValues(
            links: List<String>,
            storeLinks: List<String>,
            catalogLinks: List<String>,
            commissions: List<String>,
            force: Boolean,
        ) = applyValues(
            links = links.map(LinkModel.Companion::parse).sortedBy { it.logo },
            storeLinks = storeLinks.map(LinkModel.Companion::parse).sortedBy { it.logo },
            catalogLinks = catalogLinks,
            commissions = commissions.map(CommissionModel.Companion::parse),
            force = force,
        )

        fun captureValues(): LinksDatabaseValues {
            val links = links.toList().map { it.link }
                .plus(stateLinks.value.text.toString().takeIf { it.isNotBlank() })
                .filterNotNull()
                .distinct()
            val storeLinks = storeLinks.toList().map { it.link }
                .plus(stateStoreLinks.value.text.toString().takeIf { it.isNotBlank() })
                .filterNotNull()
                .distinct()
            val catalogLinks = catalogLinks.toList()
                .plus(stateCatalogLinks.value.text.toString().takeIf { it.isNotBlank() })
                .filterNotNull()
                .distinct()
            val commissions = commissions.toList().map { it.serializedValue }
                .plus(stateCommissions.value.text.toString().takeIf { it.isNotBlank() })
                .filterNotNull()
                .distinct()
            return LinksDatabaseValues(
                links = links,
                storeLinks = storeLinks,
                catalogLinks = catalogLinks,
                commissions = commissions,
            )
        }

        data class LinksDatabaseValues(
            val links: List<String>,
            val storeLinks: List<String>,
            val catalogLinks: List<String>,
            val commissions: List<String>,
        )

        object Saver : ComposeSaver<LinksState, List<Any>> {
            override fun SaverScope.save(value: LinksState) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.stateLinks) },
                with(EntryForm2.SingleTextState.Saver) { save(value.stateStoreLinks) },
                with(EntryForm2.SingleTextState.Saver) { save(value.stateCatalogLinks) },
                with(EntryForm2.SingleTextState.Saver) { save(value.stateCommissions) },
                with(StateUtils.snapshotListJsonSaver<LinkModel>()) { save(value.links) },
                with(StateUtils.snapshotListJsonSaver<LinkModel>()) { save(value.storeLinks) },
                with(StateUtils.snapshotListJsonSaver<String>()) { save(value.catalogLinks) },
                with(StateUtils.snapshotListJsonSaver<CommissionModel>()) { save(value.commissions) },
            )

            override fun restore(value: List<Any>) = LinksState(
                stateLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                stateStoreLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[1]) },
                stateCatalogLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
                stateCommissions = with(EntryForm2.SingleTextState.Saver) { restore(value[3]) },
                links = with(StateUtils.snapshotListJsonSaver<LinkModel>()) { restore(value[4] as String) },
                storeLinks = with(StateUtils.snapshotListJsonSaver<LinkModel>()) { restore(value[5] as String) },
                catalogLinks = with(StateUtils.snapshotListJsonSaver<String>()) { restore(value[6] as String) },
                commissions = with(StateUtils.snapshotListJsonSaver<CommissionModel>()) {
                    restore(value[7] as String)
                },
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
        fun applyValues(
            inferred: List<SeriesInfo>,
            confirmed: List<SeriesInfo>,
            force: Boolean,
        ) {
            applyValue(this.stateInferred, this.inferred, inferred, force)
            applyValue(this.stateConfirmed, this.confirmed, confirmed, force)
        }

        fun applyValues(
            inferred: List<String>,
            confirmed: List<String>,
            seriesById: Map<String, SeriesInfo>,
            force: Boolean,
        ) = applyValues(
            inferred = inferred.map { seriesById[it] ?: SeriesInfo.fake(it) },
            confirmed = confirmed.map { seriesById[it] ?: SeriesInfo.fake(it) },
            force = force,
        )

        fun captureValues(): Pair<List<String>, List<String>> =
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
                },
                confirmed = with(StateUtils.snapshotListJsonSaver<SeriesInfo>()) {
                    restore(value[3] as String)
                },
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
        fun applyValues(
            inferred: List<MerchInfo>,
            confirmed: List<MerchInfo>,
            force: Boolean,
        ) {
            applyValue(this.stateInferred, this.inferred, inferred, force)
            applyValue(this.stateConfirmed, this.confirmed, confirmed, force)
        }

        fun applyValues(
            inferred: List<String>,
            confirmed: List<String>,
            merchById: Map<String, MerchInfo>,
            force: Boolean,
        ) = applyValues(
            inferred = inferred.map { merchById[it] ?: MerchInfo.fake(it) },
            confirmed = confirmed.map { merchById[it] ?: MerchInfo.fake(it) },
            force = force,
        )

        fun captureValues(): Pair<List<String>, List<String>> =
            inferred.toList().map { it.name } to confirmed.toList().map { it.name }

        object Saver : ComposeSaver<MerchState, List<Any>> {
            override fun SaverScope.save(value: MerchState) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.stateInferred) },
                with(EntryForm2.SingleTextState.Saver) { save(value.stateConfirmed) },
                with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { save(value.inferred) },
                with(StateUtils.snapshotListJsonSaver<MerchInfo>()) { save(value.confirmed) },
            )

            override fun restore(value: List<Any>) = MerchState(
                stateInferred = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                stateConfirmed = with(EntryForm2.SingleTextState.Saver) { restore(value[1]) },
                inferred = with(StateUtils.snapshotListJsonSaver<MerchInfo>()) {
                    restore(value[2] as String)
                },
                confirmed = with(StateUtils.snapshotListJsonSaver<MerchInfo>()) {
                    restore(value[3] as String)
                },
            )
        }
    }
}
