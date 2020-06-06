plugins {
    `gradle-enterprise`
}

rootProject.name = "gradle-github-actions-plugin"

val isCI = System.getenv("CI") == "true"
if (isCI) {
    gradleEnterprise {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            tag("CI")
        }
    }
}
