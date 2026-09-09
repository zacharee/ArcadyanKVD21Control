package dev.zwander.common.util

import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.util.toMap

object HttpUtils {
    // Keys whose values should never end up in error reports.
    private val sensitiveKeys = listOf(
        "Authorization",
        "Set-Cookie",
        "Cookie",
        "passwordNew",
        "password",
        "pswd",
        "wpaKey",
        "token",
    )

    // Matches `key=value` (manual map style) and `key: value`/`"key": "value"` (JSON style)
    // pairs for any of the sensitive keys above, case-insensitively, with or without quotes
    // around the key and/or value.
    // Match examples (case-insensitive):
    // * Authorization: value
    // * Authorization: "value"
    // * "Authorization": value
    // * "Authorization": "value"
    // * Authorization=value
    private val sensitiveKeyValueRegex by lazy {
        Regex(
            """(?i)("?(?:${sensitiveKeys.joinToString("|")})"?)(\s*[:=]\s*)("(?:[^"\\]|\\.)*"|[^,\]}&;]*)"""
        )
    }

    suspend fun HttpResponse.formatForReport(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        map["status"] = this.status.value.toString().stripSensitive()
        map["headers"] = this.headers.toMap().toString().stripSensitive()
        map["requestHeaders"] = this.request.headers.toString().stripSensitive()
        map["requestBody"] = this.request.content.toByteArray().decodeToString().stripSensitive()
        map["requestUrl"] = this.request.url.toString().stripSensitive()
        map["requestMethod"] = this.request.method.value

        return map
    }

    // Don't send secrets (tokens, cookies, passwords, Wi-Fi keys, etc.) in error reports.
    fun String.stripSensitive(): String {
        return sensitiveKeyValueRegex.replace(this) { match ->
            val key = match.groupValues[1]
            val separator = match.groupValues[2]
            val value = match.groupValues[3]

            // Only match on at least `""` to avoid adding an extra end quote to just a single " char.
            val redactedValue = if (value.length >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                "\"***\""
            } else {
                "***"
            }

            "$key$separator$redactedValue"
        }
    }
}
