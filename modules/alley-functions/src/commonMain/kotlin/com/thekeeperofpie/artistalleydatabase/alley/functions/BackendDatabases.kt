package com.thekeeperofpie.artistalleydatabase.alley.functions

import androidx.annotation.VisibleForTesting
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.backend.data.ArtistCatalogQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.backend.data.BackendColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.form.data.ArtistFormEntry
import com.thekeeperofpie.artistalleydatabase.alley.form.data.ArtistFormEntryHistory
import com.thekeeperofpie.artistalleydatabase.alley.form.data.ArtistFormNonce
import com.thekeeperofpie.artistalleydatabase.alley.form.data.ArtistFormPublicKey
import com.thekeeperofpie.artistalleydatabase.alley.form.data.StampRallyFormEntry
import com.thekeeperofpie.artistalleydatabase.alley.form.data.StampRallyFormEntryHistory
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.AuthToken
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.RefreshToken
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user.UserId
import com.thekeeperofpie.artistalleydatabase.alley.functions.form.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.functions.user.AlleyUserDatabase
import com.thekeeperofpie.artistalleydatabase.alley.user.AuthCode
import com.thekeeperofpie.artistalleydatabase.alley.user.User
import com.thekeeperofpie.artistalleydatabase.alley.user.UserArtist
import com.thekeeperofpie.artistalleydatabase.alley.user.UserGeneration
import com.thekeeperofpie.artistalleydatabase.alley.user.UserMerch
import com.thekeeperofpie.artistalleydatabase.alley.user.UserSeries
import com.thekeeperofpie.artistalleydatabase.alley.user.UserSession
import com.thekeeperofpie.artistalleydatabase.alley.user.UserStampRally

object BackendDatabases {

    private val userIdAdapter = object : ColumnAdapter<UserId, String> {
        override fun decode(databaseValue: String) = UserId(databaseValue)
        override fun encode(value: UserId) = value.obfuscatedId
    }

    internal fun editDatabase(driver: SqlDriver) = AlleySqlDatabase(
        driver = driver,
        artistCatalogQueueEntryAdapter = ArtistCatalogQueueEntry.Adapter(
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            artistIdAdapter = ColumnAdapters.uuidAdapter,
        ),
        artistEntryAdapter = ColumnAdapters.artistEntryAdapter,
        artistHistoryEntryAdapter = BackendColumnAdapters.artistHistoryEntryAdapter,
        artistRemoteDataAdapter = BackendColumnAdapters.artistRemoteDataAdapter,
        artistRemoteDataHistoryAdapter = BackendColumnAdapters.artistRemoteDataHistoryAdapter,
        seriesEntryAdapter = ColumnAdapters.seriesEntryAdapter,
        stampRallyEntryAdapter = ColumnAdapters.stampRallyEntryAdapter,
        stampRallyHistoryEntryAdapter = BackendColumnAdapters.stampRallyHistoryEntryAdapter,
        stampRallyQueueEntryAdapter = BackendColumnAdapters.stampRallyQueueEntryAdapter,
    )

