package com.thekeeperofpie.artistalleydatabase.test_utils

import de.mannodermaus.junit5.AndroidJUnit5Builder
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.RunnerBuilder

@Suppress("unused")
class AndroidJUnitBuilder : RunnerBuilder() {
    private val builder = AndroidJUnit5Builder()

    override fun runnerForClass(testClass: Class<*>): Runner? {
        val runner = builder.runnerForClass(testClass) ?: return null
        return object : Runner() {
            override fun getDescription() = runner.description
            override fun run(notifier: RunNotifier) = runner.run(ParallelRunNotifier(notifier))
        }
    }
}

/**
 * Reports parallel test method runs serially so that instrumentation run results report
 * success/fail correctly.
 */
class ParallelRunNotifier(private val notifier: RunNotifier) : RunNotifier() {

    class Failures(var assumptionFailure: Failure?, var testFailure: Failure?)

    private val events = mutableMapOf<Description, Failures>()
    private val lock = Any()

    override fun fireTestStarted(description: Description) {
        events[description] = Failures(null, null)
    }

    override fun fireTestFailure(failure: Failure) {
        events[failure.description]!!.testFailure = failure
    }

    override fun fireTestAssumptionFailed(failure: Failure) {
        events[failure.description]!!.assumptionFailure = failure
    }

    override fun fireTestFinished(description: Description) = synchronized(lock) {
        val event = events[description]!!
        notifier.fireTestStarted(description)
        if (event.assumptionFailure != null) {
            notifier.fireTestAssumptionFailed(event.assumptionFailure)
        } else if (event.testFailure != null) {
            notifier.fireTestFailure(event.testFailure)
        }
        notifier.fireTestFinished(description)
    }
}