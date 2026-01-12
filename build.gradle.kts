plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.2.2"
    id("org.jetbrains.kotlin.jvm") version "2.2.20"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    kotlin("plugin.serialization") version "2.2.0"
    id("me.champeau.jmh") version "0.7.2"
    id("io.kotest") version "6.0.7"
}

group = "me.mrafonso"
version = "2.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.triumphteam.dev/snapshots/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-releases/") }
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
    fullImplementation("com.github.retrooper:packetevents-spigot:2.11.1")
    fullImplementation("dev.triumphteam:polaris-yaml:1.0.0-SNAPSHOT")
    fullImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    fullImplementation("ch.andre601:expressionparser:1.6.1")
    fullImplementation("org.bstats:bstats-bukkit:3.1.0")

    fullCompileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    fullCompileOnly("me.clip:placeholderapi:2.11.7")
    fullCompileOnly("io.github.miniplaceholders:miniplaceholders-api:3.1.0")

    // Testing specific
    testImplementation("io.kotest:kotest-assertions-core:6.0.7")
    testImplementation("io.kotest:kotest-runner-junit5:6.0.7")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.98.0")

    // JMH
    jmhImplementation(kotlin("stdlib"))
    jmhImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    jmhImplementation("org.openjdk.jmh:jmh-core:1.37")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

jmh {
    timeOnIteration.set("500ms")
    benchmarkMode.set(listOf("thrpt"))
    timeUnit.set("s")
}

kotlin {
    jvmToolchain(21)
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
                languageVersion.set(JavaLanguageVersion.of(21))
            }
            jvmArgs("-XX:+AllowEnhancedClassRedefinition")
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