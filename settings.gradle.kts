pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.legacyfabric.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://repo.spongepowered.org/maven/")
        maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
        maven("https://maven.ornithemc.net/releases")
        maven("https://maven.ornithemc.net/snapshots")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "lan-properties"

sequenceOf(
    "common",
    "fabric",
    "forge",
    "neoforge",
    "quilt",
    "ornithe"
).forEach {
    include(it)
}


val supportedVersions = listOf("v1_21")

supportedVersions.forEach { version ->
    sequenceOf("common", "fabric", "forge", "neoforge", "quilt", "ornithe").forEach { loader ->
        include("$loader-$version")
        project(":$loader-$version").projectDir = file("$loader/$version")
    }
}