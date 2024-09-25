package com.thekeeperofpie.artistalleydatabase.utils_compose

import android.content.Intent
import androidx.activity.ComponentActivity
import com.thekeeperofpie.artistalleydatabase.inject.ActivityScope
import me.tatarka.inject.annotations.Inject

@ActivityScope
@Inject
actual class ShareHandler(private val activity: ComponentActivity) {
    actual fun shareUrl(title: String?, url: String) {
        val shareIntent = Intent()
            .apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
                if (title != null) {
                    putExtra(Intent.EXTRA_TITLE, title)
                }
            }
            .let { Intent.createChooser(it, null) }
        activity.startActivity(shareIntent)
    }
}