    internal fun formDatabase(driver: SqlDriver) = AlleyFormDatabase(
        driver = driver,
        artistFormEntryAdapter = ArtistFormEntry.Adapter(
            artistIdAdapter = ColumnAdapters.uuidAdapter,
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            beforeSocialLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeStoreLinksAdapter = ColumnAdapters.listStringAdapter,
            beforePortfolioLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeCatalogLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeCommissionsAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesInferredAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchInferredAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchConfirmedAdapter = ColumnAdapters.listStringAdapter,
            beforeImagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
            beforeProfileImageAdapter = ColumnAdapters.databaseImageAdapter,
            afterSocialLinksAdapter = ColumnAdapters.listStringAdapter,
            afterStoreLinksAdapter = ColumnAdapters.listStringAdapter,
            afterPortfolioLinksAdapter = ColumnAdapters.listStringAdapter,
            afterCatalogLinksAdapter = ColumnAdapters.listStringAdapter,
            afterCommissionsAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesInferredAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
            afterMerchInferredAdapter = ColumnAdapters.listStringAdapter,
            afterMerchConfirmedAdapter = ColumnAdapters.listStringAdapter,
            afterImagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
            afterProfileImageAdapter = ColumnAdapters.databaseImageAdapter,
            timestampAdapter = ColumnAdapters.instantAdapter,
        ),
        artistFormEntryHistoryAdapter = ArtistFormEntryHistory.Adapter(
            artistIdAdapter = ColumnAdapters.uuidAdapter,
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            beforeSocialLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeStoreLinksAdapter = ColumnAdapters.listStringAdapter,
            beforePortfolioLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeCatalogLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeCommissionsAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesInferredAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchInferredAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchConfirmedAdapter = ColumnAdapters.listStringAdapter,
            beforeImagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
            beforeProfileImageAdapter = ColumnAdapters.databaseImageAdapter,
            afterSocialLinksAdapter = ColumnAdapters.listStringAdapter,
            afterStoreLinksAdapter = ColumnAdapters.listStringAdapter,
            afterPortfolioLinksAdapter = ColumnAdapters.listStringAdapter,
            afterCatalogLinksAdapter = ColumnAdapters.listStringAdapter,
            afterCommissionsAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesInferredAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
            afterMerchInferredAdapter = ColumnAdapters.listStringAdapter,
            afterMerchConfirmedAdapter = ColumnAdapters.listStringAdapter,
            afterImagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
            afterProfileImageAdapter = ColumnAdapters.databaseImageAdapter,
            timestampAdapter = ColumnAdapters.instantAdapter,
        ),
        artistFormNonceAdapter = ArtistFormNonce.Adapter(
            artistIdAdapter = ColumnAdapters.uuidAdapter,
            nonceAdapter = ColumnAdapters.uuidAdapter,
            timestampAdapter = ColumnAdapters.instantAdapter,
        ),
        artistFormPublicKeyAdapter = ArtistFormPublicKey.Adapter(
            artistIdAdapter = ColumnAdapters.uuidAdapter,
        ),
        stampRallyFormEntryAdapter = StampRallyFormEntry.Adapter(
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            artistIdAdapter = ColumnAdapters.uuidAdapter,
            beforeTablesAdapter = ColumnAdapters.listStringAdapter,
            beforeStartTablesAdapter = ColumnAdapters.setStringAdapter,
            beforeEndTablesAdapter = ColumnAdapters.setStringAdapter,
            beforeLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeTableMinAdapter = ColumnAdapters.tableMinAdapter,
            beforePrizeMerchAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchAdapter = ColumnAdapters.listStringAdapter,
            beforeImagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
            afterTablesAdapter = ColumnAdapters.listStringAdapter,
            afterLinksAdapter = ColumnAdapters.listStringAdapter,
            afterTableMinAdapter = ColumnAdapters.tableMinAdapter,
            afterStartTablesAdapter = ColumnAdapters.setStringAdapter,
            afterEndTablesAdapter = ColumnAdapters.setStringAdapter,
            afterPrizeMerchAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesAdapter = ColumnAdapters.listStringAdapter,
            afterMerchAdapter = ColumnAdapters.listStringAdapter,
            afterImagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
            timestampAdapter = ColumnAdapters.instantAdapter,
        ),
        stampRallyFormEntryHistoryAdapter = StampRallyFormEntryHistory.Adapter(
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            artistIdAdapter = ColumnAdapters.uuidAdapter,
            beforeTablesAdapter = ColumnAdapters.listStringAdapter,
            beforeStartTablesAdapter = ColumnAdapters.setStringAdapter,
            beforeEndTablesAdapter = ColumnAdapters.setStringAdapter,
            beforeLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeTableMinAdapter = ColumnAdapters.tableMinAdapter,
            beforePrizeMerchAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchAdapter = ColumnAdapters.listStringAdapter,
            beforeImagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
            afterTablesAdapter = ColumnAdapters.listStringAdapter,
            afterStartTablesAdapter = ColumnAdapters.setStringAdapter,
            afterEndTablesAdapter = ColumnAdapters.setStringAdapter,
            afterLinksAdapter = ColumnAdapters.listStringAdapter,
            afterTableMinAdapter = ColumnAdapters.tableMinAdapter,
            afterPrizeMerchAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesAdapter = ColumnAdapters.listStringAdapter,
            afterMerchAdapter = ColumnAdapters.listStringAdapter,
            afterImagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
            timestampAdapter = ColumnAdapters.instantAdapter,
        ),
    )

    @VisibleForTesting
    fun userDatabase(driver: SqlDriver) = AlleyUserDatabase(
        driver = driver,
        authCodeAdapter = AuthCode.Adapter(userIdAdapter = userIdAdapter),
        userAdapter = User.Adapter(
            userIdAdapter = userIdAdapter,
            createdAtAdapter = ColumnAdapters.instantEpochSecondsAdapter,
            refreshTokenAdapter = object : ColumnAdapter<RefreshToken, String> {
                override fun decode(databaseValue: String) = RefreshToken(databaseValue)
                override fun encode(value: RefreshToken) = value.token
            },
        ),
        userArtistAdapter = UserArtist.Adapter(
            userIdAdapter = userIdAdapter,
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            updatedAtAdapter = ColumnAdapters.instantEpochSecondsAdapter,
        ),
        userGenerationAdapter = UserGeneration.Adapter(
            userIdAdapter = userIdAdapter,
        ),
        userMerchAdapter = UserMerch.Adapter(
            userIdAdapter = userIdAdapter,
            updatedAtAdapter = ColumnAdapters.instantEpochSecondsAdapter,
        ),
        userSeriesAdapter = UserSeries.Adapter(
            userIdAdapter = userIdAdapter,
            updatedAtAdapter = ColumnAdapters.instantEpochSecondsAdapter,
        ),
        userSessionAdapter = UserSession.Adapter(
            userIdAdapter = userIdAdapter,
            authTokenAdapter = object : ColumnAdapter<AuthToken, String> {
                override fun decode(databaseValue: String) = AuthToken(databaseValue)
                override fun encode(value: AuthToken) = value.token
            },
            createdAtAdapter = ColumnAdapters.instantEpochSecondsAdapter,
        ),
        userStampRallyAdapter = UserStampRally.Adapter(
            userIdAdapter = userIdAdapter,
            updatedAtAdapter = ColumnAdapters.instantEpochSecondsAdapter,
        ),
    )
}
