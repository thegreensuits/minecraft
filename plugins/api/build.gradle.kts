repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  // - Velocity
  shadow("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
  annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

  // - Paper
  shadow("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

  // - Jedis
  implementation("redis.clients:jedis:5.2.0")

  // - Commons
  implementation("org.apache.commons:commons-lang3:3.17.0")
  implementation("com.google.guava:guava:11.0.2")
  implementation("com.google.code.gson:gson:2.12.1")

  compileOnly("org.projectlombok:lombok:1.18.36")
  annotationProcessor("org.projectlombok:lombok:1.18.36")
  
  testCompileOnly("org.projectlombok:lombok:1.18.36")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
}