package com.thekeeperofpie.artistalleydatabase.anilist.secrets

/**
 * The secrets plugin doesn't seem to support KMP, so manually delegate to the BuildConfig here.
 */
object AniListSecrets {
    const val aniListClientId = BuildConfig.aniListclientId
}
