import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.api.unimined

plugins {
    id("java")
    id("idea")
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
    id("xyz.wagyourtail.unimined") version "1.4.2-SNAPSHOT" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

val baseName: String = property("archives_base_name") as String
val modName: String = property("mod_name") as String
val modDescription: String = property("mod_description") as String
val modId: String = property("mod_id") as String
val modVersion: String = property("mod_version") as String

version = modVersion
group = "dev.xhyrom.lanprops"

val versionConfigs = mapOf(
    "v1_21_7" to VersionConfig(
        minecraftVersion = "1.21.8",
        supportedVersions = "1.21.7-1.21.8",
        javaVersion = JavaVersion.VERSION_21,
        mappings = MappingsConfig(
            provider = "mojmap"
        ),
        fabricLoaderVersion = "0.16.14",
        forgeVersion = "58.0.1",
        neoForgeVersion = "10",
        quiltVersion = "0.29.1"
    )
)

data class MappingsConfig(
    val provider: String,
    val version: String? = null
)

data class VersionConfig(
    val minecraftVersion: String,
    val supportedVersions: String,
    val javaVersion: JavaVersion,
    val mappings: MappingsConfig,
    val fabricLoaderVersion: String,
    val forgeVersion: String? = null,
    val neoForgeVersion: String? = null,
    val quiltVersion: String? = null,
    val ornitheVersion: String? = null,
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
        name in listOf("common", "fabric", "forge", "neoforge", "quilt", "ornithe") -> {
            return@subprojects
        }
        name.matches(Regex("(common|fabric|forge|neoforge|quilt|ornithe)-v\\d+(_\\d+)+")) -> {
            configureVersionSpecificModule()
        }
    }
}

fun Project.configureVersionSpecificModule() {
    val (loader, versionKey) = when {
        name.matches(Regex("common-v\\d+(_\\d+)+")) -> {
            val parts = name.split("-")
            "common" to parts[1]
        }
        name.matches(Regex("fabric-v\\d+(_\\d+)+")) -> {
            val parts = name.split("-")
            "fabric" to parts[1]
        }
        name.matches(Regex("forge-v\\d+(_\\d+)+")) -> {
            val parts = name.split("-")
            "forge" to parts[1]
        }
        name.matches(Regex("neoforge-v\\d+(_\\d+)+")) -> {
            val parts = name.split("-")
            "neoforge" to parts[1]
        }
        name.matches(Regex("quilt-v\\d+(_\\d+)+")) -> {
            val parts = name.split("-")
            "quilt" to parts[1]
        }
        name.matches(Regex("ornithe-v\\d+(_\\d+)+")) -> {
            val parts = name.split("-")
            "ornithe" to parts[1]
        }
        else -> throw GradleException("Unknown project format: $name")
    }

    val config = versionConfigs[versionKey] ?: throw GradleException("No config for version: $versionKey")

    val loaderVersionMap = mapOf(
        "forge" to config.forgeVersion,
        "neoforge" to config.neoForgeVersion,
        "quilt" to config.quiltVersion,
        "ornithe" to config.ornitheVersion
    )

    if (loader in loaderVersionMap && loaderVersionMap[loader] == null) {
        println("Skipping $name: $loader not supported for $versionKey")
        return
    }

    base {
        archivesName.set("${baseName}_${loader}-${modVersion}+${config.supportedVersions}")
    }

    java {
        sourceCompatibility = config.javaVersion
        targetCompatibility = config.javaVersion
        toolchain.languageVersion = JavaLanguageVersion.of(config.javaVersion.majorVersion.toInt())
    }

    tasks.processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        inputs.property("mod_id", modId)
        inputs.property("mod_version", modVersion)
        inputs.property("mod_name", modName)
        inputs.property("mod_description", modDescription)
        inputs.property("minecraft_version", config.minecraftVersion)
        inputs.property("supported_versions", config.supportedVersions)

        filesMatching(listOf("pack.mcmeta", "mcmod.info", "fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "${modId}.mixins.json")) {
            expand(inputs.properties)
        }
    }

    when (loader) {
        "common" -> configureCommonVersionModule(config)
        "fabric" -> configureFabricModule(versionKey, config)
        "forge" -> configureForgeModule(versionKey, config)
        "neoforge" -> configureNeoForgeModule(versionKey, config)
        "quilt" -> configureQuiltModule(versionKey, config)
        "ornithe" -> configureOrnitheModule(versionKey, config)
    }
}

