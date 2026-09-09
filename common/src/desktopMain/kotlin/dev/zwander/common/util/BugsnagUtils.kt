@file:JvmName("BugsnagUtilsJVM")

package dev.zwander.common.util

import com.bugsnag.Bugsnag
import dev.zwander.common.util.HttpUtils.stripSensitive

actual object BugsnagUtils {
    val bugsnag by lazy { Bugsnag("e709115241c5468fd88637578daa5cfa") }

    private val breadcrumbs = LinkedHashMap<Long, Pair<String, Map<String?, Any?>>>()

    @Synchronized
    actual fun notify(e: Throwable) {
        val report = bugsnag.buildReport(e)

        breadcrumbs.forEach { (time, data) ->
            report.addToTab("breadcrumbs", "$time", "${data.first.stripSensitive()}\n\n" +
                    data.second.entries.joinToString("\n") { "${it.key}=${it.value?.toString()?.stripSensitive()}" })
        }
        breadcrumbs.clear()

        bugsnag.notify(report)
    }

    actual fun addBreadcrumb(
        message: String,
        data: Map<String?, Any?>
    ) {
        breadcrumbs[System.currentTimeMillis()] = message to data
    }
}
