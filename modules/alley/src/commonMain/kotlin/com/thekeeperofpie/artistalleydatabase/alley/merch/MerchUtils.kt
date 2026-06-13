package com.thekeeperofpie.artistalleydatabase.alley.merch

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.AcrylicBlocks
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.AirFresheners
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.ApparelOther
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Aprons
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Bags
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Bandages
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Blankets
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Bookmarks
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Buttons
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Calendars
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Candles
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Carabiners
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.CardDecks
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Charms
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.ClearFiles
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Clocks
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Coasters
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Construction
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.DakimakuraCovers
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Fans
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Handicrafts
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Hats
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.HeadAccessories
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Jackets
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Jewelry
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.JournalsNotepads
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Keycaps
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Lanyards
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.LargePosters
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.LiquidContainers
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Magnets
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Makeup
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Masks
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Mirrors
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.MusicBoxes
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.MysteryBoxes
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Pants
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Patches
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.PhoneAccessories
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.PhotocardHolders
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Photocards
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Plushies
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Prints
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Puzzles
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Ribbons
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Scarves
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Shirts
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Shoes
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Skateboards
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Skirts
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Soap
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Socks
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Stamps
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Standees
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Stickers
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Tableware
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Umbrellas
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.WallScrolls
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Wallets
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.WashiTape
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.WindChimes
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.WritingInstruments
import com.thekeeperofpie.artistalleydatabase.alley.merch.icons.Zines

object MerchUtils {

    fun toIcon(name: String, required: Boolean): ImageVector? = when (name) {
        "Acrylic blocks" -> MerchIcons.AcrylicBlocks
        "Air fresheners" -> MerchIcons.AirFresheners
        "Apparel - Other" -> MerchIcons.ApparelOther
        "Aprons" -> MerchIcons.Aprons
        "Bags" -> MerchIcons.Bags
        "Bandages" -> MerchIcons.Bandages
        "Blankets" -> MerchIcons.Blankets
        "Bookmarks" -> MerchIcons.Bookmarks
        "Buttons" -> MerchIcons.Buttons
        "Calendars" -> MerchIcons.Calendars
        "Candles" -> MerchIcons.Candles
        "Carabiners" -> MerchIcons.Carabiners
        "Card decks" -> MerchIcons.CardDecks
        "Charms" -> MerchIcons.Charms
        "Clear files" -> MerchIcons.ClearFiles
        "Clocks" -> MerchIcons.Clocks
        "Coasters" -> MerchIcons.Coasters
        "Dakimakura covers" -> MerchIcons.DakimakuraCovers
        "Fans" -> MerchIcons.Fans
        "Handicrafts" -> MerchIcons.Handicrafts
        "Hats" -> MerchIcons.Hats
        "Head accessories" -> MerchIcons.HeadAccessories
        "Jackets" -> MerchIcons.Jackets
        "Jewelry" -> MerchIcons.Jewelry
        "Journals/notepads" -> MerchIcons.JournalsNotepads
        "Key caps" -> MerchIcons.Keycaps
        "Lanyards" -> MerchIcons.Lanyards
        "Large posters" -> MerchIcons.LargePosters
        "Liquid containers" -> MerchIcons.LiquidContainers
        "Magnets" -> MerchIcons.Magnets
        "Makeup" -> MerchIcons.Makeup
        "Masks" -> MerchIcons.Masks
        "Mirrors" -> MerchIcons.Mirrors
        "Music boxes" -> MerchIcons.MusicBoxes
        "Mystery boxes" -> MerchIcons.MysteryBoxes
        "Pants" -> MerchIcons.Pants
        "Patches" -> MerchIcons.Patches
        "Phone accessories" -> MerchIcons.PhoneAccessories
        "Photocard holders" -> MerchIcons.PhotocardHolders
        "Photocards" -> MerchIcons.Photocards
        "Plushies" -> MerchIcons.Plushies
        "Prints" -> MerchIcons.Prints
        "Puzzles" -> MerchIcons.Puzzles
        "Ribbons" -> MerchIcons.Ribbons
        "Scarves" -> MerchIcons.Scarves
        "Shirts" -> MerchIcons.Shirts
        "Shoes" -> MerchIcons.Shoes
        "Skateboards" -> MerchIcons.Skateboards
        "Skirts" -> MerchIcons.Skirts
        "Soap" -> MerchIcons.Soap
        "Socks" -> MerchIcons.Socks
        "Stamps" -> MerchIcons.Stamps
        "Standees" -> MerchIcons.Standees
        "Stickers" -> MerchIcons.Stickers
        "Tableware" -> MerchIcons.Tableware
        "Umbrellas" -> MerchIcons.Umbrellas
        "Wall scrolls" -> MerchIcons.WallScrolls
        "Wallets" -> MerchIcons.Wallets
        "Washi tape" -> MerchIcons.WashiTape
        "Wind chimes" -> MerchIcons.WindChimes
        "Writing instruments" -> MerchIcons.WritingInstruments
        "Zines" -> MerchIcons.Zines
        else -> MerchIcons.Construction.takeIf { required }
    }
}