fun Project.configureCommonVersionModule(config: VersionConfig) {
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
            when (config.mappings.provider) {
                "mcp" -> {
                    searge()
                    mcp("stable", config.mappings.version!!)
                }
                "mojmap" -> {
                    mojmap()
                }
                else -> throw GradleException("Unknown mappings: ${config.mappings}")
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
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
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

    artifacts {
        add(namedElements.name, tasks.named<ShadowJar>("shadowJar"))
    }
}

fun Project.configureFabricModule(versionKey: String, config: VersionConfig) {
    apply(plugin = "xyz.wagyourtail.unimined")

    val shadowBundle: Configuration by configurations.creating
    val isLegacyFabric = isLegacyVersion(config.minecraftVersion)

    unimined.minecraft {
        version(config.minecraftVersion)

        if (isLegacyFabric) {
            legacyFabric {
                loader(config.fabricLoaderVersion)
            }
        } else {
            fabric {
                loader(config.fabricLoaderVersion)
            }
        }

        mappings {
            when (config.mappings.provider) {
                "mcp" -> {
                    searge()
                    mcp("stable", config.mappings.version!!)
                }
                "mojmap" -> {
                    mojmap()
                }
                else -> throw GradleException("Unknown mappings: ${config.mappings}")
            }
        }

        defaultRemapJar = true
    }

    repositories {
        unimined.fabricMaven()
    }

    dependencies {
        implementation(project(":fabric"))
        unimined.fabricModule("fabric-resource-loader-v0", "0.132.0+1.21.8")

        implementation(project(":common-$versionKey"))
        shadowBundle(project(":common-$versionKey"))
    }

    tasks.named<RemapJarTask>("remapJar") {
        dependsOn(tasks.named<ShadowJar>("shadowJar"))
        asJar {
            inputFile.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
            archiveFileName.set("${base.archivesName.get()}.jar")
        }
    }

    tasks.build {
        dependsOn(tasks.named<RemapJarTask>("remapJar"))
    }

    configureShadowJar(shadowBundle)
}
fun Project.configureForgeModule(versionKey: String, config: VersionConfig) {
    apply(plugin = "xyz.wagyourtail.unimined")

    val shadowBundle: Configuration by configurations.creating
    val isLegacyVersion = config.minecraftVersion.startsWith("1.7.")

    unimined.minecraft {
        version(config.minecraftVersion)

        minecraftForge {
            loader(config.forgeVersion!!)
            if (!isLegacyVersion) {
                mixinConfig("${rootProject.property("mod_id")}.mixins.json")
            }
        }

        mappings {
            when (config.mappings.provider) {
                "mcp" -> {
                    searge()
                    mcp("stable", config.mappings.version!!)
                }
                "mojmap" -> {
                    mojmap()
                }
                else -> throw GradleException("Unknown mappings: ${config.mappings}")
            }
        }

        defaultRemapJar = true
    }

    dependencies {
        implementation(project(":forge"))

        implementation(project(":common-$versionKey"))
        shadowBundle(project(":common-$versionKey"))

        if (isLegacyVersion) {
            implementation("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:0.1.20") {
                isTransitive = false
            }
            annotationProcessor("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:0.1.20") {
                isTransitive = false
            }
        }
    }

    if (isLegacyVersion) {
        tasks.withType(Jar::class) {
            manifest.attributes.run {
                this["FMLCorePluginContainsFMLMod"] = "true"
                this["ForceLoadAsMod"] = "true"
                this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
                this["MixinConfigs"] = "${rootProject.property("mod_id")}.mixins.json"
            }
        }
    }

    tasks.named<RemapJarTask>("remapJar") {
        dependsOn(tasks.named<ShadowJar>("shadowJar"))
        asJar {
            inputFile.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
            archiveFileName.set("${base.archivesName.get()}.jar")
        }
    }

    tasks.build {
        dependsOn(tasks.named<RemapJarTask>("remapJar"))
    }

    configureShadowJar(shadowBundle)
}
fun Project.configureNeoForgeModule(versionKey: String, config: VersionConfig) {
    apply(plugin = "xyz.wagyourtail.unimined")

    val shadowBundle: Configuration by configurations.creating

    unimined.minecraft {
        version(config.minecraftVersion)

        neoForge {
            loader(config.neoForgeVersion!!)
            mixinConfig("${rootProject.property("mod_id")}.mixins.json")
        }

        mappings {
            when (config.mappings.provider) {
                "mcp" -> {
                    searge()
                    mcp("stable", config.mappings.version!!)
                }
                "mojmap" -> {
                    mojmap()
                }
                else -> throw GradleException("Unknown mappings: ${config.mappings}")
            }
        }

        defaultRemapJar = true
    }

    dependencies {
        implementation(project(":neoforge"))

        implementation(project(":common-$versionKey"))
        shadowBundle(project(":common-$versionKey"))
    }

    tasks.named<RemapJarTask>("remapJar") {
        dependsOn(tasks.named<ShadowJar>("shadowJar"))
        asJar {
            inputFile.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
            archiveFileName.set("${base.archivesName.get()}.jar")
        }
    }

    tasks.build {
        dependsOn(tasks.named<RemapJarTask>("remapJar"))
    }

    configureShadowJar(shadowBundle)
}
fun Project.configureQuiltModule(versionKey: String, config: VersionConfig) {
    apply(plugin = "xyz.wagyourtail.unimined")

    val shadowBundle: Configuration by configurations.creating

    unimined.minecraft {
        version(config.minecraftVersion)

        quilt {
            loader(config.quiltVersion!!)
        }

        mappings {
            when (config.mappings.provider) {
                "mcp" -> {
                    searge()
                    mcp("stable", config.mappings.version!!)
                }
                "mojmap" -> {
                    mojmap()
                }
                else -> throw GradleException("Unknown mappings: ${config.mappings}")
            }
        }

        defaultRemapJar = true
    }

    dependencies {
        implementation(project(":quilt"))

        implementation(project(":common-$versionKey"))
        shadowBundle(project(":common-$versionKey"))
    }

    tasks.named<RemapJarTask>("remapJar") {
        dependsOn(tasks.named<ShadowJar>("shadowJar"))
        asJar {
            inputFile.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
            archiveFileName.set("${base.archivesName.get()}.jar")
        }
    }

    tasks.build {
        dependsOn(tasks.named<RemapJarTask>("remapJar"))
    }

    configureShadowJar(shadowBundle)
}
fun Project.configureOrnitheModule(versionKey: String, config: VersionConfig) {
    apply(plugin = "xyz.wagyourtail.unimined")

    val shadowBundle: Configuration by configurations.creating

    unimined.minecraft {
        version(config.minecraftVersion)

        ornitheFabric {
            loader(config.ornitheVersion!!)
        }

        mappings {
            when (config.mappings.provider) {
                "mcp" -> {
                    searge()
                    mcp("stable", config.mappings.version!!)
                }
                "mojmap" -> {
                    mojmap()
                }
                else -> throw GradleException("Unknown mappings: ${config.mappings}")
            }
        }

        defaultRemapJar = true
    }

    dependencies {
        implementation(project(":ornithe"))

        implementation(project(":common-$versionKey"))
        shadowBundle(project(":common-$versionKey"))
    }

    tasks.named<RemapJarTask>("remapJar") {
        dependsOn(tasks.named<ShadowJar>("shadowJar"))
        asJar {
            inputFile.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
            archiveFileName.set("${base.archivesName.get()}.jar")
        }
    }

    tasks.build {
        dependsOn(tasks.named<RemapJarTask>("remapJar"))
    }

    configureShadowJar(shadowBundle)
}


fun isLegacyVersion(version: String): Boolean {
    val parts = version.split(".")
    if (parts.size < 2) return false

    val major = parts[0].toIntOrNull() ?: return false
    val minor = parts[1].toIntOrNull() ?: return false

    return major == 1 && minor <= 13
}

fun Project.configureShadowJar(shadowBundle: Configuration) {
    tasks.named<ShadowJar>("shadowJar") {
        configurations = listOf(shadowBundle)
        archiveClassifier = "dev-shadow"

        from("${project.rootDir}/LICENSE") {
            into("")
        }

        relocate("org.tinylog", "dev.xhyrom.lanprops.shadow.tinylog")
        relocate("com.google.gson", "dev.xhyrom.lanprops.shadow.gson")

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