package com.thekeeperofpie.artistalleydatabase.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.eygraber.uri.Uri
import com.eygraber.uri.toUri
import com.eygraber.uri.toUriOrNull
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
actual fun rememberImageSelectController(): ImageSelectController {
    val imageSelectState = remember { ImageSelectState() }
    val contract = remember { GetImageContentWithIndexChooser() }
    val imageSelectSingleLauncher = rememberLauncherForActivityResult(
        contract,
        imageSelectState.selections::tryEmit,
    )
    val imageSelectMultipleLauncher = rememberLauncherForActivityResult(
        GetMultipleContentsChooser,
    ) { imageSelectState.additions.tryEmit(it.map { it.toUri() }) }
    return remember(imageSelectState, imageSelectSingleLauncher, imageSelectMultipleLauncher) {
        ImageSelectController(
            imageSelectState,
            imageSelectSingleLauncher,
            imageSelectMultipleLauncher,
        )
    }
}

actual class ImageSelectController(
    val state: ImageSelectState,
    private val imageSelectSingleLauncher: ManagedActivityResultLauncher<Int, Pair<Int, Uri?>>,
    private val imageSelectMultipleLauncher: ManagedActivityResultLauncher<String, List<android.net.Uri>>,
) {
    actual fun requestNewImages() {
        imageSelectMultipleLauncher.launch("image/*")
    }

    actual fun requestNewImage(index: Int) {
        imageSelectSingleLauncher.launch(index)
    }
}

class ImageSelectState {

    val additions = MutableSharedFlow<List<Uri>>()
    val selections = MutableSharedFlow<Pair<Int, Uri?>>()
}

private object GetMultipleContentsChooser : ActivityResultContracts.GetMultipleContents() {
    @CallSuper
    override fun createIntent(context: Context, input: String): Intent {
        return Intent.createChooser(super.createIntent(context, input), null)
    }
}

private class GetImageContentWithIndexChooser : ActivityResultContract<Int, Pair<Int, Uri?>>() {
    private var chosenIndex = 0

    @CallSuper
    override fun createIntent(context: Context, input: Int): Intent {
        chosenIndex = input
        return Intent.createChooser(
            Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_OPENABLE)
                .setType("image/*"), null
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        chosenIndex to intent.takeIf { resultCode == Activity.RESULT_OK }?.data?.toUriOrNull()
}
