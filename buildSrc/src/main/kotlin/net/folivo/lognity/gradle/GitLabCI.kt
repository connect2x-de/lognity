package net.folivo.lognity.gradle

import org.gradle.api.provider.Provider

object GitLabCI {
    fun getDefaultVersion(baseVersion: Provider<String>): String {
        return System.getenv("CI_COMMIT_TAG")?.let { baseVersion.get() }
            ?: "${baseVersion.get()}.${System.getenv("CI_PIPELINE_IID") ?: 0}-SNAPSHOT"
    }
}