package org.nosphere.gradle.github.actions

import org.gradle.api.Action
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property
import org.gradle.util.GradleVersion
import java.io.File
import javax.inject.Inject

open class GithubActionsExtension @Inject internal constructor(
    providers: ProviderFactory,
    objects: ObjectFactory,
    layout: ProjectLayout
) {

    val running: Provider<Boolean> =
        providers.provider { environment.action.isPresent }

    val environment = GithubActionsEnvironment(providers, layout)

    fun environment(action: Action<in GithubActionsEnvironment>) =
        action.execute(environment)

    val derived = GithubActionsDerived(environment, providers)

    val buildScan = GithubActionsBuildScan(objects)

    fun buildScan(action: Action<in GithubActionsBuildScan>) =
        action.execute(buildScan)
}

class GithubActionsBuildScan internal constructor(objects: ObjectFactory) {

    val autoTag: Property<Boolean> =
        objects.property<Boolean>().convention(true)

    val autoTagPrefix: Property<String> =
        objects.property<String>().convention("github:")
}

/**
 * https://docs.github.com/en/actions/reference/environment-variables#default-environment-variables
 */
class GithubActionsEnvironment internal constructor(
    private val providers: ProviderFactory,
    private val layout: ProjectLayout
) {

    val home: Provider<Directory> = envDirectory("HOME")
    val workflow: Provider<String> = envString("GITHUB_WORKFLOW")
    val runId: Provider<String> = envString("GITHUB_RUN_ID")
    val runNumber: Provider<String> = envString("GITHUB_RUN_NUMBER")
    val jobId: Provider<String> = envString("GITHUB_JOB")
    val action: Provider<String> = envString("GITHUB_ACTION")
    val actionPath: Provider<Directory> = envDirectory("GITHUB_ACTION_PATH")
    val actor: Provider<String> = envString("GITHUB_ACTOR")
    val repository: Provider<String> = envString("GITHUB_REPOSITORY")
    val eventName: Provider<String> = envString("GITHUB_EVENT_NAME")
    val eventPath: Provider<RegularFile> = envFile("GITHUB_EVENT_PATH")
    val workspace: Provider<Directory> = envDirectory("GITHUB_WORKSPACE")
    val sha: Provider<String> = envString("GITHUB_SHA")
    val ref: Provider<String> = envString("GITHUB_REF")
    val headRef: Provider<String> = envString("GITHUB_HEAD_REF")
    val baseRef: Provider<String> = envString("GITHUB_BASE_REF")
    val serverUrl: Provider<String> = envString("GITHUB_SERVER_URL")
    val apiUrl: Provider<String> = envString("GITHUB_API_URL")
    val graphqlUrl: Provider<String> = envString("GITHUB_GRAPHQL_URL")
    val runnerOs: Provider<String> = envString("RUNNER_OS")
    val runnerTemp: Provider<Directory> = envDirectory("RUNNER_TEMP")
    val runnerToolCache: Provider<Directory> = envDirectory("RUNNER_TOOL_CACHE")

    private
    fun envString(env: String) =
        if (GradleVersion.current().baseVersion >= GradleVersion.version("6.5")) {
            providers.environmentVariable(env).forUseAtConfigurationTime()
        } else {
            providers.environmentVariable(env)
        }

    private
    fun envDirectory(env: String) =
        envString(env).flatMap { value ->
            layout.dir(providers.provider { File(value) })
        }

    private
    fun envFile(env: String) =
        envString(env).flatMap { value ->
            layout.file(providers.provider { File(value) })
        }
}

class GithubActionsDerived internal constructor(
    private val env: GithubActionsEnvironment,
    private val providers: ProviderFactory,
) {
    val runUrl: Provider<String>
        get() {
            return if (GradleVersion.current().baseVersion >= GradleVersion.version("6.6")) {
                env.serverUrl
                    .zip(env.repository) { url, repo -> "$url/$repo/actions/runs" }
                    .zip(env.runId) { url, run -> "$url/$run" }
            } else {
                providers.provider {
                    "${env.serverUrl.get()}/${env.repository.get()}/actions/runs/${env.runId.get()}"
                }
            }
        }
}
