package net.folivo.lognity.gradle

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType

fun PublishingExtension.setProjectInfo( // @formatter:off
    name: String,
    description: String,
    url: String = "https://gitlab.com/trixnity/$name"
) { // @formatter:on
    publications.withType<MavenPublication>().configureEach {
        pom {
            this.name.set(name)
            this.description.set(description)
            this.url.set(url)
        }
    }
}