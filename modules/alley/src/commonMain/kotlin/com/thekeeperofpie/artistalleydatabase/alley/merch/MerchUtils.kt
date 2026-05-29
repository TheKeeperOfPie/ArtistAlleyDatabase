package com.thekeeperofpie.artistalleydatabase.alley.merch

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.ApparelOther
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Aprons
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Bags
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Bandages
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Bookmarks
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Calendars
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Candles
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Charms
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Clocks
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Construction
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Hats
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.HeadAccessories
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Jackets
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Jewelry
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Keycaps
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Lanyards
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.LargePosters
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.LiquidContainers
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Makeup
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Mirrors
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.MusicBoxes
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.MysteryBoxes
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Pants
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.PhoneAccessories
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Plushies
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Puzzles
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Ribbons
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Scarves
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Shirts
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Shoes
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Skateboards
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Skirts
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Socks
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Stickers
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Tableware
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.WallScrolls
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.WindChimes
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.WritingInstruments

object MerchUtils {

    fun toIcon(name: String, required: Boolean): ImageVector? = when (name) {
        "Apparel - Other" -> MerchIcons.ApparelOther
        "Aprons" -> MerchIcons.Aprons
        "Bags" -> MerchIcons.Bags
        "Bandages" -> MerchIcons.Bandages
        "Bookmarks" -> MerchIcons.Bookmarks
        "Calendars" -> MerchIcons.Calendars
        "Candles" -> MerchIcons.Candles
        "Charms" -> MerchIcons.Charms
        "Clocks" -> MerchIcons.Clocks
        "Hats" -> MerchIcons.Hats
        "Head accessories" -> MerchIcons.HeadAccessories
        "Jackets" -> MerchIcons.Jackets
        "Jewelry" -> MerchIcons.Jewelry
        "Key caps" -> MerchIcons.Keycaps
        "Lanyards" -> MerchIcons.Lanyards
        "Large posters" -> MerchIcons.LargePosters
        "Liquid containers" -> MerchIcons.LiquidContainers
        "Makeup" -> MerchIcons.Makeup
        "Mirrors" -> MerchIcons.Mirrors
        "Music boxes" -> MerchIcons.MusicBoxes
        "Mystery boxes" -> MerchIcons.MysteryBoxes
        "Pants" -> MerchIcons.Pants
        "Phone accessories" -> MerchIcons.PhoneAccessories
        "Plushies" -> MerchIcons.Plushies
        "Puzzles" -> MerchIcons.Puzzles
        "Ribbons" -> MerchIcons.Ribbons
        "Scarves" -> MerchIcons.Scarves
        "Shirts" -> MerchIcons.Shirts
        "Shoes" -> MerchIcons.Shoes
        "Skateboards" -> MerchIcons.Skateboards
        "Skirts" -> MerchIcons.Skirts
        "Socks" -> MerchIcons.Socks
        "Stickers" -> MerchIcons.Stickers
        "Tableware" -> MerchIcons.Tableware
        "Wall scrolls" -> MerchIcons.WallScrolls
        "Wind chimes" -> MerchIcons.WindChimes
        "Writing instruments" -> MerchIcons.WritingInstruments
        else -> MerchIcons.Construction.takeIf { required }
    }
}
