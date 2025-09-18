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

package net.folivo.lognity.java

import net.folivo.lognity.api.Level
import java.util.logging.Level as JavaLevel

fun Level.asJavaLevel(): JavaLevel = when (this) {
    Level.TRACE -> JavaLevel.FINEST
    Level.DEBUG -> JavaLevel.FINE
    Level.INFO -> JavaLevel.INFO
    Level.WARN -> JavaLevel.WARNING
    Level.ERROR, Level.FATAL -> JavaLevel.SEVERE
}

fun JavaLevel.asLognityLevel(): Level = when (this) {
    JavaLevel.ALL, JavaLevel.FINEST, JavaLevel.FINER -> Level.TRACE
    JavaLevel.FINE -> Level.DEBUG
    JavaLevel.WARNING -> Level.WARN
    JavaLevel.SEVERE -> Level.ERROR
    JavaLevel.OFF -> Level.FATAL
    else -> Level.INFO
}