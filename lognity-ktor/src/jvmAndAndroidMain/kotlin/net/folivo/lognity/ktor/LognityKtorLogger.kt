@file:JvmName("LognityKtorLoggerImpl")

package net.folivo.lognity.ktor

import net.folivo.lognity.slf4j.LognitySlf4jLogger

// Warning about @PublishedApi cannot be fixed here..
internal actual typealias LognityKtorLogger = LognitySlf4jLogger