package com.thekeeperofpie.artistalleydatabase.entry

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow

interface EntrySettings {

    val cropDocumentUri: MutableStateFlow<Uri?>
}