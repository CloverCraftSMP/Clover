plugins {
    alias(libs.plugins.stonecutter)
}

stonecutter active "26.1.x"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String

    var isS3 = current.parsed <= "1.21.1"
    dependencies["vanillabackport"] = if (isS3) node.project.property("deps.vanillabackport") as String else "0.0.0"
    dependencies["tide"] = if (isS3) node.project.property("deps.tide") as String else "0.0.0"

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }

        string(current.parsed >= "26.1") {
            replace("classTweaker v2 named", "classTweaker v2 official")
        }
    }
}

// Make newer versions be published last
stonecutter tasks {
    order("publishModrinth")
}