package de.connect2x.lognity.config.appender

import de.connect2x.lognity.api.config.ConfigBuilder
import de.connect2x.lognity.api.format.Formatter

/**
 * Factory type for creating an appender from its serializable description.
 *
 * @param C the type of the serializable appender description.
 */
typealias AppenderFactory<C> = ConfigBuilder.(config: C, formatter: Formatter) -> Unit