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

import net.folivo.lognity.gradle.GitLabCI
import net.folivo.lognity.gradle.apache2License
import net.folivo.lognity.gradle.defaultDependencyLocking
import net.folivo.lognity.gradle.isCI

plugins {
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.android.library) apply false
    signing
    `maven-publish`
    alias(libs.plugins.gradleNexus)
}

group = "net.folivo.lognity"
version = GitLabCI.getDefaultVersion(libs.versions.lognity)

subprojects {
    apply<MavenPublishPlugin>()
    apply<SigningPlugin>()

    group = rootProject.group
    version = rootProject.version
    if (isCI) defaultDependencyLocking()

    publishing {
        apache2License()
    }
}