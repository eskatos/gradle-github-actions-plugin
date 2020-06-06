package org.nosphere.gradle.github

import org.gradle.util.GradleVersion
import org.nosphere.gradle.github.actions.GithubActionsExtension
import org.nosphere.gradle.github.actions.GithubActionsReport

val githubActions = extensions.create<GithubActionsExtension>("githubActions")

tasks.register<GithubActionsReport>("githubActions", githubActions).configure {
    group = HelpTasksPlugin.HELP_GROUP
    description = "Displays the GitHub Actions configuration and environment"
}

afterEvaluate {

    githubActions.buildScan.autoTag.finalizeValue()
    githubActions.buildScan.autoTagPrefix.finalizeValue()

    if (
        githubActions.running.get() &&
        githubActions.buildScan.autoTag.getOrElse(false)
    ) {
        if (GradleVersion.current().baseVersion >= GradleVersion.version("6.0")) {
            extensions.findByName("buildScan")?.let { buildScan ->
                applyBuildScanConfiguration(buildScan)
            }
        } else {
            plugins.withId("com.gradle.build-scan") {
                applyBuildScanConfiguration(extensions.getByName("buildScan"))
            }
        }
    }
}


fun applyBuildScanConfiguration(buildScanExtension: Any) {
    val prefix = githubActions.buildScan.autoTagPrefix.getOrElse("")
    buildScanExtension.withGroovyBuilder {
        "tag"("${prefix}action")
        githubActions.buildScanValues(prefix).forEach { (name, value) ->
            "value"(name, value)
        }
    }
    logger.info("Build Scan tagged with Github Actions environment")
}

fun GithubActionsExtension.buildScanValues(prefix: String): Map<String, String> {
    val providerByName = mapOf(
        "workflow" to environment.workflow,
        "action" to environment.action,
        "actor" to environment.actor,
        "repository" to environment.repository,
        "event" to environment.eventName,
        "sha" to environment.sha,
        "ref" to environment.ref
    )
    return providerByName.filter { it.value.isPresent }
        .mapKeys { (name, _) -> "${prefix}$name" }
        .mapValues { (_, provider) -> provider.get() }
}
