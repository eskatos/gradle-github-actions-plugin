# Gradle Plugin for Github Actions

This Gradle plugin allows your build to easily get Github Actions environment and tag Gradle Build Scans accordingly.

Also see the [Gradle Command](https://github.com/marketplace/actions/gradle-command) Github Action that allows to execute Gradle commands.

[![Build Status](https://github.com/eskatos/gradle-github-actions-plugin/workflows/CI/badge.svg)](https://github.com/eskatos/gradle-github-actions-plugin/actions)

## Usage

```kotlin
plugins {
    id("org.nosphere.gradle.github.actions") version "1.2.0"
}

// for example:
if (githubActions.running.get()) {
    println("Commit SHA: ${githubActions.environment.sha.get()}")
}
```

If you have the Gradle Enterprise or Gradle Build Scan plugin applied, or use `--scan`, your build scans will automatically be tagged `github:action` and have the following custom values attached:

![tag](src/docs/images/build-scan-tag.png "Build Scan tag")

![tag](src/docs/images/build-scan-values.png "Build Scan tag")

https://scans.gradle.com/s/o5bk2fu3zwm3y

You can disable that or change the tag and values prefix:

```kotlin
githubActions.buildScan {
    autoTag.set(false)
    autoTagPrefix.set("ga:")
}
```

## Compatibility matrix

| Plugin | Java | Gradle | Build Scan
| --- | --- | --- | ---
| `1.2.0` | `>= 1.8` | `>= 6.1` | `>= 3.0`
| `1.1.0` | `>= 1.8` | `>= 5.2` | `>= 1.1`
| `1.0.0` | `>= 1.8` | `>= 5.2` | `>= 1.1`
