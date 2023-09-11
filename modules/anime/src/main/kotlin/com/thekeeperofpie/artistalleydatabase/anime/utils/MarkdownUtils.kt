package com.thekeeperofpie.artistalleydatabase.anime.utils

import com.thekeeperofpie.artistalleydatabase.compose.StableSpanned
import io.noties.markwon.Markwon

fun Markwon.toStableMarkdown(input: String) = StableSpanned(toMarkdown(input))
