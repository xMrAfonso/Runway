package me.mrafonso.runway.listener.console

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy
import org.apache.logging.log4j.core.config.AppenderRef
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.message.SimpleMessage

/**
 * Routes the server's root log appenders through a log4j [RewriteAppender] that applies [ConsoleFormatter].
 *
 * Log4j filters can only drop events, so rewriting means sitting in front of the real appenders
 * (console, latest.log...). [uninstall] puts the original appenders back exactly as they were.
 */
class ConsoleRewriter(private val formatter: ConsoleFormatter) {
    private var uninstall: (() -> Unit)? = null

    /** Guards against re-entry, e.g. if parsing a line itself logs something. */
    private val rewriting = ThreadLocal.withInitial { false }

    fun install() {
        if (uninstall != null) return

        val context = (LogManager.getRootLogger() as Logger).context
        val config = context.configuration
        val root = config.rootLogger
        val refs: Array<AppenderRef> = root.appenderRefs.toTypedArray()

        val appender = RewriteAppender.createAppender(APPENDER_NAME, "true", refs, config, Policy(), null)
        appender.start()
        config.addAppender(appender)
        refs.forEach { root.removeAppender(it.ref) }
        root.addAppender(appender, null, null)
        context.updateLoggers()

        uninstall = {
            root.removeAppender(APPENDER_NAME)
            refs.forEach { ref ->
                config.getAppender<Appender>(ref.ref)?.let { root.addAppender(it, ref.level, ref.filter) }
            }
            appender.stop()
            context.updateLoggers()
        }
    }

    fun uninstall() {
        uninstall?.invoke()
        uninstall = null
    }

    private inner class Policy : RewritePolicy {
        override fun rewrite(source: LogEvent): LogEvent {
            if (rewriting.get()) return source
            val message = source.message?.formattedMessage ?: return source

            rewriting.set(true)
            val formatted = try {
                formatter.format(message)
            } catch (_: Exception) {
                // A broken placeholder must never swallow a log line.
                null
            } finally {
                rewriting.set(false)
            }

            return formatted?.let { Log4jLogEvent.Builder(source).setMessage(SimpleMessage(it)).build() } ?: source
        }
    }

    private companion object {
        const val APPENDER_NAME = "RunwayConsoleRewrite"
    }
}
