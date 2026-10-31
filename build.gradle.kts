plugins {
    id("java-library")
    id("com.gtnewhorizons.retrofuturagradle") version "2.0.2"
}

val modVersion: String = (findProperty("mod_version") as String?) ?: "0.1.0"

group = "com.netheryx"
version = "1.7.10-$modVersion"

base {
    archivesName.set("netheryx")
}

// Minecraft 1.7.10 se compile et s'execute en Java 8 (Gradle lui-meme tourne en Java 17 ou plus)
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(org.gradle.jvm.toolchain.JvmVendorSpec.AZUL)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// Minecraft 1.7.10 + Forge 10.13.4.1614 (mappings MCP stable_12)
minecraft {
    mcVersion.set("1.7.10")
    username.set("Developer")
}

repositories {
    maven {
        name = "GTNH Maven"
        url = uri("https://nexus.gtnewhorizons.com/repository/public/")
    }
}

// Version du mod dans mcmod.info
tasks.processResources.configure {
    val projVersion = modVersion
    inputs.property("version", projVersion)
    filesMatching("mcmod.info") {
        expand(mapOf("modVersion" to projVersion))
    }
}
