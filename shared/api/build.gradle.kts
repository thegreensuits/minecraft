java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

dependencies {
  implementation("redis.clients:jedis:5.2.0")

  // - Commons
  implementation("org.apache.commons:commons-lang3:3.17.0")
  implementation("com.google.guava:guava:11.0.2")

  compileOnly("org.projectlombok:lombok:1.18.36")
  annotationProcessor("org.projectlombok:lombok:1.18.36")
  
  testCompileOnly("org.projectlombok:lombok:1.18.36")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
}