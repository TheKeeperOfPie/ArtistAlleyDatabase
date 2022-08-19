package com.thekeeperofpie.artistalleydatabase.cds

import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridModel
import java.io.File

class CdEntryGridModel(
    val value: CdEntry,
    override val localImageFile: File?,
    override val placeholderText: String,
) : EntryGridModel {

    override val id get() = value.id
    override val imageWidth get() = value.imageWidth
    override val imageWidthToHeightRatio get() = value.imageWidthToHeightRatio
}