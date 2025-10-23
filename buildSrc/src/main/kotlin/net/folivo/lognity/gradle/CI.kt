package net.folivo.lognity.gradle

val isCI = System.getenv("CI") != null
val isRelease = System.getenv("CI_COMMIT_TAG")?.matches("^v\\d+.\\d+.\\d+.*".toRegex()) ?: false