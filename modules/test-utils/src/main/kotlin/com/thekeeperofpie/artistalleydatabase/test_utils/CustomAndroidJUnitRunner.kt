package com.thekeeperofpie.artistalleydatabase.test_utils

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.CustomTestApplication

class CustomAndroidJUnitRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ) = super.newApplication(cl, CustomHiltTestApplication_Application::class.java.name, context)!!
}

@CustomTestApplication(TestApplication::class)
interface CustomHiltTestApplication

open class TestApplication : Application()
