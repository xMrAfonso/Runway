plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "me.mrafonso"
version = "1.1.0"

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
        name = "extendedclip-repo"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "triumph-repo"
        url = uri("https://repo.triumphteam.dev/snapshots/")
    }
    maven {
        name = "codemc-repo"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    //compileOnly("com.github.retrooper.packetevents:spigot:2.2.1") Waiting for 2.2.2 to fix an issue
    implementation(files("libs/packetevents-spigot-2.2.1.jar"))
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

tasks.shadowJar {
    relocate("com.github.retrooper", "me.mrafonso.packetevents")
    minimize()
}