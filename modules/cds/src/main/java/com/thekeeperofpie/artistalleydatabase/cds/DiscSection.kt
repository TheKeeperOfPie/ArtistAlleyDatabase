package com.thekeeperofpie.artistalleydatabase.cds

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.compose.CustomOutlinedTextField
import com.thekeeperofpie.artistalleydatabase.compose.dropdown.DropdownMenuItem
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.FormStringR
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.DiscEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.TrackEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DiscSection(private val json: Json, lockState: LockState? = null) :
    EntrySection.Custom<List<String>>(lockState) {

    private val discs = mutableStateListOf(DiscData().apply { tracks += DiscData.TrackData() })

    override fun headerRes() = when (discs.size) {
        0 -> R.string.cd_entry_discs_header_zero
        1 -> R.string.cd_entry_discs_header_one
        else -> R.string.cd_entry_discs_header_many
    }

    @Composable
    override fun Content(lockState: LockState?) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            discs.forEachIndexed { index, disc ->
                DiscContent(disc = disc, index = index, lockState = lockState, onDone = {
                    addNewDisc(index + 1)
                })
            }
        }
    }

    @Composable
    private fun DiscContent(
        disc: DiscData,
        index: Int,
        lockState: LockState?,
        onDone: (() -> Unit)
    ) {
        var showOverflow by remember { mutableStateOf(false) }
        Box {
            DiscRow(
                disc = disc,
                index = index,
                lockState = lockState,
                onClickMore = { showOverflow = !showOverflow },
                onDone = onDone,
            )

            Box(
                Modifier
                    .width(48.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
            ) {
                DropdownMenu(
                    expanded = showOverflow,
                    onDismissRequest = { showOverflow = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(FormStringR.delete)) },
                        onClick = {
                            discs.removeAt(index)
                            showOverflow = false
                        }
                    )
                    if (index > 0) {
                        DropdownMenuItem(
                            text = { Text(stringResource(FormStringR.move_up)) },
                            onClick = {
                                val oldValue = discs[index]
                                discs[index] = discs[index - 1]
                                discs[index - 1] = oldValue
                                showOverflow = false
                            }
                        )
                    }
                    if (index < discs.size - 1) {
                        DropdownMenuItem(
                            text = { Text(stringResource(FormStringR.move_down)) },
                            onClick = {
                                val oldValue = discs[index]
                                discs[index] = discs[index + 1]
                                discs[index + 1] = oldValue
                                showOverflow = false
                            }
                        )
                    }
                }
            }
        }

        TrackContent(disc.tracks, lockState)
    }

    @Composable
    private fun DiscRow(
        disc: DiscData,
        index: Int,
        lockState: LockState?,
        onClickMore: () -> Unit = {},
        onDone: () -> Unit,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = (index + 1).toString().padStart(2, '0'),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .wrapContentWidth()
            )

            EntryTextField(
                text = disc.name,
                onValueChange = { disc.name = it },
                labelRes = R.string.cd_entry_disc_name_label,
                placeholderRes = R.string.cd_entry_disc_name_placeholder,
                onDone = onDone,
                lockState = lockState,
                modifier = Modifier.weight(1f)
            )

            EntryTextField(
                text = disc.duration,
                onValueChange = { disc.duration = it },
                labelRes = R.string.cd_entry_disc_duration_label,
                placeholderRes = R.string.cd_entry_disc_duration_placeholder,
                onDone = onDone,
                lockState = lockState,
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(max = 200.dp)
            )

            AnimatedVisibility(
                visible = lockState?.editable != false,
                enter = expandHorizontally(),
                exit = shrinkHorizontally(),
            ) {
                IconButton(onClick = onClickMore) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(
                            FormStringR.more_actions_content_description
                        ),
                    )
                }
            }
        }
    }

    @Composable
    private fun TrackContent(
        tracks: MutableList<DiscData.TrackData>,
        lockState: LockState?
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp)
        ) {
            tracks.forEachIndexed { index, track ->
                var showOverflow by remember { mutableStateOf(false) }
                Box {
                    TrackRow(
                        track = track,
                        index = index,
                        lockState = lockState,
                        onClickMore = { showOverflow = !showOverflow },
                        onDone = { tracks.add(index + 1, DiscData.TrackData()) },
                    )

                    Box(
                        Modifier
                            .width(48.dp)
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp)
                    ) {
                        DropdownMenu(
                            expanded = showOverflow,
                            onDismissRequest = { showOverflow = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(FormStringR.delete)) },
                                onClick = {
                                    tracks.removeAt(index)
                                    showOverflow = false
                                }
                            )
                            if (index > 0) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(FormStringR.move_up)) },
                                    onClick = {
                                        val oldValue = tracks[index]
                                        tracks[index] = tracks[index - 1]
                                        tracks[index - 1] = oldValue
                                        showOverflow = false
                                    }
                                )
                            }
                            if (index < tracks.size - 1) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(FormStringR.move_down)) },
                                    onClick = {
                                        val oldValue = tracks[index]
                                        tracks[index] = tracks[index + 1]
                                        tracks[index + 1] = oldValue
                                        showOverflow = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TrackRow(
        track: DiscData.TrackData,
        index: Int,
        lockState: LockState?,
        onClickMore: () -> Unit = {},
        onDone: (() -> Unit)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = (index + 1).toString().padStart(2, '0'),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .wrapContentWidth()
            )
            EntryTextField(
                text = track.title,
                onValueChange = { track.title = it },
                labelRes = R.string.cd_entry_track_title_label,
                placeholderRes = null,
                onDone = onDone,
                lockState = lockState,
                modifier = Modifier.weight(1f, fill = true)
            )
            EntryTextField(
                text = track.duration,
                onValueChange = { track.duration = it },
                labelRes = R.string.cd_entry_track_duration_label,
                placeholderRes = R.string.cd_entry_track_duration_placeholder,
                singleLine = true,
                onDone = onDone,
                lockState = lockState,
                modifier = Modifier.fillMaxHeight()
            )

            AnimatedVisibility(
                visible = lockState?.editable != false,
                enter = expandHorizontally(),
                exit = shrinkHorizontally(),
            ) {
                IconButton(onClick = onClickMore) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(
                            FormStringR.more_actions_content_description
                        ),
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun EntryTextField(
        text: String,
        onValueChange: (String) -> Unit,
        @StringRes labelRes: Int,
        @StringRes placeholderRes: Int?,
        singleLine: Boolean = false,
        onDone: (() -> Unit)? = {},
        lockState: LockState?,
        modifier: Modifier = Modifier
    ) {
        CustomOutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            label = { Text(stringResource(labelRes)) },
            placeholder = placeholderRes?.let { { Text(stringResource(it)) } },
            singleLine = singleLine,
            readOnly = lockState?.editable == false,
            modifier = modifier.focusable(lockState?.editable != false),
            keyboardOptions = onDone?.let { KeyboardOptions(imeAction = ImeAction.Done) }
                ?: KeyboardOptions.Default,
            keyboardActions = onDone?.let { KeyboardActions(onDone = { onDone() }) }
                ?: KeyboardActions.Default,
            contentPadding = TextFieldDefaults.textFieldWithoutLabelPadding(
                top = 8.dp,
                bottom = 8.dp
            ),
        )
    }

    override fun serializedValue() = discs.map {
        DiscEntry(
            name = it.name,
            duration = it.duration,
            tracks = it.tracks.mapIndexed { index, track ->
                TrackEntry(
                    number = (index + 1).toString(),
                    titles = mapOf("unknown" to track.title),
                    duration = track.duration,
                )
            }.map(json::encodeToString)
        )
    }.map(json::encodeToString)

    fun setDiscs(discs: List<DiscEntry>, lockState: LockState? = this.lockState) {
        this.lockState = lockState
        this.discs.clear()
        this.discs.addAll(
            discs.map {
                DiscData(
                    name = it.name,
                    duration = it.duration,
                ).apply {
                    it.tracks
                        .map { json.decodeFromString<TrackEntry>(it) }
                        .map {
                            it to DiscData.TrackData(
                                title = it.title,
                                duration = it.duration,
                            )
                        }
                        .sortedBy { it.first.number.toIntOrNull() }
                        .map { it.second }
                        .run(tracks::addAll)
                }
            }
        )
    }

    private fun addNewDisc(index: Int) {
        discs.add(index, DiscData().apply { tracks += DiscData.TrackData() })
    }

    class DiscData(name: String = "", duration: String = "") {
        var name by mutableStateOf(name)
        var duration by mutableStateOf(duration)

        val tracks = mutableStateListOf<TrackData>()

        class TrackData(title: String = "", duration: String = "") {
            var title by mutableStateOf(title)
            var duration by mutableStateOf(duration)
        }
    }
}

