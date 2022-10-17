package com.thekeeperofpie.artistalleydatabase.form

import android.content.Intent
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridModel
import java.io.File

object EntryUtils {

    fun NavGraphBuilder.entryDetailsComposable(
        route: String,
        block: @Composable (id: String, imageFile: File?, imageRatio: Float) -> Unit
    ) = composable(
        route +
                "?entry_id={entry_id}" +
                "&entry_image_file={entry_image_file}" +
                "&entry_image_ratio={entry_image_ratio}",
        arguments = listOf(
            navArgument("entry_id") {
                type = NavType.StringType
                nullable = false
            },
            navArgument("entry_image_file") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("entry_image_ratio") {
                type = NavType.FloatType
            },
        )
    ) {
        val arguments = it.arguments!!
        val id = arguments.getString("entry_id")!!
        val imageFile = arguments.getString("entry_image_file")?.let(::File)
        val imageRatio = arguments.getFloat("entry_image_ratio", 1f)
        block(id, imageFile, imageRatio)
    }

    fun NavHostController.navToEntryDetails(route: String, entry: EntryGridModel) {
        val imageRatio = entry.imageWidthToHeightRatio
        val imageFileParameter = "&entry_image_file=${entry.localImageFile?.toPath()}"
            .takeIf { entry.localImageFile != null }
            .orEmpty()
        navigate(
            route
                    + "?entry_id=${entry.id}"
                    + "&entry_image_ratio=$imageRatio"
                    + imageFileParameter
        )
    }

    @MainThread
    fun openInternalImage(navHostController: NavHostController, file: File) {
        val context = navHostController.context
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(imageUri, "image/*")
        }

        val chooserIntent = Intent.createChooser(
            intent,
            context.getString(R.string.entry_open_full_image_content_description)
        )
        context.startActivity(chooserIntent)
    }
}