package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anilist.type.MediaListStatus
import java.time.LocalDate

class MediaEditData {
    var status by mutableStateOf<MediaListStatus?>(null)
    var score by mutableStateOf("")
    var progress by mutableStateOf("")
    var repeat by mutableStateOf("")
    var startDate by mutableStateOf<LocalDate?>(null)
    var endDate by mutableStateOf<LocalDate?>(null)
    var priority by mutableStateOf("")
    var private by mutableStateOf(false)
    var updatedAt by mutableStateOf<Long?>(null)
    var createdAt by mutableStateOf<Long?>(null)

    var showing by mutableStateOf(false)
    var deleting by mutableStateOf(false)
    var saving by mutableStateOf(false)
    var errorRes by mutableStateOf<Pair<Int, Exception?>?>(null)
}

