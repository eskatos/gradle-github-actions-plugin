package org.nosphere.gradle.github.actions

import org.gradle.internal.os.OperatingSystem
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.nosphere.gradle.github.AbstractPluginTest

// Note that tests that deal with build scans don't assert the tags and custom values content
// but publishes a scan so it can be checked by hand
// TODO find out how to assert build scan tagging
// TODO unignore build-scans tests on windows
@RunWith(Parameterized::class)
class GithubActionsPluginTest(testMatrix: TestMatrix) : AbstractPluginTest(testMatrix) {

    @Test
    fun `says _not_ running a github action when appropriate`() {

        withBuildScript(
            """
            plugins {
                id("org.nosphere.gradle.github.actions")
            }
            assert githubActions.running.get() == false
            """
        )

        build(emptyEnvironment, "githubActions")
    }

    @Test
    fun `says running a github action when appropriate`() {

        withBuildScript(
            """
            plugins {
                id("org.nosphere.gradle.github.actions")
            }
            assert githubActions.running.get() == true
        """
        )

        build(githubActionEnvironment, "githubActions")
    }

    @Test
    fun `can use environment at configuration time`() {

        withBuildScript(
            """
            plugins {
                id("org.nosphere.gradle.github.actions")
            }

            githubActions.environment {
                println("home: ${'$'}{home.get().asFile}")
                println("workflow: ${'$'}{workflow.get()}")
                println("action: ${'$'}{action.get()}")
                println("actor: ${'$'}{actor.get()}")
                println("repository: ${'$'}{repository.get()}")
                println("eventName: ${'$'}{eventName.get()}")
                println("eventPath: ${'$'}{eventPath.get().asFile}")
                println("workspace: ${'$'}{workspace.get().asFile}")
                println("sha: ${'$'}{sha.get()}")
                println("ref: ${'$'}{ref.get()}")
            }
        """
        )

        build(githubActionEnvironment, "help") {
            outputContains("home: $homeTestValue")
            outputContains("workflow: workflow")
            outputContains("action: some/action")
            outputContains("actor: octocat")
            outputContains("repository: octocat/hello-world")
            outputContains("eventName: webhook")
            outputContains("eventPath: $eventPathTestValue")
            outputContains("workspace: $workspaceTestValue")
            outputContains("sha: ffac537e6cbbf934b08745a378932722df287a53")
            outputContains("ref: refs/heads/feature-branch-1.")
        }


        if (testMatrix.configurationCache) {
            build(githubActionEnvironment, "help") {
                outputContains("Reusing configuration cache")
            }
            build(githubActionEnvironment + ("GITHUB_ACTOR" to "tacotco"), "help") {
                outputContains(
                    "Calculating task graph as configuration cache cannot be reused " +
                        "because environment variable 'GITHUB_ACTOR' has changed."
                )
            }
        }
    }

    @Test
    fun `report tasks displays environment and build scan configuration`() {

        withBuildScript(
            """
            plugins {
                id("org.nosphere.gradle.github.actions")
            }
        """
        )

        build(githubActionEnvironment, "githubActions") {
            outputContains("home = $homeTestValue")
            outputContains("workflow = workflow")
            outputContains("action = some/action")
            outputContains("actor = octocat")
            outputContains("repository = octocat/hello-world")
            outputContains("eventName = webhook")
            outputContains("eventPath = $eventPathTestValue")
            outputContains("workspace = $workspaceTestValue")
            outputContains("sha = ffac537e6cbbf934b08745a378932722df287a53")
            outputContains("ref = refs/heads/feature-branch-1.")
            outputContains("autoTag = true")
            outputContains("autoTagPrefix = github:")
        }

        if (testMatrix.configurationCache) {
            build(githubActionEnvironment, "githubActions") {
                outputContains("Reusing configuration cache")
            }
            build(githubActionEnvironment + ("GITHUB_ACTOR" to "tacotco"), "githubActions") {
                outputContains("Reusing configuration cache")
            }
        }
    }

    @Test
    fun `environment providers absent when _not_ running a github action`() {

        withBuildScript(
            """
            plugins {
                id("org.nosphere.gradle.github.actions")
            }

            githubActions.environment {
                assert !home.present
                assert !workflow.present
                assert !action.present
                assert !actor.present
                assert !repository.present
                assert !eventName.present
                assert !eventPath.present
                assert !workspace.present
                assert !sha.present
                assert !ref.present
            }
        """
        )

        build(emptyEnvironment, "githubActions")
    }

    @Test
    fun `publishes tagged build scan by default`() {

        assumeFalse(isWindows)

        withSettingsForBuildScans()

        withBuildScript(
            """
            plugins {
                id("org.nosphere.gradle.github.actions")
            }

            buildScan {
                termsOfServiceUrl = "https://gradle.com/terms-of-service"
                termsOfServiceAgree = "yes"
            }
        """
        )

        build(githubActionEnvironment, "githubActions", "--scan", "-i") {
            println(output)
            outputContains("Build Scan tagged with Github Actions environment")
        }
    }

    @Test
    fun `publishes untagged build scan if instructed`() {

        assumeFalse(isWindows)

        withSettingsForBuildScans()

        withBuildScript(
            """
            plugins {
                id("org.nosphere.gradle.github.actions")
            }

            buildScan {
                termsOfServiceUrl = "https://gradle.com/terms-of-service"
                termsOfServiceAgree = "yes"
            }

            githubActions.buildScan.autoTag.set(false)
        """
        )

        build(githubActionEnvironment, "githubActions", "--scan", "-i") {
            println(output)
            outputDoesNotContain("Build Scan tagged with Github Actions environment")
        }
    }

    private
    val emptyEnvironment = emptyMap<String, String>()

    private
    val homeTestValue =
        if (isWindows) "C:\\Users\\github"
        else "/home/github"

    private
    val eventPathTestValue =
        if (isWindows) "C:\\github\\workflow\\event.json"
        else "/github/workflow/event.json"

    private
    val workspaceTestValue =
        if (isWindows) "C:\\Users\\runner\\work\\octocat\\hello-world"
        else "/home/runner/work/octocat/hello-world"

    private
    val githubActionEnvironment = mapOf(
        "HOME" to homeTestValue,
        "GITHUB_WORKFLOW" to "workflow",
        "GITHUB_ACTION" to "some/action",
        "GITHUB_ACTOR" to "octocat",
        "GITHUB_REPOSITORY" to "octocat/hello-world",
        "GITHUB_EVENT_NAME" to "webhook",
        "GITHUB_EVENT_PATH" to eventPathTestValue,
        "GITHUB_WORKSPACE" to workspaceTestValue,
        "GITHUB_SHA" to "ffac537e6cbbf934b08745a378932722df287a53",
        "GITHUB_REF" to "refs/heads/feature-branch-1."
    )

    private
    val isWindows
        get() = OperatingSystem.current().isWindows

    private
    fun withSettingsForBuildScans() {
        withSettingsScript(
            """
                plugins {
                    id("com.gradle.enterprise") version "3.6.4"
                }
            """
        )
    }
}
