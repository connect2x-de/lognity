package de.connect2x.lognity.config.extension

import de.connect2x.lognity.config.SerializableConfig

/**
 * A provider that supplies a value.
 */
typealias Provider<T> = () -> T

/**
 * A provider that supplies a value based on a template string and the current configuration.
 */
typealias TemplateProvider<T> = SerializableConfig.(String) -> T

/**
 * A provider for built-in values identified by a string.
 */
typealias BuiltinProvider<T> = (String) -> T