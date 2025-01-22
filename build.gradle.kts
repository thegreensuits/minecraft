plugins {
  kotlin("jvm") version "1.8.0"
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

buildscript {
  repositories {
    mavenCentral()
    mavenLocal()
    maven("https://plugins.gradle.org/m2/")
  }

  dependencies {
    classpath("com.github.johnrengelman:shadow:8.1.1")
  }
}

group = "fr.thegreensuits"
version = "1.0-SNAPSHOT"

allprojects {
  repositories {
    mavenCentral()
    mavenLocal()
  }
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "com.github.johnrengelman.shadow")

  group = "fr.thegreensuits"
  version = "1.0-SNAPSHOT"

  java {
    toolchain {
      languageVersion.set(JavaLanguageVersion.of(21))
    }
  }

  /* tasks {
    register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
      archiveClassifier.set("all")
      dependencies {
        include(dependency("com.google.guava:guava:31.1-jre"))
      }
      relocate("com.google.common", "fr.thegreensuits.libs.common")
    }

    build {
      dependsOn("shadowJar")
    }
  } */
}