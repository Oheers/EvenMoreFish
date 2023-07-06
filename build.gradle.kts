plugins {
    id("java")
    id("maven-publish")
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

group = "com.oheers.evenmorefish"
version = "1.6.11.3"

description = "A fishing extension bringing an exciting new experience to fishing."

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://github.com/deanveloper/SkullCreator/raw/mvn-repo/")
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://raw.githubusercontent.com/FabioZumbi12/RedProtect/mvn-repo/")
    maven("https://libraries.minecraft.net/")
    maven("https://nexus.neetgames.com/repository/maven-releases/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://repo.essentialsx.net/releases/")
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.vault.api)
    compileOnly(libs.placeholder.api)
    compileOnly(libs.authlib)

    compileOnly(libs.worldguard.core)
    compileOnly(libs.worldguard.bukkit)
    compileOnly(libs.worldedit.core)
    compileOnly(libs.worldedit.bukkit)

    compileOnly(libs.redprotect.core)
    compileOnly(libs.redprotect.spigot)
    compileOnly(libs.aurelium.skills)
    compileOnly(libs.griefprevention)
    compileOnly(libs.itemsadder.api)
    compileOnly(libs.mcmmo)

    implementation(libs.nbt.api)
    implementation(libs.bstats)

    library(libs.friendlyid)
    library(libs.flyway.core)
    library(libs.flyway.mysql)
    library(libs.hikaricp)
    library(libs.caffeine)
}

bukkit {
    name = "EvenMoreFish"
    author = "Oheers"
    main = "com.oheers.fish.EvenMoreFish"
    version = project.version.toString()
    description = project.description.toString()
    website = "https://github.com/Oheers/EvenMoreFish"

    depend = listOf("Vault")
    softDepend = listOf("WorldGuard", "PlaceholderAPI", "RedProtect", "mcMMO", "AureliumSkills", "ItemsAdder")
    loadBefore = listOf("AntiAC")
    apiVersion = "1.16"

    commands {
        register("evenmorefish") {
            usage = "/<command> [name]"
            aliases = listOf("emf")
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        minimize()

        exclude("META-INF/**")

        archiveFileName.set("EvenMoreFish-${project.version}.jar")
        archiveClassifier.set("shadow")

        relocate("de.tr7zw.changeme.nbtapi", "com.oheers.fish.utils.nbt")
        relocate("org.bstats", "com.oheers.evenmorefish.bstats")
    }
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}