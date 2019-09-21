plugins {
    `build-scan`
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "org.nosphere.gradle.github"
version = "1.0.0-SNAPSHOT"

val isCI = System.getenv("CI") == "true"
if (isCI) {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        tag("CI")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
    jvmTarget.set("1.6")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation(gradleTestKit())
}

tasks.validateTaskProperties {
    failOnWarning = true
    enableStricterValidation = true
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
