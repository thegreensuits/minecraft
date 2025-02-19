plugins {
    id("java")
}

group = "fr.thegreensuits"
version = "1.0-SNAPSHOT"
description = "TheGreenSuits Velocity proxy"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    shadow("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    implementation(project(":plugins:api"))

    implementation("redis.clients:jedis:5.2.0")
    implementation("org.spongepowered:configurate-yaml:4.1.2")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    
    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
}

tasks.shadowJar {
    archiveBaseName.set("proxy")
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates by tasks.registering(Copy::class) {
    val props = mapOf("version" to project.version, "group" to project.group, "description" to project.description)
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main {
    java.srcDir(generateTemplates.map { it.outputs })
}