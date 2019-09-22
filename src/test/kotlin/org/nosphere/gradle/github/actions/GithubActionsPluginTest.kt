package org.nosphere.gradle.github.actions

import org.gradle.internal.os.OperatingSystem
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertThat
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.nosphere.gradle.github.AbstractPluginTest


// Note that tests that deal with build scans don't assert the tags and custom values content
// but publishes a scan so it can be checked by hand
// TODO find out how to assert build scan tagging
@RunWith(Parameterized::class)
class GithubActionsPluginTest(gradleVersion: String) : AbstractPluginTest(gradleVersion) {

    @Test
    fun `says *not* running a github action when appropriate`() {

        withBuildScript("""
            plugins {
                id("org.nosphere.gradle.github.actions")
            }
            assert githubActions.running.get() == false
        """)

        build(emptyEnvironment, "help")
    }

    @Test
    fun `says running a github action when appropriate`() {

        withBuildScript("""
            plugins {
                id("org.nosphere.gradle.github.actions")
            }
            assert githubActions.running.get() == true
        """)

        build(githubActionEnvironment, "help")
    }

    @Test
    fun `collects github actions environment variables`() {

        withBuildScript("""
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
        """)

        build(githubActionEnvironment, "help") {
            assertThat(output, containsString("home: $homeTestValue"))
            assertThat(output, containsString("workflow: workflow"))
            assertThat(output, containsString("action: some/action"))
            assertThat(output, containsString("actor: octocat"))
            assertThat(output, containsString("repository: octocat/hello-world"))
            assertThat(output, containsString("eventName: webhook"))
            assertThat(output, containsString("eventPath: $eventPathTestValue"))
            assertThat(output, containsString("workspace: $workspaceTestValue"))
            assertThat(output, containsString("sha: ffac537e6cbbf934b08745a378932722df287a53"))
            assertThat(output, containsString("ref: refs/heads/feature-branch-1."))
        }
    }

    @Test
    fun `environment providers absent when *not* running a github action`() {

        withBuildScript("""
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
        """)

        build(emptyEnvironment, "help")
    }

    @Test
    fun `publishes tagged build scan by default`() {

        // TODO unignore build-scans tests on windows
        assumeFalse(isWindows)

        withBuildScript("""
            plugins {
                id("org.nosphere.gradle.github.actions")
                id("com.gradle.build-scan") version "2.1"
            }
            
            buildScan {
                termsOfServiceUrl = "https://gradle.com/terms-of-service"
                termsOfServiceAgree = "yes"
            }
        """)

        build(githubActionEnvironment, "help", "--scan", "-i") {
            println(output)
            assertThat(output, containsString("Build Scan tagged with Github Actions environment"))
        }
    }

    @Test
    fun `publishes untagged build scan if instructed`() {

        // TODO unignore build-scans tests on windows
        assumeFalse(isWindows)

        withBuildScript("""
            plugins {
                id("org.nosphere.gradle.github.actions")
                id("com.gradle.build-scan") version "2.1"
            }
            
            buildScan {
                termsOfServiceUrl = "https://gradle.com/terms-of-service"
                termsOfServiceAgree = "yes"
            }
            
            githubActions.buildScan.autoTag.set(false)
        """)

        build(githubActionEnvironment, "help", "--scan", "-i") {
            println(output)
            assertThat(output, not(containsString("Build Scan tagged with Github Actions environment")))
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
}
