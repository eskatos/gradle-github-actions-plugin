package org.nosphere.gradle.github

import org.nosphere.gradle.github.actions.GithubActionsExtension


val githubActions = extensions.create<GithubActionsExtension>("githubActions")


project.afterEvaluate {

    githubActions.buildScan.autoTag.finalizeValue()
    githubActions.buildScan.autoTagPrefix.finalizeValue()

    if (githubActions.running.get()) {

        val autoTag = githubActions.buildScan.autoTag.getOrElse(false)
        if (autoTag) {
            val prefix = githubActions.buildScan.autoTagPrefix.getOrElse("")
            project.plugins.withId("com.gradle.build-scan") {
                project.extensions.getByName("buildScan").withGroovyBuilder {
                    "tag"("${prefix}action")
                    githubActions.buildScanValues(prefix).forEach { (name, value) ->
                        "value"(name, value)
                    }
                }
                project.logger.info("Build Scan tagged with Github Actions environment")
            }
        }
    }
}


fun GithubActionsExtension.buildScanValues(prefix: String): Map<String, String> {
    val providersByNamed = mapOf(
        "workflow" to environment.workflow,
        "action" to environment.action,
        "actor" to environment.actor,
        "repository" to environment.repository,
        "event" to environment.eventName,
        "sha" to environment.sha,
        "ref" to environment.ref
    )
    return providersByNamed.filter { it.value.isPresent }
        .mapKeys { (name, _) -> "${prefix}$name" }
        .mapValues { (_, provider) -> provider.get() }
}
