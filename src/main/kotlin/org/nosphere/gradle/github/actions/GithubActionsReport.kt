package org.nosphere.gradle.github.actions

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

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
                    action = ${env.action.displayValue}
                    actor = ${env.actor.displayValue}
                    repository = ${env.repository.displayValue}
                    eventName = ${env.eventName.displayValue}
                    eventPath = ${env.eventPath.displayValue}
                    workspace = ${env.workspace.displayValue}
                    sha = ${env.sha.displayValue}
                    ref = ${env.ref.displayValue}
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
        get() = map(Any::toString).getOrElse("<absent>")
}
