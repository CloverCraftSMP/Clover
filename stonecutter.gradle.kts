plugins {
    alias(libs.plugins.stonecutter)
}

stonecutter active "1.21.1"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
    dependencies["vanillabackport"] = node.project.property("deps.vanillabackport") as String
    dependencies["tide"] = node.project.property("deps.tide") as String

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}

// Make newer versions be published last
stonecutter tasks {
    order("publishModrinth")
}