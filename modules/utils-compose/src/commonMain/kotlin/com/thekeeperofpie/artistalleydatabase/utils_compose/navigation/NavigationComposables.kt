package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun NavigationHeader(
    titleRes: StringResource,
    viewAllRoute: NavDestination?,
    modifier: Modifier = Modifier,
    viewAllContentDescriptionTextRes: StringResource? = null,
) {
    val navigationController = LocalNavigationController.current
    DetailsSectionHeader(
        text = stringResource(titleRes),
        onClickViewAll = viewAllRoute?.let {
            {
                navigationController.navigate(viewAllRoute)
            }
        },
        viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
        modifier = Modifier
            .clickable(enabled = viewAllRoute != null) {
                navigationController.navigate(viewAllRoute!!)
            }
            .then(modifier)
            .recomposeHighlighter()
    )
}
