import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources

@Suppress("unused")
class CommonPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = target.configure()
}

@Suppress("UnstableApiUsage")
private fun Project.configure() {
    repositories {
        fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
            forRepository { maven(url) { name = alias } }
            filter { groups.forEach {
                includeGroup(it)
                includeGroupAndSubgroups(it)
            } }
        }

        strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
        strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
        strictMaven("https://maven.fabricmc.net/", "Fabric", "net.fabricmc", "org.spongepowered")
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xdoclint:none")
    }

    val currentVersion = findProperty("stonecutter.current") as? String ?: name

    val processResources = tasks.named<ProcessResources>("processResources") {
        fun prop(name: String) = project.property(name).toString().also {
            inputs.property(name, it)
        }

        val props = HashMap<String, String>().apply {
            this["id"] = prop("mod.id")
            this["name"] = prop("mod.name")
            this["version"] = prop("mod.version")
            this["minecraft"] = prop("mod.mc_compat")

            if (currentVersion <= "1.21") {
                this["clutterbestiary"] = prop("deps.clutterbestiary")
                this["status"] = prop("deps.status")
                this["larion"] = prop("deps.larion")
                this["tide"] = prop("deps.tide")
                this["modpack_checker"] = prop("deps.modpack_checker")
                this["horseman"] = prop("deps.horseman")
                this["vanillabackport"] = prop("deps.vanillabackport")
                this["supplementaries"] = prop("deps.supplementaries")
                this["enderscape"] = prop("deps.enderscape")
                this["yacl"] = prop("deps.yacl")
            }
        }

        filesMatching(listOf("fabric.mod.json")) {
            expand(props)
        }
    }
}
