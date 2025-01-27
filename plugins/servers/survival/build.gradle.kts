repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  shadow("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

  shadow(project(":plugins:api"))
}

tasks.shadowJar {
  archiveBaseName.set("survival")
}