rootProject.name = "mio"

plugins {
  id("com.meowool.toolkit.gradle-dsl-x") version "2.1"
}

buildscript {
  repositories {
    mavenCentral()
    google()
  }
  configurations.all {
    resolutionStrategy.force("com.android.tools.build:gradle:4.1.1")
  }
}

rootGradleDslX {
  useMeowoolSpec()
  allprojects {
    afterEvaluate {
      dokka(DokkaFormat.Html) {
        outputDirectory.set(rootDir.resolve("docs/apis"))
      }
    }
  }
}

importProjects(rootDir)