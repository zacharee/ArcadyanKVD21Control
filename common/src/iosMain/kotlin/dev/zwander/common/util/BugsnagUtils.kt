@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")
package dev.zwander.common.util

import com.rickclephas.kmp.nsexceptionkt.core.asNSException
import dev.zwander.bugsnag.cinterop.BSGBreadcrumbType
import dev.zwander.bugsnag.cinterop.Bugsnag
import dev.zwander.bugsnag.cinterop.BugsnagError
import dev.zwander.common.util.HttpUtils.stripSensitive
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual object BugsnagUtils {
    actual fun notify(e: Throwable) {
        Bugsnag.notify(e.asNSException(true)) { event ->
            event?.let { event ->
                event.setErrors(event.errors.map { error ->
                    (error as BugsnagError).setErrorMessage(
                        error.errorMessage?.stripSensitive(),
                    )
                    error
                })
            }
            true
        }
    }

    actual fun addBreadcrumb(
        message: String,
        data: Map<String?, Any?>,
    ) {
        Bugsnag.leaveBreadcrumbWithMessage(
            message.stripSensitive(),
            data.mapKeys { it.key }.mapValues { (it.value as? String)?.stripSensitive() ?: it },
            BSGBreadcrumbType.BSGBreadcrumbTypeRequest,
        )
    }
}
