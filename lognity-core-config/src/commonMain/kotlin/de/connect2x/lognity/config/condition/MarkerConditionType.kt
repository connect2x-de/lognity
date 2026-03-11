package de.connect2x.lognity.config.condition

/**
 * All condition types that might be used by a [de.connect2x.lognity.api.marker.Marker] condition.
 */
enum class MarkerConditionType { // @formatter:off
    KEY_EQUALS,
    KEY_NOT_EQUALS,
    KEY_CONTAINS,
    KEY_NOT_CONTAINS,
    KEY_STARTS_WITH,
    KEY_NOT_STARTS_WITH,
    KEY_ENDS_WITH,
    KEY_NOT_ENDS_WITH,
    NAME_EQUALS,
    NAME_NOT_EQUALS,
    NAME_CONTAINS,
    NAME_NOT_CONTAINS,
    NAME_STARTS_WITH,
    NAME_NOT_STARTS_WITH,
    NAME_ENDS_WITH,
    NAME_NOT_ENDS_WITH;

    operator fun invoke(key: String?, name: String?, value: String): Boolean = when (this) {
        KEY_EQUALS -> key == value
        KEY_NOT_EQUALS -> key != value
        KEY_CONTAINS -> key?.let { key -> value in key } == true
        KEY_NOT_CONTAINS -> key?.let { key -> value !in key } == true
        KEY_STARTS_WITH -> key?.startsWith(value) == true
        KEY_NOT_STARTS_WITH -> key?.startsWith(value) != true
        KEY_ENDS_WITH -> key?.endsWith(value) == true
        KEY_NOT_ENDS_WITH -> key?.endsWith(value) != true
        NAME_EQUALS -> name == value
        NAME_NOT_EQUALS -> name != value
        NAME_CONTAINS -> name?.let { name -> value in name } == true
        NAME_NOT_CONTAINS -> name?.let { name -> value !in name } == true
        NAME_STARTS_WITH -> name?.startsWith(value) == true
        NAME_NOT_STARTS_WITH -> name?.startsWith(value) != true
        NAME_ENDS_WITH -> name?.endsWith(value) == true
        NAME_NOT_ENDS_WITH -> name?.endsWith(value) != true
    }
} // @formatter:on