package org.nosphere.gradle.github

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
        extensions.findByName("buildScan")?.let { buildScan ->
            applyBuildScanConfiguration(buildScan)
        }
    }
}


fun applyBuildScanConfiguration(buildScanExtension: Any) {

    val prefix = githubActions.buildScan.autoTagPrefix.getOrElse("")
    val buildScanValues = githubActions.buildScanValues(prefix)
    val runUrl = githubActions.derived.runUrl
    val jobUrl = githubActions.derived.jobUrl

    val log = logger

    val bsClass = buildScanExtension::class.java
    val bfMethod = bsClass.getMethod("buildFinished", Action::class.java)
    val tagMethod = bsClass.getMethod("tag", String::class.java)
    val valueMethod = bsClass.getMethod("value", String::class.java, String::class.java)
    val linkMethod = bsClass.getMethod("link", String::class.java, String::class.java)

    bfMethod.invoke(buildScanExtension, object : Action<Any> {
        override fun execute(bsExt: Any) {
            tagMethod.invoke(buildScanExtension, "${prefix}action")
            buildScanValues.forEach { (name, valueProvider) ->
                valueMethod.invoke(buildScanExtension, name, valueProvider.get())
            }
            linkMethod.invoke(buildScanExtension, "${prefix}run", runUrl.get())
            linkMethod.invoke(buildScanExtension, "${prefix}job", jobUrl.get())
            log.info("Build Scan tagged with Github Actions environment")
        }
    })
}

fun GithubActionsExtension.buildScanValues(prefix: String): Map<String, Provider<String>> {
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
}
