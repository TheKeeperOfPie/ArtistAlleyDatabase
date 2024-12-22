package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.settings.SettingsStore
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

// TODO: Refactor SettingsProvider so that both Android and Desktop can inherit it
@SingletonScope
@Inject
class AndroidSettingsProvider(
    scope: ApplicationScope,
    json: Json,
    featureOverrideProvider: FeatureOverrideProvider,
    settingsStore: SettingsStore,
) : SettingsProvider(scope, json, featureOverrideProvider, settingsStore), AppSettings
