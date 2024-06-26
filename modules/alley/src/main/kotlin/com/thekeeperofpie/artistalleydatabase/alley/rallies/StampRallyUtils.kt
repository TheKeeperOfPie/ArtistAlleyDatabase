package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.thekeeperofpie.artistalleydatabase.alley.R

@Composable
fun StampRallyEntry.prizeLimitText() = prizeLimit?.toString()
    ?: stringResource(R.string.alley_stamp_rally_prize_limit_unknown)

@Composable
fun StampRallyEntry.tableMinText() = when {
    tableMin == null -> stringResource(R.string.alley_stamp_rally_minimum_free)
    tableMin < 0 -> stringResource(R.string.alley_stamp_rally_minimum_unknown)
    tableMin == 0 -> stringResource(R.string.alley_stamp_rally_minimum_any)
    else -> stringResource(R.string.alley_stamp_rally_minimum_usd, tableMin)
}
