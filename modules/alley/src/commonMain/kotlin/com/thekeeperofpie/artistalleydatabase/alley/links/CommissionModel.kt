package com.thekeeperofpie.artistalleydatabase.alley.links

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Web
import androidx.compose.ui.graphics.vector.ImageVector
import com.eygraber.uri.Uri

sealed interface CommissionModel {
    val icon: ImageVector

    data object OnSite : CommissionModel {
        override val icon = Icons.Default.FormatPaint
    }

    data object Online : CommissionModel {
        override val icon = Icons.Default.Web
    }

    data class Link(val logo: Logo?, val link: String) : CommissionModel {
        override val icon = logo?.icon ?: Icons.Default.Link
    }

    data class Unknown(val host: String, override val icon: ImageVector = Icons.Default.Info) :
        CommissionModel

    companion object {
        fun parse(value: String) = when {
            value.equals("On-site", ignoreCase = true) -> OnSite
            value.equals("Online", ignoreCase = true) -> Online
            value.startsWith("https") -> {
                val uri = Uri.parseOrNull(value)
                val host = uri?.host?.removePrefix("www.")
                when (host) {
                    "vgen.co" -> Link(Logo.VGEN, value)
                    else -> Link(null, value)
                }
            }
            else -> Unknown(host =  Uri.parseOrNull(value)?.host ?: value)
        }
    }
}
