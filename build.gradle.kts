import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.wagyourtail.unimined.api.unimined

plugins {
    id("java")
    id("idea")
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
    id("xyz.wagyourtail.unimined") version "1.4.2-SNAPSHOT" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

val baseName: String = property("archives_base_name") as String
val modId: String = property("mod_id") as String
val modVersion: String = property("mod_version") as String

version = modVersion
group = "dev.xhyrom.lanprops"

val versionConfigs = mapOf(
    "v1_21" to VersionConfig(
        minecraftVersion = property("minecraft_version_1_21") as String,
        supportedVersions = property("supported_minecraft_versions_1_21") as String,
        javaVersion = JavaVersion.VERSION_21,
        mcpVersion = property("mcp_version_1_21") as String,
        fabricLoaderVersion = property("fabric_loader_version_1_21") as String,
        forgeVersion = property("forge_version_1_21") as String,
        useLegacyFabric = true
    )
)

data class VersionConfig(
    val minecraftVersion: String,
    val supportedVersions: String,
    val javaVersion: JavaVersion,
    val mcpVersion: String? = null,
    val fabricLoaderVersion: String,
    val forgeVersion: String? = null,
    val neoforgeVersion: String? = null,
    val useLegacyFabric: Boolean = false
)

subprojects {
    apply(plugin = "java")
    apply(plugin = "me.modmuss50.mod-publish-plugin")
    apply(plugin = "com.github.johnrengelman.shadow")

    project.version = rootProject.version
    project.group = rootProject.group

    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/maven/")
        maven("https://repo.sleeping.town/")
        maven("https://libraries.minecraft.net")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.legacyfabric.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://jitpack.io")
    }

    tasks.withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }

    when {
        name in listOf("common", "fabric", "forge") -> {
            return@subprojects
        }
        name.matches(Regex("(common|fabric|forge)-v\\d+_\\d+")) -> {
            configureVersionSpecificModule()
        }
    }
}

fun Project.configureVersionSpecificModule() {
    val (loader, versionKey) = when {
        name.matches(Regex("common-v\\d+_\\d+")) -> {
            val parts = name.split("-")
            "common" to parts[1]
        }
        name.matches(Regex("fabric-v\\d+_\\d+")) -> {
            val parts = name.split("-")
            "fabric" to parts[1]
        }
        name.matches(Regex("forge-v\\d+_\\d+")) -> {
            val parts = name.split("-")
            "forge" to parts[1]
        }
        name.matches(Regex("neoforge-v\\d+_\\d+")) -> {
            val parts = name.split("-")
            "neoforge" to parts[1]
        }
        else -> throw GradleException("Unknown project format: $name")
    }

    val config = versionConfigs[versionKey] ?: throw GradleException("No config for version: $versionKey")

    base {
        archivesName.set("${baseName}_${loader}-${modVersion}+${config.supportedVersions}")
    }

    java {
        sourceCompatibility = config.javaVersion
        targetCompatibility = config.javaVersion
        toolchain.languageVersion = JavaLanguageVersion.of(config.javaVersion.majorVersion.toInt())
    }

    tasks.processResources {
        inputs.property("version", modVersion)
        inputs.property("mod_id", modId)
        inputs.property("minecraft_version", config.minecraftVersion)
        inputs.property("supported_versions", config.supportedVersions)

        filesMatching(listOf("mcmod.info", "fabric.mod.json", "META-INF/mods.toml", "${modId}.mixins.json")) {
            expand(inputs.properties)
        }
    }

    sourceSets {
        main {
            java {
                srcDir("../src/main/java")
                srcDir("src/main/java")
            }
            resources {
                srcDir("../src/main/resources")
                srcDir("src/main/resources")
            }
        }
    }

    when (loader) {
        "common" -> configureCommonVersionModule(versionKey, config)
        "fabric" -> configureFabricModule(versionKey, config)
        "forge" -> configureForgeModule(versionKey, config)
        "neoforge" -> configureNeoForgeModule(versionKey, config)
    }
}

