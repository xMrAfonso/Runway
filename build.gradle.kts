plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "me.mrafonso"
version = "1.1.6"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.triumphteam.dev/snapshots/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-releases/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("com.github.retrooper:packetevents-spigot:2.7.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    compileOnly("com.github.simplix-softworks:simplixstorage:3.2.7")
    compileOnly("dev.triumphteam:triumph-cmd-bukkit:2.0.0-ALPHA-10")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.runServer {
    minecraftVersion("1.21.3")
}

tasks.shadowJar {
    relocate(" com.github.retrooper", "me.mrafonso.shadow.packetevents")
    minimize()
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    outputs.dir("$buildDir/resources")
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
}