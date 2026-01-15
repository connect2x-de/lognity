import de.connect2x.conventions.apache2
import de.connect2x.conventions.authenticatedPackageRegistry
import de.connect2x.conventions.authenticatedSonatype
import de.connect2x.conventions.c2xOrganization
import de.connect2x.conventions.defaultDependencyLocking
import de.connect2x.conventions.signPublications
import de.connect2x.conventions.withVersionSuffix
import org.jetbrains.dokka.gradle.DokkaPlugin
import java.time.Duration
import java.time.ZonedDateTime

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform) apply false
    alias(sharedLibs.plugins.android.library) apply false
    alias(sharedLibs.plugins.c2xConventions)
    alias(sharedLibs.plugins.kotlin.kapt) apply false
    alias(sharedLibs.plugins.dokka)
    alias(sharedLibs.plugins.gradleNexus)
    `maven-publish`
    signing
}

group = "de.connect2x.lognity"
version = withVersionSuffix(libs.versions.lognity)

subprojects {
    group = rootProject.group
    version = rootProject.version
    if (System.getenv("WITH_LOCK")?.toBoolean() == true) defaultDependencyLocking()

    if ("example" in project.name) return@subprojects

    apply<MavenPublishPlugin>()
    apply<SigningPlugin>()
    apply<DokkaPlugin>()

    signing {
        signPublications()
    }

    dokka {
        moduleName = project.name
        pluginsConfiguration {
            html {
                homepageLink = "https://gitlab.com/connect2x/lognity"
                footerMessage = "&copy; ${ZonedDateTime.now().year} connect2x GmbH"
            }
        }
    }

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier = "javadoc"
        dependsOn(tasks.dokkaGeneratePublicationHtml)
        from(tasks.dokkaGeneratePublicationHtml)
    }

    publishing {
        repositories {
            authenticatedPackageRegistry()
        }
        publications.withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                apache2()
                c2xOrganization()
            }
        }
    }
}

nexusPublishing {
    authenticatedSonatype()
    connectTimeout = Duration.ofSeconds(30)
    clientTimeout = Duration.ofMinutes(45)
}