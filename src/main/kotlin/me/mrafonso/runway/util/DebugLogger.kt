package me.mrafonso.runway.util

import java.util.logging.Logger

/**
 * Logs diagnostic messages only while debug mode is enabled.
 *
 * Messages are built lazily, so disabled debug logging costs nothing on hot paths
 * such as tag resolution.
 *
 * @property enabled Checked on every call, so toggling debug mode takes effect immediately.
 */
class DebugLogger(
    private val logger: Logger?,
    private val enabled: () -> Boolean
) {
    fun log(message: () -> String) {
        if (logger != null && enabled()) logger.info("[Debug] ${message()}")
    }

    companion object {
        val NONE = DebugLogger(null) { false }
    }
}
