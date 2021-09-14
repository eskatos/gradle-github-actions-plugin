plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"
    id("org.nosphere.gradle.github.actions") version "1.2.0"
}

group = "org.nosphere.gradle.github"
version = "1.3.0-SNAPSHOT"

pluginBundle {
    website = "https://github.com/eskatos/gradle-github-actions-plugin"
    vcsUrl = "https://github.com/eskatos/gradle-github-actions-plugin"
    description = "Gradle Plugin for Github Actions"
    tags = listOf("github-actions", "scans")
    plugins {
        named("org.nosphere.gradle.github.actions") {
            displayName = "Gradle Github Actions Plugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
    jvmTarget.set("1.8")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation(gradleTestKit())
}

tasks.validatePlugins {
    failOnWarning.set(true)
    enableStricterValidation.set(true)
}

val sourcesJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    archiveClassifier.set("sources")
    from(sourceSets.main.map { it.allSource })
    from(layout.buildDirectory.dir("generated-sources/kotlin-dsl-plugins/kotlin"))
}

publishing {
    publications {
        register<MavenPublication>("pluginMaven") {
            artifact(sourcesJar.get())
        }
    }
}
