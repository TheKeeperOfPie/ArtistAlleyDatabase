package com.thekeeperofpie.artistalleydatabase.alley.links

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Web
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site_tooltip
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online_tooltip
import com.eygraber.uri.Uri
import org.jetbrains.compose.resources.stringResource

sealed interface CommissionModel {
    val icon: ImageVector

    data object OnSite : CommissionModel {
        override val icon = Icons.Default.FormatPaint
    }

    data object Online : CommissionModel {
        override val icon = Icons.Default.Web
    }

    data class Link(val logo: Logo?, val host: String?, val link: String) : CommissionModel {
        override val icon = logo?.icon ?: Icons.Default.Link
    }

    data class Unknown(val value: String, override val icon: ImageVector = Icons.Default.Info) :
        CommissionModel

    companion object {
        fun parse(value: String) = when {
            value.equals("On-site", ignoreCase = true) -> OnSite
            value.equals("Online", ignoreCase = true) -> Online
            value.startsWith("https") -> {
                val uri = Uri.parseOrNull(value)
                val host = uri?.host?.removePrefix("www.")
                val logo = when (host) {
                    "vgen.co" -> Logo.VGEN
                    else -> null
                }
                Link(logo, host, value)
            }
            else -> Unknown(value = Uri.parseOrNull(value)?.host ?: value)
        }
    }
}

@Composable
fun CommissionModel.tooltip() = when (this) {
    is CommissionModel.Link -> link
    CommissionModel.OnSite -> stringResource(Res.string.alley_artist_commission_on_site_tooltip)
    CommissionModel.Online -> stringResource(Res.string.alley_artist_commission_online_tooltip)
    is CommissionModel.Unknown -> value
}

@Composable
fun CommissionModel.text() = when (this) {
    is CommissionModel.Link -> host ?: link
    CommissionModel.OnSite -> stringResource(Res.string.alley_artist_commission_on_site)
    CommissionModel.Online -> stringResource(Res.string.alley_artist_commission_online)
    is CommissionModel.Unknown -> value
}
