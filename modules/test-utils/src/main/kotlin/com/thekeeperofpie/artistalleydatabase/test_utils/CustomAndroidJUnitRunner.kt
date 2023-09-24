package com.thekeeperofpie.artistalleydatabase.test_utils

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import dagger.hilt.android.testing.CustomTestApplication
import kotlinx.coroutines.MainScope

class CustomAndroidJUnitRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ) = super.newApplication(cl, CustomHiltTestApplication_Application::class.java.name, context)!!
}

@CustomTestApplication(TestApplication::class)
interface CustomHiltTestApplication

@Suppress("LeakingThis")
open class TestApplication : Application(), ScopedApplication {
    override val scope = MainScope()
    override val app = this
}
