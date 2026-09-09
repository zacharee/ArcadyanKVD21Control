@file:JvmName("BugsnagUtilsJVM")

package dev.zwander.common.util

import com.bugsnag.Bugsnag
import com.bugsnag.EndpointConfiguration
import com.bugsnag.delivery.AsyncHttpDelivery
import com.bugsnag.delivery.HttpDelivery
import com.bugsnag.delivery.SyncHttpDelivery
import com.bugsnag.serialization.Serializer
import dev.zwander.common.util.HttpUtils.stripSensitive
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.Proxy

private const val BUGSNAG_API_KEY = "e709115241c5468fd88637578daa5cfa"

// bugsnag-java has no API to rewrite an exception's message (or a cause's message) after a
// Report is built, so unlike Android/iOS we can't sanitize by mutating the report. Instead,
// wrap delivery so the fully-serialized JSON payload is scrubbed right before it's sent -
// the last point at which we can guarantee nothing unsanitized leaves the process.
private class SanitizingSerializer(private val delegate: Serializer) : Serializer {
    override fun writeToStream(stream: OutputStream, obj: Any) {
        val buffer = ByteArrayOutputStream()
        delegate.writeToStream(buffer, obj)
        stream.write(buffer.toByteArray().toString(Charsets.UTF_8).stripSensitive().toByteArray(Charsets.UTF_8))
    }
}

private class SanitizingDelivery(private val delegate: HttpDelivery) : HttpDelivery {
    override fun deliver(serializer: Serializer, obj: Any, headers: Map<String, String>) {
        delegate.deliver(SanitizingSerializer(serializer), obj, headers)
    }

    override fun setEndpoint(endpoint: String) = delegate.setEndpoint(endpoint)
    override fun setTimeout(timeout: Int) = delegate.setTimeout(timeout)
    override fun setProxy(proxy: Proxy) = delegate.setProxy(proxy)

    override fun close() = delegate.close()
}

actual object BugsnagUtils {
    val bugsnag by lazy {
        Bugsnag(BUGSNAG_API_KEY).apply {
            val notifyEndpoint = EndpointConfiguration.fromApiKey(BUGSNAG_API_KEY).notifyEndpoint

            (delivery as? AsyncHttpDelivery)?.setBaseDelivery(
                SanitizingDelivery(SyncHttpDelivery(notifyEndpoint)),
            )
        }
    }

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
