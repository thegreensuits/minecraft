dependencies {
  shadow(project(":shared:api"))
}

tasks.shadowJar {
  archiveBaseName.set("core")
}