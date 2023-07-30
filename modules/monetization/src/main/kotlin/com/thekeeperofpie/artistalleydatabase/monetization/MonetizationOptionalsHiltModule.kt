package com.thekeeperofpie.artistalleydatabase.monetization

import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface MonetizationOptionalsHiltModule {

    @BindsOptionalOf
    fun bindOptionalMonetizationProvider(): MonetizationProvider

    @BindsOptionalOf
    fun bindOptionalSubscriptionProvider(): SubscriptionProvider
}