fun Project.configureCommonVersionModule(versionKey: String, config: VersionConfig) {
    apply(plugin = "xyz.wagyourtail.unimined")
    apply(plugin = "java-library")

    val shadowBundle: Configuration by configurations.creating
    val namedElements: Configuration by configurations.creating {
        isCanBeConsumed = true
        isCanBeResolved = false
    }

    unimined.minecraft {
        version(config.minecraftVersion)

        mappings {
            if (config.useLegacyFabric) {
                searge()
                mcp("stable", config.mcpVersion!!)
            } else {
                mojmap()
            }
        }

        defaultRemapJar = false
    }

    dependencies {
        "api"(project(":common"))
        shadowBundle(project(":common"))

        compileOnly("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    }

    tasks.named<ShadowJar>("shadowJar") {
        configurations = listOf(shadowBundle)
        archiveClassifier = "no-remap"
    }

    artifacts {
        add(namedElements.name, tasks.named<ShadowJar>("shadowJar"))
    }
}
fun Project.configureFabricModule(versionKey: String, config: VersionConfig) {
    apply(plugin = "xyz.wagyourtail.unimined")

    val shadowBundle: Configuration by configurations.creating

    unimined.minecraft {
        version(config.minecraftVersion)

        if (config.useLegacyFabric) {
            legacyFabric {
                loader(config.fabricLoaderVersion)
            }
            mappings {
                searge()
                mcp("stable", config.mcpVersion!!)
            }
        } else {
            fabric {
                loader(config.fabricLoaderVersion)
            }
            mappings {
                mojmap()
            }
        }

        defaultRemapJar = true
    }

    dependencies {
        implementation(project(":fabric"))

        implementation(project(":common-$versionKey"))
        shadowBundle(project(":common-$versionKey"))
    }

    configureShadowJar(shadowBundle)
}
fun Project.configureForgeModule(versionKey: String, config: VersionConfig) {
    apply(plugin = "xyz.wagyourtail.unimined")

    val shadowBundle: Configuration by configurations.creating

    unimined.minecraft {
        version(config.minecraftVersion)

        if (config.useLegacyFabric) { // Using this flag to determine if it's legacy
            minecraftForge {
                loader(config.forgeVersion!!)
                mixinConfig("${rootProject.property("mod_id")}.mixins.json")
            }
            mappings {
                searge()
                mcp("stable", config.mcpVersion!!)
            }
        } else {
            neoForge {
                loader(config.neoforgeVersion!!)
                mixinConfig("${rootProject.property("mod_id")}.mixins.json")
            }
            mappings {
                mojmap()
            }
        }

        defaultRemapJar = true
    }

    dependencies {
        implementation(project(":forge"))

        implementation(project(":common-$versionKey"))
        shadowBundle(project(":common-$versionKey"))

        if (config.useLegacyFabric) { // Legacy forge
            implementation("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:0.1.20") {
                isTransitive = false
            }
            annotationProcessor("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:0.1.20") {
                isTransitive = false
            }
        }
    }

    if (config.useLegacyFabric) {
        tasks.withType(Jar::class) {
            manifest.attributes.run {
                this["FMLCorePluginContainsFMLMod"] = "true"
                this["ForceLoadAsMod"] = "true"
                this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
                this["MixinConfigs"] = "${rootProject.property("mod_id")}.mixins.json"
            }
        }
    }

    configureShadowJar(shadowBundle)
}
fun Project.configureNeoForgeModule(versionKey: String, config: VersionConfig) {
    apply(plugin = "xyz.wagyourtail.unimined")

    val shadowBundle: Configuration by configurations.creating

    unimined.minecraft {
        version(config.minecraftVersion)

        neoForge {
            loader(config.neoforgeVersion!!)
            mixinConfig("${rootProject.property("mod_id")}.mixins.json")
        }
        mappings {
            mojmap()
        }

        defaultRemapJar = true
    }

    dependencies {
        implementation(project(":neoforge"))

        implementation(project(":common-$versionKey"))
        shadowBundle(project(":common-$versionKey"))

        if (config.useLegacyFabric) { // Legacy forge
            implementation("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:0.1.20") {
                isTransitive = false
            }
            annotationProcessor("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:0.1.20") {
                isTransitive = false
            }
        }
    }

    if (config.useLegacyFabric) {
        tasks.withType(Jar::class) {
            manifest.attributes.run {
                this["FMLCorePluginContainsFMLMod"] = "true"
                this["ForceLoadAsMod"] = "true"
                this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
                this["MixinConfigs"] = "${rootProject.property("mod_id")}.mixins.json"
            }
        }
    }

    configureShadowJar(shadowBundle)
}


fun Project.configureShadowJar(shadowBundle: Configuration) {
    tasks.named<ShadowJar>("shadowJar") {
        configurations = listOf(shadowBundle)
        archiveClassifier = "dev-shadow"

        from("${project.rootDir}/LICENSE") {
            into("")
        }

        relocate("org.tinylog", "dev.xhyrom.lanprops.shadow.tinylog")

        mergeServiceFiles()
    }

}

fun getLatestChangelog(): String {
    val lines = rootProject.rootDir.resolve("CHANGELOG.md").readLines()
    val changelogLines = mutableListOf<String>()
    var inSegment = false

    for (line in lines) {
        if (line.startsWith("## ")) {
            if (inSegment) break
            inSegment = true
        }
        if (inSegment) {
            changelogLines += line
        }
    }

    return changelogLines.joinToString("\n").trim()
}

tasks.register("viewLatestChangelog") {
    group = "documentation"
    description = "Print the topmost single version section from the full CHANGELOG.md file."

    doLast {
        println(getLatestChangelog())
    }
}