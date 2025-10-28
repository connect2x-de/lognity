package net.folivo.lognity.gradle

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.withType
import java.net.URI

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

fun RepositoryHandler.authenticatedPackageRegistry() {
    System.getenv("CI_API_V4_URL")?.let { apiUrl ->
        maven {
            url = URI.create("$apiUrl/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
            name = "GitLab"
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
    }
}