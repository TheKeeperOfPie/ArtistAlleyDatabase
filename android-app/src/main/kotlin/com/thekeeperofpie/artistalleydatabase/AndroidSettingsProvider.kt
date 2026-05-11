package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.settings.SettingsStore
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json

// TODO: Refactor SettingsProvider so that both Android and Desktop can inherit it
@SingleIn(AppScope::class)
@Inject
class AndroidSettingsProvider(
    scope: ApplicationScope,
    json: Json,
    featureOverrideProvider: FeatureOverrideProvider,
    settingsStore: SettingsStore,
) : SettingsProvider(scope, json, featureOverrideProvider, settingsStore), AppSettings
