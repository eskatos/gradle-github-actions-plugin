plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
    id("org.nosphere.gradle.github.actions") version "1.3.2"
}

group = "org.nosphere.gradle.github"
version = "1.4.0-SNAPSHOT"

gradlePlugin {
    plugins {
        named("org.nosphere.gradle.github.actions") {
            displayName = "Gradle Github Actions Plugin"
            description = "Gradle Plugin for Github Actions"
        }
    }
}

pluginBundle {
    website = "https://github.com/eskatos/gradle-github-actions-plugin"
    vcsUrl = "https://github.com/eskatos/gradle-github-actions-plugin"
    tags = listOf("github-actions", "scans")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withSourcesJar()
}

kotlinDslPluginOptions {
    jvmTarget.set("1.8")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation(gradleTestKit())
}

tasks.validatePlugins {
    failOnWarning.set(true)
    enableStricterValidation.set(true)
}
