package com.thekeeperofpie.artistalleydatabase.test_utils

import android.annotation.SuppressLint
import androidx.test.core.app.ApplicationProvider
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.dx.mockito.inline.extended.StaticMockitoSession
import dagger.hilt.android.internal.Contexts
import dagger.hilt.android.internal.testing.TestApplicationComponentManager
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.runner.Description
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

/**
 * Adapts [dagger.hilt.android.testing.HiltAndroidRule] for JUnit 5 Jupiter.
 *
 * Handles parallelized dependency injection, but doesn't work for tests which need to inject from
 * the main thread. That mostly means UI tests, which require a single threaded model. For those
 * tests, concurrency needs to be disabled.
 */
class HiltInjectExtension : Extension, TestInstancePostProcessor,
    AfterAllCallback, BeforeAllCallback {

    companion object {
        private val CANONICAL_NAME = this::class.java.canonicalName
        private val NAMESPACE = ExtensionContext.Namespace.create(CANONICAL_NAME)
        private const val KEY_MOCKITO_SESSION = "MockitoSession"
        private val checkStateIsClearedMethod = TestApplicationComponentManager::class.java
            .getDeclaredMethod("checkStateIsCleared")
            .apply { isAccessible = true }
        private val setAutoAddModuleMethod = TestApplicationComponentManager::class.java
            .getDeclaredMethod("setAutoAddModule", Boolean::class.javaPrimitiveType)
            .apply { isAccessible = true }
        private val setTestInstanceMethod = TestApplicationComponentManager::class.java
            .getDeclaredMethod("setTestInstance", Object::class.java)
            .apply { isAccessible = true }
        private val setHasHiltTestRuleMethod = TestApplicationComponentManager::class.java
            .getDeclaredMethod("setHasHiltTestRule", Description::class.java)
            .apply { isAccessible = true }
        private val verifyDelayedComponentWasMadeReadyMethod =
            TestApplicationComponentManager::class.java
                .getDeclaredMethod("verifyDelayedComponentWasMadeReady")
                .apply { isAccessible = true }
        private val clearStateMethod = TestApplicationComponentManager::class.java
            .getDeclaredMethod("clearState")
            .apply { isAccessible = true }
        private val injectMethod = TestApplicationComponentManager::class.java
            .getDeclaredMethod("inject")
            .apply { isAccessible = true }
        private val threadLocal = ThreadLocal<() -> Any>()
        private val threadMap = mutableMapOf<Long, () -> Any>()
    }

    override fun beforeAll(context: ExtensionContext) {
        val session = ExtendedMockito.mockitoSession()
            .mockStatic(Contexts::class.java)
            .strictness(Strictness.LENIENT)
            .startMocking()
        ExtendedMockito.`when`(Contexts.getApplication(any())).thenAnswer {
            val application = it.arguments[0] as CustomHiltTestApplication_Application
            val mockApplication = ExtendedMockito.spy(application)
            ExtendedMockito.doAnswer {
                threadLocal.get()?.invoke()
                    ?: threadMap.firstNotNullOfOrNull { it.value }?.invoke()
                    ?: application.generatedComponent()
            }
                .whenever(mockApplication)
                .generatedComponent()
            mockApplication
        }
        context.getStore(NAMESPACE).put(KEY_MOCKITO_SESSION, session)
    }

    @SuppressLint("CheckResult")
    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val application = Contexts.getApplication(ApplicationProvider.getApplicationContext())
        val componentManager = TestApplicationComponentManager(application)
        threadLocal.set { componentManager.generatedComponent() }
        threadMap[Thread.currentThread().id] = { componentManager.generatedComponent() }
        context.getStore(NAMESPACE).put(CANONICAL_NAME, componentManager)
        checkStateIsClearedMethod.invoke(componentManager)
        setAutoAddModuleMethod.invoke(componentManager, true)
        setTestInstanceMethod.invoke(componentManager, testInstance)
        setHasHiltTestRuleMethod.invoke(
            componentManager,
            Description.createTestDescription(
                context.requiredTestClass,
                ""
            )
        )
        injectMethod.invoke(componentManager)
    }

    override fun afterAll(context: ExtensionContext) {
        (context.getStore(NAMESPACE)
            .get(KEY_MOCKITO_SESSION) as StaticMockitoSession)
            .finishMocking()
        val componentManager = context.getStore(NAMESPACE)
            .get(CANONICAL_NAME) as TestApplicationComponentManager
        verifyDelayedComponentWasMadeReadyMethod.invoke(componentManager)
        clearStateMethod.invoke(componentManager)
    }
}
