/*
 * Copyright 2025 Trixnity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.folivo.lognity.slf4j

import com.google.auto.service.AutoService
import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.helpers.BasicMDCAdapter
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

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