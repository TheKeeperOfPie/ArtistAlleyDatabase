package com.thekeeperofpie.artistalleydatabase.test_utils

import android.annotation.SuppressLint
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

@SuppressLint("VisibleForTests")
class TestInitializerExtension : BeforeAllCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext?) {
        CustomDispatchers.enable()
        TestNetworkController.initialize()
    }

    override fun afterAll(context: ExtensionContext?) {
        TestNetworkController.destroy()
    }
}
