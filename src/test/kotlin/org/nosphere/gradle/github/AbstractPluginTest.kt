/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nosphere.gradle.github

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runners.Parameterized
import java.io.File


abstract class AbstractPluginTest(
    protected val gradleVersion: String
) {

    companion object {

        @Parameterized.Parameters(name = "Gradle {0}")
        @JvmStatic
        fun testedGradleVersions() = listOf(
            "6.0-20190920220032+0000",
            "5.6.2",
            "5.2"
        )
    }

    @Rule
    @JvmField
    val tmpDir = TemporaryFolder()

    protected
    val rootDir: File by lazy {
        tmpDir.root
    }

    @Before
    fun setup() {
        rootDir.resolve("settings.gradle").writeText("")
    }

    protected
    fun withFile(path: String, text: String = "") =
        rootDir.resolve(path).writeText(text.trimIndent())

    protected
    fun withBuildScript(text: String) =
        withFile("build.gradle", text)

    protected
    fun build(vararg arguments: String, block: BuildResult.() -> Unit = {}): BuildResult =
        gradleRunnerFor(*arguments)
            .build()
            .also(block)

    protected
    fun build(environment: Map<String, String>, vararg arguments: String, block: BuildResult.() -> Unit = {}): BuildResult =
        gradleRunnerFor(*arguments)
            .withEnvironment(environment)
            .build()
            .also(block)

    protected
    fun buildAndFail(vararg arguments: String, block: BuildResult.() -> Unit = {}): BuildResult =
        gradleRunnerFor(*arguments)
            .buildAndFail()
            .also(block)

    private
    fun gradleRunnerFor(vararg arguments: String) =
        GradleRunner.create()
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withProjectDir(rootDir)
            .withArguments(*(arguments.toList().plus("-s")).toTypedArray())

    protected
    fun BuildResult.outcomeOf(path: String) =
        task(path)?.outcome
}
