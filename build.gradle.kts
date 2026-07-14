plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.ploceus)
}

group = property("project.group") as String
version = property("project.version") as String

ploceus {
    setIntermediaryGeneration(2)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(ploceus.featherMappings(property("feather.build") as String))
    modImplementation(libs.fabric.loader)
}

kotlin {
    jvmToolchain(8)
}