package com.thekeeperofpie.artistalleydatabase.anime.characters.data

// TODO: Gotta be a better way to model this
interface CharacterEntryProvider<Character, CharacterEntry, MediaEntry> {
    /** Proxies to a real type to decouple the character data class from recommendations */
    fun characterEntry(character: Character, media: List<MediaEntry>): CharacterEntry
    fun id(characterEntry: CharacterEntry): String
    fun media(characterEntry: CharacterEntry) : List<MediaEntry>
    fun copyCharacterEntry(entry: CharacterEntry, media: List<MediaEntry>): CharacterEntry
}
