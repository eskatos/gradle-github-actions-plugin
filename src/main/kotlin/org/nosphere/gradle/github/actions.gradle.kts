package org.nosphere.gradle.github

import org.nosphere.gradle.github.actions.GithubActionsExtension


val githubActions = extensions.create<GithubActionsExtension>("githubActions")


afterEvaluate {

    githubActions.buildScan.autoTag.finalizeValue()
    githubActions.buildScan.autoTagPrefix.finalizeValue()

    if (
        githubActions.running.get() &&
        githubActions.buildScan.autoTag.getOrElse(false)
    ) {

        plugins.withId("com.gradle.build-scan") {
            val prefix = githubActions.buildScan.autoTagPrefix.getOrElse("")
            extensions.getByName("buildScan").withGroovyBuilder {
                "tag"("${prefix}action")
                githubActions.buildScanValues(prefix).forEach { (name, value) ->
                    "value"(name, value)
                }
            }
            logger.info("Build Scan tagged with Github Actions environment")
        }
    }
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
