package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.compose.runtime.compositionLocalOf
import artistalleydatabase.modules.anilist.generated.resources.Res
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_arabic
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_catalan
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_chinese
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_dutch
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_english
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_filipino
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_finnish
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_french
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_german
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_hebrew
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_hindi
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_hungarian
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_indonesian
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_italian
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_japanese
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_korean
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_malaysian
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_nepali
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_portuguese
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_spanish
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_swedish
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_tagalog
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_thai
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_turkish
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_urdu
import artistalleydatabase.modules.anilist.generated.resources.aniList_voice_actor_language_vietnamese
import org.jetbrains.compose.resources.StringResource

enum class VoiceActorLanguageOption(val apiValue: String, val textRes: StringResource) {
    JAPANESE("Japanese", Res.string.aniList_voice_actor_language_japanese),
    ENGLISH("English", Res.string.aniList_voice_actor_language_english),
    KOREAN("Korean", Res.string.aniList_voice_actor_language_korean),
    ITALIAN("Italian", Res.string.aniList_voice_actor_language_italian),
    SPANISH("Spanish", Res.string.aniList_voice_actor_language_spanish),
    PORTUGUESE("Portuguese", Res.string.aniList_voice_actor_language_portuguese),
    FRENCH("French", Res.string.aniList_voice_actor_language_french),
    GERMAN("German", Res.string.aniList_voice_actor_language_german),
    HEBREW("Hebrew", Res.string.aniList_voice_actor_language_hebrew),
    HUNGARIAN("Hungarian", Res.string.aniList_voice_actor_language_hungarian),
    CHINESE("Chinese", Res.string.aniList_voice_actor_language_chinese),
    ARABIC("Arabic", Res.string.aniList_voice_actor_language_arabic),
    FILIPINO("Filipino", Res.string.aniList_voice_actor_language_filipino),
    CATALAN("Catalan", Res.string.aniList_voice_actor_language_catalan),
    FINNISH("Finnish", Res.string.aniList_voice_actor_language_finnish),
    TURKISH("Turkish", Res.string.aniList_voice_actor_language_turkish),
    DUTCH("Dutch", Res.string.aniList_voice_actor_language_dutch),
    SWEDISH("Swedish", Res.string.aniList_voice_actor_language_swedish),
    THAI("Thai", Res.string.aniList_voice_actor_language_thai),
    TAGALOG("Tagalog", Res.string.aniList_voice_actor_language_tagalog),
    MALAYSIAN("Malaysian", Res.string.aniList_voice_actor_language_malaysian),
    INDONESIAN("Indonesian", Res.string.aniList_voice_actor_language_indonesian),
    VIETNAMESE("Vietnamese", Res.string.aniList_voice_actor_language_vietnamese),
    NEPALI("Nepali", Res.string.aniList_voice_actor_language_nepali),
    HINDI("Hindi", Res.string.aniList_voice_actor_language_hindi),
    URDU("Urdu", Res.string.aniList_voice_actor_language_urdu),
}

val LocalLanguageOptionVoiceActor = compositionLocalOf { VoiceActorLanguageOption.JAPANESE to true }
