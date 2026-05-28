package com.splunk.android.common.logger

data class Log internal constructor(
    val level: Level,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {

    enum class Level {
        ERROR, WARN, INFO, DEBUG, VERBOSE;

        override fun toString(): String {
            return when (this) {
                ERROR -> "E"
                WARN -> "W"
                INFO -> "I"
                DEBUG -> "D"
                VERBOSE -> "V"
            }
        }
    }

    val time = System.currentTimeMillis()

    override fun equals(other: Any?): Boolean =
        this === other || other is Log && time == other.time && level == other.level && tag == other.tag && message == other.message && throwable == other.throwable

    override fun hashCode(): Int {
        var result = level.hashCode()
        result = 31 * result + tag.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + (throwable?.hashCode() ?: 0)
        result = 31 * result + time.hashCode()
        return result
    }
}
