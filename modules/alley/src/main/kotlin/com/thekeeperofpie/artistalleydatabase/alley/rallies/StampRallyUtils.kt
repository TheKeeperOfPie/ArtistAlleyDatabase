package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.thekeeperofpie.artistalleydatabase.alley.R

@Composable
fun StampRallyEntry.prizeLimitText() = prizeLimit?.toString()
    ?: stringResource(R.string.alley_stamp_rally_prize_limit_unknown)
