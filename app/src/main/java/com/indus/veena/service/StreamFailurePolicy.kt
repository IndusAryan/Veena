package com.indus.veena.service

/** MediaSession can serialize a typed HTTP exception as a generic exception with its message. */
internal object StreamFailurePolicy {
    private val statusPattern = Regex("""\b(?:Response code:\s*|HTTP(?: status)?\s+)([1-5]\d{2})\b""", RegexOption.IGNORE_CASE)

    fun httpStatus(error: Throwable): Int? =
        generateSequence(error) { it.cause }.firstNotNullOfOrNull {
            statusPattern.find(it.message.orEmpty())?.groupValues?.get(1)?.toIntOrNull()
        }

    fun shouldRefresh(status: Int?): Boolean = status == 403 || status == 503
}
