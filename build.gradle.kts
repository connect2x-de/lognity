import net.folivo.lognity.gradle.GitLabCI
import net.folivo.lognity.gradle.apache2License
import net.folivo.lognity.gradle.defaultDependencyLocking
import net.folivo.lognity.gradle.isCI

plugins {
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.android.library) apply false
    `maven-publish`
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