import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  java; kotlin;
  kotlin("plugin.serialization") version "1.5.20"
  id("io.morethan.jmhreport") version "0.9.0"
}
tasks.withType<Test> {
  useJUnitPlatform()
}
dependencies {
  implementationOf(
    Libs.Kotlin.Reflect,
    Libs.Square.Okio,
    Libs.Square.Kotlinpoet,
    Libs.Apache.Commons.Compress,
    "org.openjdk.jmh:jmh-core:_",
    "com.meowool.toolkit:sweekt:_",
    "io.kotest:kotest-runner-junit5:_",
  )
  implementation("org.openjdk.jol:jol-core:_")
  implementation("it.unimi.dsi:fastutil-core:8.5.4")
  implementation("org.eclipse.collections:eclipse-collections:10.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
  annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:_")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
  languageVersion = "1.5"
}