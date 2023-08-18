package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.annotation.StringRes
import androidx.compose.runtime.compositionLocalOf

enum class VoiceActorLanguageOption(val apiValue: String, @StringRes val textRes: Int) {
    JAPANESE("Japanese", R.string.aniList_voice_actor_language_japanese),
    ENGLISH("English", R.string.aniList_voice_actor_language_english),
    KOREAN("Korean", R.string.aniList_voice_actor_language_korean),
    ITALIAN("Italian", R.string.aniList_voice_actor_language_italian),
    SPANISH("Spanish", R.string.aniList_voice_actor_language_spanish),
    PORTUGUESE("Portuguese", R.string.aniList_voice_actor_language_portuguese),
    FRENCH("French", R.string.aniList_voice_actor_language_french),
    GERMAN("German", R.string.aniList_voice_actor_language_german),
    HEBREW("Hebrew", R.string.aniList_voice_actor_language_hebrew),
    HUNGARIAN("Hungarian", R.string.aniList_voice_actor_language_hungarian),
    CHINESE("Chinese", R.string.aniList_voice_actor_language_chinese),
    ARABIC("Arabic", R.string.aniList_voice_actor_language_arabic),
    FILIPINO("Filipino", R.string.aniList_voice_actor_language_filipino),
    CATALAN("Catalan", R.string.aniList_voice_actor_language_catalan),
    FINNISH("Finnish", R.string.aniList_voice_actor_language_finnish),
    TURKISH("Turkish", R.string.aniList_voice_actor_language_turkish),
    DUTCH("Dutch", R.string.aniList_voice_actor_language_dutch),
    SWEDISH("Swedish", R.string.aniList_voice_actor_language_swedish),
    THAI("Thai", R.string.aniList_voice_actor_language_thai),
    TAGALOG("Tagalog", R.string.aniList_voice_actor_language_tagalog),
    MALAYSIAN("Malaysian", R.string.aniList_voice_actor_language_malaysian),
    INDONESIAN("Indonesian", R.string.aniList_voice_actor_language_indonesian),
    VIETNAMESE("Vietnamese", R.string.aniList_voice_actor_language_vietnamese),
    NEPALI("Nepali", R.string.aniList_voice_actor_language_nepali),
    HINDI("Hindi", R.string.aniList_voice_actor_language_hindi),
    URDU("Urdu", R.string.aniList_voice_actor_language_urdu),
}

val LocalLanguageOptionVoiceActor = compositionLocalOf { VoiceActorLanguageOption.JAPANESE to true }
