package com.thekeeperofpie.artistalleydatabase.ui.theme

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.navigation.NavHostController
import artistalleydatabase.app.generated.resources.Res
import artistalleydatabase.app.generated.resources.error_launching_generic_uri
import com.thekeeperofpie.artistalleydatabase.CustomApplication
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppTheme
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

@Composable
fun AndroidTheme(
    settings: SettingsProvider,
    navHostController: NavHostController,
    content: @Composable () -> Unit,
) {
    val appTheme by settings.appTheme.collectAsState()
    val context = LocalContext.current
    val uriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            try {
                val deepLinkUri = Uri.parse(uri)
                if (deepLinkUri.getQueryParameter(UriUtils.FORCE_EXTERNAL_URI_PARAM) != "true"
                    && navHostController.graph.hasDeepLink(deepLinkUri)
                ) {
                    navHostController.navigate(deepLinkUri)
                } else {
                    val strippedUri = uri.replace("?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true", "")
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strippedUri)))
                }
            } catch (e: Exception) {
                Log.d(CustomApplication.TAG, "Error launching URI $uri", e)
                Toast.makeText(
                    context,
                    runBlocking { getString(Res.string.error_launching_generic_uri) },
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    AppTheme(appTheme =  { appTheme }) {
        CompositionLocalProvider(LocalUriHandler provides uriHandler) {
            content()
        }
    }
}
