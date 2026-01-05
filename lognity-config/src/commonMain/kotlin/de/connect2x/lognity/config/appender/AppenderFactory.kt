package de.connect2x.lognity.config.appender

import de.connect2x.lognity.api.config.ConfigBuilder
import de.connect2x.lognity.api.format.Formatter

typealias AppenderFactory<C> = ConfigBuilder.(config: C, formatter: Formatter) -> Unit