package de.connect2x.lognity.config.condition

/**
 * All condition types that might be used by a [String] condition.
 */
enum class StringConditionType { // @formatter:off
    EQUALS,
    NOT_EQUALS,
    CONTAINS,
    NOT_CONTAINS,
    STARTS_WITH,
    NOT_STARTS_WITH,
    ENDS_WITH,
    NOT_ENDS_WITH;

    operator fun invoke(s: String, value: String): Boolean = when (this) {
        EQUALS -> s == value
        NOT_EQUALS -> s != value
        CONTAINS -> value in s
        NOT_CONTAINS -> value !in s
        STARTS_WITH -> s.startsWith(value)
        NOT_STARTS_WITH -> !s.startsWith(value)
        ENDS_WITH -> s.endsWith(value)
        NOT_ENDS_WITH -> !s.endsWith(value)
    }
} // @formatter:on