package com.thekeeperofpie.artistalleydatabase.art.sections

import androidx.compose.runtime.toMutableStateList
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.form.EntrySection

class PrintSizeDropdown(lockState: LockState? = null) : EntrySection.Dropdown(
    R.string.art_entry_size_header,
    R.string.art_entry_size_dropdown_content_description,
    PrintSize.PORTRAITS
        .map { Item.Basic(it, it.textRes) }
        .plus(PrintSizeCustomTextFields())
        .toMutableStateList(),
    lockState,
) {

    fun initialize(printWidth: Int?, printHeight: Int?, lockState: LockState?) {
        var indexOfSize = options.indexOfFirst {
            if (it !is Item.Basic<*>) return@indexOfFirst false
            @Suppress("UNCHECKED_CAST")
            it as Item.Basic<PrintSize>
            it.value.printWidth == printWidth && it.value.printHeight == printHeight
        }

        if (indexOfSize < 0) {
            if (printWidth != null && printHeight != null) {
                indexOfSize = options.size - 1
                (options.last() as PrintSizeCustomTextFields)
                    .run {
                        width = printWidth.toString()
                        height = printHeight.toString()
                    }
            } else {
                indexOfSize = 0
            }
        }
        selectedIndex = indexOfSize
        this.lockState = lockState
    }

    fun onSizeChange(widthToHeightRatio: Float) {
        if (widthToHeightRatio > 1f) {
            PrintSize.LANDSCAPES.forEachIndexed { index, printSize ->
                options[index] = Item.Basic(printSize, printSize.textRes)
            }
        } else {
            PrintSize.PORTRAITS.forEachIndexed { index, printSize ->
                options[index] = Item.Basic(printSize, printSize.textRes)
            }
        }
    }

    fun setOptions(sizes: Iterable<PrintSize>) {
        options.clear()
        options.addAll(sizes.map { Item.Basic(it, it.textRes) } + PrintSizeCustomTextFields())
    }

    @Suppress("UNCHECKED_CAST")
    fun finalWidth() = when (val item = selectedItem()) {
        is Item.Basic<*> -> (item as Item.Basic<PrintSize>).value.printWidth
        is PrintSizeCustomTextFields -> item.width.toIntOrNull()
        else -> throw IllegalArgumentException()
    }

    @Suppress("UNCHECKED_CAST")
    fun finalHeight() = when (val item = selectedItem()) {
        is Item.Basic<*> -> (item as Item.Basic<PrintSize>).value.printHeight
        is PrintSizeCustomTextFields -> item.height.toIntOrNull()
        else -> throw IllegalArgumentException()
    }
}