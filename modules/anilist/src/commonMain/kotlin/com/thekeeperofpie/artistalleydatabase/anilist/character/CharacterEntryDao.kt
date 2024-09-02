package com.thekeeperofpie.artistalleydatabase.anilist.character

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Dao
interface CharacterEntryDao {

    @Query("""SELECT * FROM character_entries WHERE id = :id""")
    fun getEntry(id: Int): Flow<CharacterEntry?>

    @Query("""SELECT * FROM character_entries WHERE id in (:ids)""")
    fun getEntries(ids: Collection<String>): Flow<List<CharacterEntry>>

    @RawQuery([CharacterEntry::class])
    fun getEntries(query: RoomRawQuery): Flow<List<CharacterEntry>>

    fun getEntries(query: String, limit: Int = 5, offset: Int = 0): Flow<List<CharacterEntry>> {
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map { queryValue ->
                mutableListOf<String>().apply {
                    this += "name:$queryValue"
                    this += "mediaTitle:$queryValue"
                    this += "voiceActors:$queryValue"
                }
            }

        if (options.isEmpty()) {
            return emptyFlow()
        }

        val bindArguments: List<String> = options.map { it.joinToString(separator = " OR ") }

        val statement = bindArguments.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM character_entries
                JOIN character_entries_fts ON character_entries.id = character_entries_fts.id
                WHERE character_entries_fts MATCH ?
                """.trimIndent()
        } + "\n LIMIT $limit OFFSET $offset"

        return getEntries(RoomRawQuery(statement) {
            bindArguments.forEachIndexed { index, arg ->
                it.bindText(index, arg)
            }
        })
    }

    @Query("""SELECT DISTINCT (id) FROM character_entries WHERE id in (:ids)""")
    suspend fun getEntriesById(ids: Collection<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: CharacterEntry)

    @Query("DELETE FROM character_entries")
    suspend fun deleteAll()

    @Query("DELETE FROM character_entries WHERE id = :id")
    suspend fun delete(id: String)
}
