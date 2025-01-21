repositories {
  maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
  shadow("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")

  shadow(project(":shared:api"))
}

tasks.shadowJar {
  archiveBaseName.set("survival")
}