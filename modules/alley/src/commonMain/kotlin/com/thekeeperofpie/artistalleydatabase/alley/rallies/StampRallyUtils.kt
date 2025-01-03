package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.runtime.Composable
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_prize_limit_unknown
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallyEntry.prizeLimitText() = prizeLimit?.toString()
    ?: stringResource(Res.string.alley_stamp_rally_prize_limit_unknown)
