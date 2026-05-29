plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.4.1"
    id("org.jetbrains.kotlin.jvm") version "2.3.21"
    id("org.jetbrains.kotlinx.kover") version "0.9.8"
    kotlin("plugin.serialization") version "2.3.21"
    id("me.champeau.jmh") version "0.7.3"
    id("io.kotest") version "6.1.11"
}

group = "me.mrafonso"
version = "2.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.triumphteam.dev/snapshots/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-snapshots/") }
    maven { url = uri("https://codeberg.org/api/packages/Andre601/maven/") }
}

dependencies {
    fun fullImplementation(dependency: String) {
        implementation(dependency)
        testImplementation(dependency)
    }

    fun fullCompileOnly(dependency: String) {
        compileOnly(dependency)
        testImplementation(dependency)
    }

    implementation(kotlin("stdlib"))
    fullImplementation("dev.triumphteam:triumph-cmd-bukkit:2.0.0-BETA-4")
    fullImplementation("com.github.retrooper:packetevents-spigot:2.12.2-SNAPSHOT")
    fullImplementation("dev.triumphteam:polaris-yaml:1.0.0-SNAPSHOT")
    fullImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    fullImplementation("ch.andre601:expressionparser:1.6.1")
    fullImplementation("org.bstats:bstats-bukkit:3.2.1")

    fullCompileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    fullCompileOnly("me.clip:placeholderapi:2.12.2")
    fullCompileOnly("io.github.miniplaceholders:miniplaceholders-api:3.1.0")

    // Testing specific
    testImplementation("io.netty:netty-buffer:4.1.110.Final")
    testImplementation("io.kotest:kotest-assertions-core:6.1.11")
    testImplementation("io.kotest:kotest-runner-junit5:6.1.11")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.113.1")

    // JMH
    jmhImplementation(kotlin("stdlib"))
    jmhImplementation("io.papermc.paper:paper-api:26.1.2.build.+")
    jmhImplementation("org.openjdk.jmh:jmh-core:1.37")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

jmh {
    timeOnIteration.set("500ms")
    benchmarkMode.set(listOf("thrpt"))
    timeUnit.set("s")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    runServer {
        systemProperty("net.kyori.adventure.text.warnWhenLegacyFormattingDetected", false)
        minecraftVersion("1.21.11")
        javaToolchains {
            launcherFor {
                vendor = JvmVendorSpec.JETBRAINS
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }
    }

    shadowJar {
        relocate("com.github.retrooper", "me.mrafonso.runway.shadow.packetevents")
        relocate("org.bstats", "me.mrafonso.runway.shadow.bstats")
        minimize()
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }
}
