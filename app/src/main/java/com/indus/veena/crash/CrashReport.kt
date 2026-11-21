package com.indus.veena.crash

import java.io.PrintWriter
import java.io.Writer

/** Kept independent of Android so cause chains and suppressed exceptions are testable. */
object CrashReport {
    const val MAX_REPORT_CHARS = 262_144
    fun format(error: Throwable, thread: String, environment: String, timestamp: String): String {
        val output = BoundedWriter(MAX_REPORT_CHARS)
        val writer = PrintWriter(output)
        writer.println("Veena crash report")
        writer.println("Time: $timestamp")
        writer.println(environment)
        writer.println("Thread: $thread")
        writer.println()
        writer.println("Exception and causes")
        error.printStackTrace(writer)
        writer.flush()
        return output.toString()
    }

    /** Bound allocation while writing, rather than allocating an unbounded stack trace first. */
    private class BoundedWriter(private val limit: Int) : Writer() {
        private val text = StringBuilder()
        private var truncated = false
        override fun write(buffer: CharArray, offset: Int, length: Int) {
            val count = minOf(length, limit - text.length)
            if (count > 0) text.append(buffer, offset, count)
            if (count < length) truncated = true
        }
        override fun write(value: String, offset: Int, length: Int) {
            val count = minOf(length, limit - text.length)
            if (count > 0) text.append(value, offset, offset + count)
            if (count < length) truncated = true
        }
        override fun flush() = Unit
        override fun close() = Unit
        override fun toString() = text.toString() + if (truncated) "\n[Report truncated]" else ""
    }
}
