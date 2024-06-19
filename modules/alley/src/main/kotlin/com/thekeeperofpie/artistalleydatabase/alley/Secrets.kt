package com.thekeeperofpie.artistalleydatabase.alley

internal object Secrets {
    /** Link to download a single cell value from the spreadsheet to serve as a current version  */
    val updateUrl = "https://docs.google.com/spreadsheets/d/${BuildConfig.sheetId}/gviz/tq" +
            "?tqx=out:csv&sheet=${BuildConfig.updateSheetName}&range=${BuildConfig.updateSheetCell}"

    /** Where to find updates to the app */
    val apkUrl = BuildConfig.apkUrl

    /** Where to find updates to the app */
    val currentSheetVersion = BuildConfig.sheetVersion
}
