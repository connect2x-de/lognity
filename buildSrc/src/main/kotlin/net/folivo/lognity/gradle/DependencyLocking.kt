package net.folivo.lognity.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.gradle.kotlin.dsl.register

fun Project.defaultDependencyLocking() {
    dependencyLocking {
        lockAllConfigurations()
    }
    tasks.register<DependencyReportTask>("dependenciesForAll")
}