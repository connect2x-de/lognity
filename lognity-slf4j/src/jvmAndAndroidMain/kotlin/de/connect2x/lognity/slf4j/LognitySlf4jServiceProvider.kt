package de.connect2x.lognity.slf4j

import com.google.auto.service.AutoService
import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.helpers.BasicMDCAdapter
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

/**
 * SLF4J service provider that wires Lognity into SLF4J's ServiceLoader mechanism.
 *
 * This class is discovered via Java's ServiceLoader (auto-registered by
 * [AutoService]) and supplies the SLF4J factories and
 * MDC adapter used at runtime.
 */
@Suppress("UNUSED") // Used at runtime by ServiceLoader
@AutoService(SLF4JServiceProvider::class) // Autowire service config
class LognitySlf4jServiceProvider : SLF4JServiceProvider {
    private val mdcAdapter: BasicMDCAdapter by lazy { BasicMDCAdapter() }

    override fun getLoggerFactory(): ILoggerFactory = LognitySlf4jLoggerFactory
    override fun getMarkerFactory(): IMarkerFactory = LognitySlf4jMarkerFactory
    override fun getMDCAdapter(): MDCAdapter = mdcAdapter
    override fun getRequestedApiVersion(): String = "2.0.0"
    override fun initialize() {}
}