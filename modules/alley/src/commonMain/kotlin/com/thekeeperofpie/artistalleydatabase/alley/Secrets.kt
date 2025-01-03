package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.alley.secrets.BuildKonfig

internal object Secrets {
    /** Link to download a single cell value from the spreadsheet to serve as a current version  */
    val updateUrl = "https://docs.google.com/spreadsheets/d/${BuildKonfig.sheetId}/gviz/tq" +
            "?tqx=out:csv&sheet=${BuildKonfig.updateSheetName}&range=${BuildKonfig.updateSheetCell}"

    /** Where to find updates to the app */
    val apkUrl = BuildKonfig.apkUrl

    /** Where to find updates to the app */
    val currentSheetVersion = BuildKonfig.sheetVersion
}
