package org.nosphere.gradle.github.actions

import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property
import java.io.File


open class GithubActionsExtension(
    providers: ProviderFactory,
    objects: ObjectFactory,
    layout: ProjectLayout
) {

    val running: Provider<Boolean> =
        providers.provider { environment.action.orNull != null }

    val environment = GithubActionsEnvironment(providers, objects, layout)

    fun environment(action: Action<in GithubActionsEnvironment>) =
        action.execute(environment)

    val buildScan = GithubActionsBuildScan(objects)

    fun buildScan(action: Action<in GithubActionsBuildScan>) =
        action.execute(buildScan)
}


class GithubActionsBuildScan(objects: ObjectFactory) {

    val autoTag: Property<Boolean> =
        objects.property<Boolean>().convention(true)

    val autoTagPrefix: Property<String> =
        objects.property<String>().convention("github:")
}


/**
 * https://help.github.com/en/articles/virtual-environments-for-github-actions#default-environment-variables
 */
class GithubActionsEnvironment(
    private val providers: ProviderFactory,
    private val objects: ObjectFactory,
    private val layout: ProjectLayout
) {

    val home = envDirectory("HOME")
    val workflow = envString("GITHUB_WORKFLOW")
    val action = envString("GITHUB_ACTION")
    val actor = envString("GITHUB_ACTOR")
    val repository = envString("GITHUB_REPOSITORY")
    val eventName = envString("GITHUB_EVENT_NAME")
    val eventPath = envFile("GITHUB_EVENT_PATH")
    val workspace = envDirectory("GITHUB_WORKSPACE")
    val sha = envString("GITHUB_SHA")
    val ref = envString("GITHUB_REF")

    private
    fun envString(env: String) =
        providers.provider { System.getenv(env) }

    private
    fun envDirectory(env: String) =
        providers.provider {
            System.getenv(env)?.let { path ->
                // TODO how to create an absolute Directory without creating a property?
                objects.directoryProperty().apply { set(File(path)) }.get()
            }
        }

    private
    fun envFile(env: String) =
        layout.file(providers.provider {
            System.getenv(env)?.let(::File)
        })
}
