plugins {
    id("java")
}

group = "me.mrafonso"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "jitpack-repo"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "dmulloy2-repo"
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
    maven {
        name = "extendedclip-repo"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "triumph-repo"
        url = uri("https://repo.triumphteam.dev/snapshots/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")
    compileOnly("com.github.simplix-softworks:simplixstorage:3.2.7")
    compileOnly("dev.triumphteam:triumph-cmd-bukkit:2.0.0-ALPHA-9")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
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