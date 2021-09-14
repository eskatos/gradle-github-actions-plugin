package org.nosphere.gradle.github.actions

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault
abstract class GithubActionsReport @Inject internal constructor(

    private
    var githubActions: GithubActionsExtension

) : DefaultTask() {

    @TaskAction
    fun action() {

        println()
        val env = githubActions.environment
        println(
            """
            githubActions {
                environment {
                    home = ${env.home.displayValue}
                    workflow = ${env.workflow.displayValue}
                    runId = ${env.runId.displayValue}
                    runNumber = ${env.runNumber.displayValue}
                    jobId = ${env.jobId.displayValue}
                    action = ${env.action.displayValue}
                    actionPath = ${env.actionPath.displayValue}
                    actor = ${env.actor.displayValue}
                    repository = ${env.repository.displayValue}
                    eventName = ${env.eventName.displayValue}
                    eventPath = ${env.eventPath.displayValue}
                    workspace = ${env.workspace.displayValue}
                    sha = ${env.sha.displayValue}
                    ref = ${env.ref.displayValue}
                    headRef = ${env.headRef.displayValue}
                    baseRef = ${env.baseRef.displayValue}
                    serverUrl = ${env.serverUrl.displayValue}
                    apiUrl = ${env.apiUrl.displayValue}
                    graphqlUrl = ${env.graphqlUrl.displayValue}
                    runnerOs = ${env.runnerOs.displayValue}
                    runnerTemp = ${env.runnerTemp.displayValue}
                    runnerToolCache = ${env.runnerToolCache.displayValue}
                }
                buildScan {
                    autoTag = ${githubActions.buildScan.autoTag.displayValue}
                    autoTagPrefix = ${githubActions.buildScan.autoTagPrefix.displayValue}
                }
            }
            """.trimIndent()
        )
        println()
    }

    private
    val Provider<*>.displayValue: String
        get() = map { it.toString() }.getOrElse("<absent>")
}
