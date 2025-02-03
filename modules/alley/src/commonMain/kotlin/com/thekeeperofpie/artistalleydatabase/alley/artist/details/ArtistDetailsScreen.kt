package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_artist_name
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_catalog
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_links
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_links_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_merch
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_merch_unconfirmed
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_merch_unconfirmed_expand
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_merch_unconfirmed_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series_unconfirmed
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series_unconfirmed_expand
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series_unconfirmed_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_stamp_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_store
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_tags_unconfirmed_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.ui.Logos
import com.thekeeperofpie.artistalleydatabase.utils_compose.InfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.expandableListInfoText
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object ArtistDetailsScreen {

    @Composable
    operator fun invoke(
        viewModel: ArtistDetailsViewModel,
        onClickBack: () -> Unit,
        onSeriesClick: (String) -> Unit,
        onMerchClick: (String) -> Unit,
        onStampRallyClick: (StampRallyEntry) -> Unit,
        onArtistMapClick: () -> Unit,
    ) {
        val entry = viewModel.entry
        if (entry == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                CircularProgressIndicator()
            }
            return
        }

        var showFullImagesIndex by rememberSaveable { mutableStateOf<Int?>(null) }
        val artist = entry.artist
        DetailsScreen(
            title = { ArtistTitle(artist) },
            sharedElementId = artist.id,
            favorite = { entry.favorite },
            onFavoriteToggle = viewModel::onFavoriteToggle,
            images = viewModel::images,
            onClickBack = onClickBack,
            initialImageIndex = viewModel.initialImageIndex,
            onClickOpenInMap = onArtistMapClick,
        ) {
            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                InfoText(
                    stringResource(Res.string.alley_artist_details_artist_name),
                    artist.name,
                    showDividerAbove = false,
                )
            }

            val summary = artist.summary
            if (!summary.isNullOrBlank()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    InfoText(
                        label = stringResource(Res.string.alley_artist_details_description),
                        body = summary,
                        showDividerAbove = false
                    )
                }
            }

            val uriHandler = LocalUriHandler.current
            val onClickOpenUri: (String) -> Unit = {
                try {
                    uriHandler.openUri(it)
                } catch (ignored: Throwable) {
                }
            }

            if (artist.links.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_artist_details_links,
                        contentDescriptionTextRes = Res.string.alley_artist_details_links_expand_content_description,
                        values = artist.links,
                        allowExpand = false,
                        showDividerAbove = false,
                        item = { link, _, isLast -> LinkRow(link, isLast) },
                    )
                }
            }
            if (artist.storeLinks.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_artist_details_store,
                        contentDescriptionTextRes = null,
                        values = artist.storeLinks,
                        allowExpand = false,
                        showDividerAbove = false,
                        item = { link, _, isLast -> LinkRow(link, isLast) },
                    )
                }
            }

            if (artist.catalogLinks.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_artist_details_catalog,
                        contentDescriptionTextRes = null,
                        values = artist.catalogLinks,
                        valueToText = { it },
                        onClick = onClickOpenUri,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            if (entry.stampRallies.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_artist_details_stamp_rallies,
                        contentDescriptionTextRes = null,
                        values = entry.stampRallies,
                        valueToText = { it.fandom },
                        onClick = onStampRallyClick,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            ConfirmedAndInferred(
                confirmed = entry.seriesConfirmed,
                inferred = entry.seriesInferred,
                headerTextRes = Res.string.alley_artist_details_series,
                headerTextResUnconfirmed = Res.string.alley_artist_details_series_unconfirmed,
                unconfirmedIconContentDescriptionTextRes = Res.string.alley_artist_details_series_unconfirmed_icon_content_description,
                expandTextRes = Res.string.alley_artist_details_series_unconfirmed_expand,
                itemToText = { it },
                onClick = onSeriesClick,
            )

            ConfirmedAndInferred(
                confirmed = entry.artist.merchConfirmed,
                inferred = entry.artist.merchInferred,
                headerTextRes = Res.string.alley_artist_details_merch,
                headerTextResUnconfirmed = Res.string.alley_artist_details_merch_unconfirmed,
                unconfirmedIconContentDescriptionTextRes = Res.string.alley_artist_details_merch_unconfirmed_icon_content_description,
                expandTextRes = Res.string.alley_artist_details_merch_unconfirmed_expand,
                itemToText = { it },
                onClick = onMerchClick,
            )

            FilledTonalButton(
                onClick = onArtistMapClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = stringResource(Res.string.alley_open_in_map),
                    )
                    Text(stringResource(Res.string.alley_open_in_map))
                }
            }
        }
    }

    @Composable
    private fun <T> ConfirmedAndInferred(
        confirmed: List<T>,
        inferred: List<T>,
        headerTextRes: StringResource,
        headerTextResUnconfirmed: StringResource,
        unconfirmedIconContentDescriptionTextRes: StringResource,
        expandTextRes: StringResource,
        itemToText: @Composable (T) -> String,
        onClick: (T) -> Unit,
    ) {
        if (confirmed.isNotEmpty()) {
            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                expandableListInfoText(
                    labelTextRes = headerTextRes,
                    contentDescriptionTextRes = null,
                    values = confirmed,
                    valueToText = itemToText,
                    onClick = onClick,
                    allowExpand = false,
                    showDividerAbove = false,
                )
            }
        }

        if (inferred.isNotEmpty()) {
            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                val expandedByDefault = confirmed.isEmpty()
                var expanded by remember { mutableStateOf(expandedByDefault) }
                var showPopup by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .conditionally(!expandedByDefault) {
                            clickable { expanded = !expanded }
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .clickable(interactionSource = null, indication = null) {
                                showPopup = !showPopup
                            }
                    ) {
                        Text(
                            text = stringResource(headerTextResUnconfirmed),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.surfaceTint,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 4.dp)
                        )

                        Box {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(
                                    unconfirmedIconContentDescriptionTextRes
                                ),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .heightIn(max = 20.dp)
                                    .padding(top = 6.dp)
                            )

                            if (showPopup) {
                                Popup(onDismissRequest = { showPopup = false }) {
                                    Text(
                                        text = stringResource(Res.string.alley_artist_details_tags_unconfirmed_explanation),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                            .widthIn(max = 200.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (!expandedByDefault) {
                        TrailingDropdownIconButton(
                            expanded = expanded,
                            contentDescription = stringResource(expandTextRes),
                            onClick = { expanded = !expanded },
                        )
                    }
                }
                if (expanded) {
                    expandableListInfoText(
                        labelTextRes = headerTextResUnconfirmed,
                        contentDescriptionTextRes = null,
                        values = inferred,
                        valueToText = itemToText,
                        onClick = onClick,
                        allowExpand = false,
                        showDividerAbove = false,
                        header = null,
                    )
                }
            }
        }
    }

    @Composable
    private fun LinkRow(link: String, isLast: Boolean) {
        val model = parseLinkModel(link)
        val uriHandler = LocalUriHandler.current
        val bottomPadding = if (isLast) 12.dp else 8.dp
        if (model == null) {
            Text(
                text = link.removePrefix("https://"),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(link) }
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = bottomPadding,
                    )
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clickable { uriHandler.openUri(link) }
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = bottomPadding,
                    )
                    .fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(20.dp)
                        .widthIn(min = 20.dp)
                ) {
                    Icon(
                        imageVector = model.icon,
                        contentDescription = null,
                        modifier = Modifier.height(16.dp)
                    )
                }
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    private fun parseLinkModel(link: String): LinkModel? {
        val uri = Uri.parseOrNull(link) ?: return null
        val path = uri.path?.removePrefix("/")?.removeSuffix("/") ?: return null
        val host = uri.host?.removePrefix("www.") ?: return null
        return when (host) {
            "artstation.com" -> LinkModel(icon = Logos.artStation, path)
            "bsky.app" -> LinkModel(icon = Logos.bluesky, path.substringAfter("profile/"))
            "deviantart.com" -> LinkModel(icon = Logos.deviantArt, path)
            "discord.com", "discord.gg" -> LinkModel(icon = Logos.discord, "Discord")
            "etsy.com" -> LinkModel(icon = Logos.etsy, path.substringAfter("shop/"))
            "gallerynucleus.com" -> LinkModel(icon = Logos.galleryNucleus, path.substringAfter("artists/"))
            "gamejolt.com" -> LinkModel(icon = Logos.gameJolt, path)
            "inprnt.com" -> LinkModel(icon = Logos.inprnt, path.substringAfter("gallery/"))
            "instagram.com" -> LinkModel(icon = Logos.instagram, path)
            "ko-fi.com" -> LinkModel(icon = Logos.koFi, path)
            "linktr.ee" -> LinkModel(icon = Logos.linktree, path)
            "patreon.com" -> LinkModel(icon = Logos.patreon, path)
            "redbubble.com" -> LinkModel(icon = Logos.redbubble, path.substringAfter("people/"))
            "threads.net" -> LinkModel(icon = Logos.threads, path)
            "tiktok.com" -> LinkModel(icon = Logos.tikTok, path)
            "tumblr.com" -> LinkModel(icon = Logos.tumblr, path.removePrefix("blog/"))
            "twitch.tv" -> LinkModel(icon = Logos.twitch, path)
            "x.com", "twitter.com" -> LinkModel(icon = Logos.x, path)
            "youtube.com" -> LinkModel(
                icon = Logos.youTube,
                title = path.takeUnless { it.startsWith("channel/") } ?: "YouTube",
            )
            else -> when {
                host.contains("bigcartel.com") ->
                    LinkModel(icon = Logos.bigCartel, host.substringBefore(".bigcartel.com"))
                host.contains("bsky.social") ->
                    LinkModel(icon = Logos.bluesky, host.substringBefore(".bsky.social"))
                host.contains("carrd.co") ->
                    LinkModel(icon = Logos.carrd, host.substringBefore(".carrd.co"))
                host.contains("etsy.com") ->
                    LinkModel(icon = Logos.etsy, host.substringBefore(".etsy.com"))
                host.contains("faire.com") ->
                    LinkModel(icon = Logos.faire, host.substringBefore(".faire.com"))
                host.contains("gumroad.com") ->
                    LinkModel(icon = Logos.gumroad, host.substringBefore(".gumroad.com"))
                host.contains("itch.io") ->
                    LinkModel(icon = Logos.itchIo, host.substringBefore(".itch.io"))
                host.contains("myshopify.com") ->
                    LinkModel(icon = Logos.shopify, host.substringBefore(".myshopify.com"))
                host.contains("storenvy.com") ->
                    LinkModel(icon = Logos.storenvy, host.substringBefore(".storenvy.com"))
                host.contains("substack.com") ->
                    LinkModel(icon = Logos.substack, host.substringBefore(".substack.com"))
                host.contains("threadless.com") ->
                    LinkModel(icon = Logos.threadless, host.substringBefore(".threadless.com"))
                host.contains("tumblr.com") ->
                    LinkModel(icon = Logos.tumblr, host.substringBefore(".tumblr.com"))
                host.contains("weebly.com") ->
                    LinkModel(icon = Logos.weebly, host.substringBefore(".weebly.com"))
                else -> null
            }
        }
    }

    private data class LinkModel(
        val icon: ImageVector,
        val title: String,
    )
}
