package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.anilist.R

/**
 * Used to close the browser tab after logging in.
 *
 * Adds an [Activity] marked `android:launchMode="singleTask"` between the previous app screen and
 * the browser tab. When the result is sent back, the share target can launch this with
 * [Intent.FLAG_ACTIVITY_CLEAR_TOP] to wipe out the tab by bringing this to the front, which will
 * also close itself, leaving the user on the previous screen.
 */
class AniListOAuthTrampolineActivity : ComponentActivity() {

    private val viewModel: TrampolineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        if (savedInstanceState != null) {
            finish()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.previouslyPaused) {
            finish()
            return
        }

        val actionIcon =
            BitmapFactory.decodeResource(application.resources, R.drawable.baseline_login_24)
        val actionButtonText = application.getString(R.string.aniList_oAuth_login_menu_action)
        val pendingIntent = PendingIntent.getActivity(
            application,
            0,
            Intent(application, AniListOAuthShareTargetActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        AniListOAuthStore.setShareTargetEnabled(application, true)
        CustomTabsIntent.Builder()
            .setActionButton(actionIcon, actionButtonText, pendingIntent)
            .build()
            .launchUrl(this, Uri.parse(AniListOAuthStore.ANILIST_OAUTH_URL))

    }

    override fun onPause() {
        super.onPause()
        viewModel.previouslyPaused = true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finish()
    }

    class TrampolineViewModel : ViewModel() {
        var previouslyPaused = false
    }
}
