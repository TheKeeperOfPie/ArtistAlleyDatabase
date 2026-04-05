package com.thekeeperofpie.artistalleydatabase.alley.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_libraries_title
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.jetbrains.compose.resources.stringResource

internal object AboutLibrariesScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        onNavigateBack: () -> Unit,
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    AppBar(
                        text = stringResource(Res.string.alley_libraries_title),
                        upIconOption = UpIconOption.Back(onNavigateBack),
                    )
                },
                modifier = Modifier.widthIn(max = 1200.dp),
            ) {
                val libraries by produceLibraries {
                    val allLibraries =
                        graph.aboutLibrariesProviders.fold(
                            JsonArray(emptyList()) to JsonObject(emptyMap())
                        ) { (libraries, licenses), provider ->
                            val root = Json.decodeFromString<JsonObject>(
                                provider.readBytes().decodeToString()
                            )
                            JsonArray(libraries + root["libraries"]!!.jsonArray) to
                                    JsonObject(licenses + root["licenses"]!!.jsonObject)
                        }
                    Json.encodeToString(
                        JsonObject(
                            content = mapOf(
                                "libraries" to JsonArray(allLibraries.first.toSet().toList()),
                                "licenses" to allLibraries.second
                            )
                        )
                    )
                }
                val listState = rememberLazyListState()
                val scrollAreaState = rememberScrollAreaState(listState)
                ScrollArea(
                    state = scrollAreaState,
                    modifier = Modifier
                        .padding(it)
                ) {
                    Box {
                        LibrariesContainer(
                            libraries = libraries,
                            lazyListState = listState,
                        )
                        PrimaryVerticalScrollbar(modifier = Modifier.align(Alignment.TopEnd))
                    }
                }
            }
        }
    }
}
