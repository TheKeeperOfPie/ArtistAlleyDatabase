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
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.stringResource

@Serializable
sealed interface CommissionModel {
    val icon: ImageVector
    val serializedValue: String

    @Serializable
    data object OnSite : CommissionModel {
        override val icon = Icons.Default.FormatPaint
        override val serializedValue get() = "On-site"
    }

    @Serializable
    data object Online : CommissionModel {
        override val icon = Icons.Default.Web
        override val serializedValue get() = "Online"
    }

    @Serializable
    data class Link(val logo: Logo?, val host: String?, val link: String) : CommissionModel {
        @Transient
        override val icon = logo?.icon ?: Icons.Default.Link
        override val serializedValue get() = link
    }

    @Serializable
    data class Unknown(val value: String) : CommissionModel {
        @Transient
        override val icon: ImageVector = Icons.Default.Info
        override val serializedValue get() = value
    }

    companion object {
        fun parse(value: String) = when {
            value.equals(OnSite.serializedValue, ignoreCase = true) -> OnSite
            value.equals(Online.serializedValue, ignoreCase = true) -> Online
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
