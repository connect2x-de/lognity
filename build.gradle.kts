import com.vanniktech.maven.publish.MavenPublishBasePlugin
import de.connect2x.conventions.CI
import de.connect2x.conventions.apache2
import de.connect2x.conventions.c2xOrganization
import de.connect2x.conventions.defaultDependencyLocking
import de.connect2x.conventions.defaultPublishing
import de.connect2x.conventions.withVersionSuffix
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import java.time.ZonedDateTime

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform) apply false
    alias(sharedLibs.plugins.kotlin.jvm) apply false
    alias(sharedLibs.plugins.android.library) apply false
    alias(sharedLibs.plugins.kotlin.kapt) apply false
    alias(sharedLibs.plugins.mavenPublish) apply false
    alias(sharedLibs.plugins.c2xConventions)
    alias(sharedLibs.plugins.dokka) apply false
    alias(sharedLibs.plugins.kotlinx.benchmark) apply false
    `maven-publish`
    signing
}

group = "de.connect2x.lognity"
version = withVersionSuffix(libs.versions.lognity)

subprojects {
    group = rootProject.group
    version = rootProject.version
    if (System.getenv("WITH_LOCK")?.toBoolean() == true) defaultDependencyLocking()

    if ("example" in project.name || "schema" in project.name) return@subprojects

    if(CI.isCI) {
        apply<MavenPublishBasePlugin>()
        apply<SigningPlugin>()
        apply<DokkaPlugin>()

        extensions.configure<DokkaExtension> {
            moduleName = project.name
            pluginsConfiguration {
                named<DokkaHtmlPluginParameters>("html") {
                    homepageLink = "https://gitlab.com/connect2x/lognity"
                    footerMessage = "&copy; ${ZonedDateTime.now().year} connect2x GmbH"
                }
            }
        }
    }

    apply<MavenPublishPlugin>()
    defaultPublishing()

    publishing {
        publications.withType<MavenPublication> {
            pom {
                apache2()
                c2xOrganization()
            }
        }
    }
}