@Preview
@Composable
fun Preview() {
    val json = Json { isLenient = true }
    DiscSection(json).apply {
        setDiscs(
            listOf(
                DiscEntry(
                    name = "Disc 1 (CD) Vocal",
                    duration = "13:03",
                    tracks = listOf(
                        TrackEntry(
                            number = "01",
                            titles = mapOf("en" to "Higher's High"),
                            duration = "3:32",
                        ),
                        TrackEntry(
                            number = "02",
                            titles = mapOf(
                                "en" to "Cry Song",
                                "ja" to "クライソング",
                            ),
                            duration = "3:46",
                        ),
                        TrackEntry(
                            number = "03",
                            titles = mapOf(
                                "en" to "Drama",
                                "ja" to "ドラマ",
                            ),
                            duration = "4:13",
                        ),
                        TrackEntry(
                            number = "04",
                            titles = mapOf("en" to "Higher's High (TV Size ver.)"),
                            duration = "1:32",
                        ),
                    ).map(json::encodeToString)
                ),
                DiscEntry(
                    name = "Disc 2 (Blu-ray) Vocal, Video",
                    duration = "3:38",
                    tracks = listOf(
                        TrackEntry(
                            number = "01",
                            titles = mapOf(
                                "en" to """TV Anime "Senyoku no Sigrdrifa" × "Higher's High" Special Movie""",
                                "ja" to "TVアニメ「戦翼のシグルドリーヴァ」×「Higher's High」スペシャルムービー",
                            ),
                            duration = "3:38",
                        ),
                    ).map(json::encodeToString)
                ),
            )
        )
    }.Content(lockState = EntrySection.LockState.UNLOCKED)
}