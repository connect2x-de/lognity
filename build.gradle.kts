import de.connect2x.conventions.authenticatedPackageRegistry
import de.connect2x.conventions.defaultDependencyLocking
import de.connect2x.conventions.signPublications
import de.connect2x.conventions.withVersionSuffix

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform) apply false
    alias(sharedLibs.plugins.android.library) apply false
    alias(sharedLibs.plugins.c2xConventions)
    alias(libs.plugins.kotlin.kapt) apply false // TODO: add KAPT to shared catalog
    `maven-publish`
    signing
}

group = "de.connect2x.lognity"
version = withVersionSuffix(libs.versions.lognity)

subprojects {
    group = rootProject.group
    version = rootProject.version
    if (System.getenv("WITH_LOCK")?.toBoolean() == true) defaultDependencyLocking()

    if("example" in project.name) return@subprojects

    apply<MavenPublishPlugin>()
    apply<SigningPlugin>()

    publishing {
        repositories {
            authenticatedPackageRegistry()
        }
    }

    signing {
        signPublications()
    }
}