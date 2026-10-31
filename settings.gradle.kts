rootProject.name = "netheryx"

pluginManagement {
    repositories {
        maven {
            // RetroFuturaGradle : outil de compilation des mods Forge 1.7.10
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
                includeGroup("com.gtnewhorizons")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Telecharge automatiquement le JDK 8 necessaire a la 1.7.10
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
