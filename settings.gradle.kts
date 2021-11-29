@file:Suppress("SpellCheckingInspection")

rootProject.name = "mio"

pluginManagement {
  repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.meowool.gradle.toolkit") version "0.1.0-SNAPSHOT"
}

buildscript {
  configurations.all {
    // Check for updates every build
    resolutionStrategy {
      force("com.android.tools.build:gradle:4.2.2")
      cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }
  }
}

dependencyMapper {
  libraries {
    // TODO Remove
    map("com.meowool.toolkit:sweekt" to "Meowool.Toolkit.Sweekt")
  }
}

gradleToolkit {
  useMeowoolSpec()
  allprojects {
    optIn("com.meowool.io.InternalMioApi")
    dokka(DokkaFormat.Html) {
      outputDirectory.set(rootDir.resolve("docs/apis"))
    }
  }
  publications {
    data {
      val baseVersion = "0.1.0"
      version = "$baseVersion-LOCAL"
      // Used to publish non-local versions of artifacts in CI environment
      versionInCI = "$baseVersion-SNAPSHOT"

      displayName = "Meowool I/O"
      artifactId = "io"
      groupId = "com.meowool.toolkit"
      description = "A fast and easy-to-use multiplatform I/O Library of meowool-toolkit."
      url = "https://github.com/meowool-toolkit/${rootProject.name}"
      vcs = "$url.git"
      developer {
        id = "rin"
        name = "Rin Orz"
        url = "https://github.com/RinOrz/"
      }
    }
  }
}

importProjects(rootDir)

// Only set in the CI environment, waiting the issue to be fixed:
// https://youtrack.jetbrains.com/issue/KTIJ-15015
if (isCiEnvironment) extra["kotlin.mpp.enableGranularSourceSetsMetadata"] = true