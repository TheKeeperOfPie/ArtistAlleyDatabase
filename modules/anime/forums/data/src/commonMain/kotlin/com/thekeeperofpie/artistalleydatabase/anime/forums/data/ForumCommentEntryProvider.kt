package com.thekeeperofpie.artistalleydatabase.anime.forums.data

interface ForumCommentEntryProvider<Comment, CommentEntry> {
    fun commentEntry(comment: Comment): CommentEntry
    fun copyCommentEntry(entry: CommentEntry, liked: Boolean): CommentEntry
    fun id(entry: CommentEntry): String
    fun liked(entry: CommentEntry): Boolean?
}
