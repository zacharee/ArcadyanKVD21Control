@file:JvmName("BugsnagUtilsAndroid")

package dev.zwander.common.util

import com.bugsnag.android.BreadcrumbType
import com.bugsnag.android.Bugsnag
import dev.zwander.common.util.HttpUtils.stripSensitive

actual object BugsnagUtils {
    actual fun notify(e: Throwable) {
        Bugsnag.notify(e) { event ->
            event.errors.forEach { error ->
                error.errorMessage = error.errorMessage?.stripSensitive()
            }

            true
        }
    }

    actual fun addBreadcrumb(
        message: String,
        data: Map<String?, Any?>
    ) {
        Bugsnag.leaveBreadcrumb(
            message.stripSensitive(),
            data.mapValues { (it.value as? String)?.stripSensitive() ?: it },
            BreadcrumbType.REQUEST,
        )
    }
}
