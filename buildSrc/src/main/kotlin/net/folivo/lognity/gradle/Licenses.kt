package net.folivo.lognity.gradle

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType

fun PublishingExtension.apache2License() {
    publications.withType<MavenPublication>().configureEach {
        pom {
            licenses {
                license {
                    name.set("Apache License 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
        }
    }
}