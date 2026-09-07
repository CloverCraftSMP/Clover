import org.jetbrains.java.decompiler.api.Decompiler
import org.jetbrains.java.decompiler.main.decompiler.SingleFileSaver

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.vineflower:vineflower:${property("deps.vineflower_source_decomp")}")
    }
}


plugins {
    alias(libs.plugins.mpp)
    alias(libs.plugins.loomx)
    alias(ft.plugins.default)
    alias(ft.plugins.fabric)
    alias(ft.plugins.mixin)
    alias(ft.plugins.dependency)
    id("clover-common")
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

// This can be used for publishing on Modrinth and Curseforge
val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

repositories {
    mavenLocal()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.bawnorton.com/releases")
    maven("https://maven.blamejared.com")
    maven("https://maven.isxander.dev/releases")
}

dependencies {
    /**
     * Fetches only the required Fabric API modules to not waste time downloading all of them for each version.
     * @see <a href="https://github.com/FabricMC/fabric">List of Fabric API modules</a>
     */
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(fabricApi.module(it, sc.properties["deps.fabric_api"]))
    }

    fun resolveModsModrinth(vararg mods: String) {
        for (it in mods) {
            fletchingTable.modrinth(it, sc.current.version)?.let { dep ->
                modCompileOnly(dep)
            }
        }
    }

    fun resolvePinnedMod(vararg mods: String) {
        for (mod in mods) {
            fletchingTable.modrinth(mod, sc.current.version) {
                limit = 50
                version = property(mod).toString()
            }?.let { dep ->
                modCompileOnly(dep)
            }
        }
    }

    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    fletchingTable.modrinth("fabric-api", sc.current.version)?.let { dep ->
        modRuntimeOnly(dep)
    }

    if (sc.current.version <= "1.21.1") {
        resolveModsModrinth(
            "clutterbestiary",
            "larion-worldgen",
            "modpack-checker",
            "horseman",
            "vanillabackport",
            "supplementaries",
            "tide"
        )
        modCompileOnly("com.blamejared.crafttweaker:CraftTweaker-fabric-1.21.1:${property("deps.crafttweaker")}")
        include(modImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")!!)
    }

    resolveModsModrinth("status", "simple-copper-pipes")

    include(modImplementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${property("deps.mixin_squared")}")!!)!!)
    include(modImplementation("com.moulberry:mixinconstraints:${property("deps.mixinconstraints")}")!!)

    fapi("fabric-lifecycle-events-v1", "fabric-resource-loader-v0", "fabric-content-registries-v0", "fabric-data-generation-api-v1", "fabric-loot-api-v3", "fabric-game-rule-api-v1", "fabric-command-api-v2")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
    accessWidenerPath = sc.process(
        rootProject.file("src/main/resources/clover.ct"),
        "build/processed.ct"
    )

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        // vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging)
    }
}

fabricApi {
    configureDataGeneration {
        client = false
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}


tasks {
    processResources {
        /** handled under build-logic **/
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }

    register("genModCompileOnlySources") {
        group = "fabric"

        val remappedJars = project.configurations["compileClasspath"]
            .resolvedConfiguration
            .resolvedArtifacts
            .map { it.file }
            .filter { file ->
                file.path.contains("loom-cache/remapped_mods") &&
                !file.name.endsWith("-sources.jar")
            }

        doLast {
            remappedJars.forEach { jar ->
                val sourcesJar = File(jar.parentFile, jar.name.replace(".jar", "-sources.jar"))

                if (sourcesJar.exists()) {
                    println("Skipping ${jar.name}, sources already exist")
                    return@forEach
                }

                println("Decompiling ${jar.name}...")

                Decompiler.Builder()
                    .inputs(jar)
                    .output(SingleFileSaver(sourcesJar))
                    .build()
                    .decompile()

                println("  -> ${sourcesJar.name}")
            }
        }
    }
}

fletchingTable {
    mixins.configure(sourceSets.main) {
        mixin("clover.mixins.json")
    }

    fabric.configure(sourceSets.main) {
        entrypoint("main", "com.clovercraftsmp.clover.Clover")
        entrypoint("client", "com.clovercraftsmp.clover.client.CloverClient")
        entrypoint("fabric-datagen", "com.clovercraftsmp.clover.datagen.CloverDataGenerator")
        entrypoint("mixinsquared", "com.clovercraftsmp.clover.conditional.MixinCanceller")
    }
}

// Publishes builds to Modrinth with changelog from the CHANGELOG.md file
//publishMods {
//    file = tasks.remapJar.map { it.archiveFile.get() }
//    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile.get() })
//    displayName = "${property("mod.name")} ${property("mod.version")} for ${property("mod.mc_title")}"
//    version = property("mod.version") as String
//    changelog = rootProject.file("CHANGELOG.md").readText()
//    type = STABLE
//    modLoaders.add("fabric")
//
//    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null
//
//    modrinth {
//        projectId = property("publish.modrinth") as String
//        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
//        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))
//        requires {
//            slug = "fabric-api"
//        }
//    }
//
//    github {
//        repository = property("publish.github_repo") as String
//        accessToken = providers.environmentVariable("GITHUB_TOKEN")
//        commitish = "main"
//    }
//}