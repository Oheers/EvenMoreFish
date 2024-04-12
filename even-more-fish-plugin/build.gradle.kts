plugins {
    id("java")
    id("maven-publish")
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.oheers.evenmorefish"
version = "1.6.11.17"

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
    maven("https://repo.auxilor.io/repository/maven-public/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.firedev.uk/repository/maven-public/")
    maven("https://repo.essentialsx.net/releases/")
}

dependencies {
    compileOnly(libs.spigot.api)
    implementation(project(":even-more-fish-api"))
    compileOnly(libs.vault.api)
    compileOnly(libs.placeholder.api)
    compileOnly(libs.authlib)

    compileOnly(libs.worldguard.core) {
        exclude("com.sk89q.worldedit", "worldedit-core")
    }
    compileOnly(libs.worldguard.bukkit)
    compileOnly(libs.worldedit.core)
    compileOnly(libs.worldedit.bukkit)

    compileOnly(libs.redprotect.core) {
        exclude("net.ess3", "EssentialsX")
        exclude("org.spigotmc", "spigot-api")
    }
    compileOnly(libs.redprotect.spigot) {
        exclude("net.ess3", "EssentialsX")
        exclude("org.spigotmc", "spigot-api")
        exclude("com.destroystokyo.paper", "paper-api")
        exclude("de.keyle", "mypet")
        exclude("com.sk89q.worldedit", "worldedit-core")
        exclude("com.sk89q.worldedit", "worldedit-bukkit")
        exclude("com.sk89q.worldguard", "worldguard-bukkit")
    }
    compileOnly(libs.aurelium.skills)
    compileOnly(libs.griefprevention)
    compileOnly(libs.mcmmo)
    compileOnly(libs.headdatabase.api)
    compileOnly(libs.playerpoints)
    compileOnly(libs.cmi.api)
    compileOnly(libs.essx.api)

    implementation(libs.nbt.api)
    implementation(libs.bstats)
    implementation(libs.universalscheduler)
    implementation(libs.acf)

    library(libs.friendlyid)
    library(libs.flyway.core)
    library(libs.flyway.mysql)
    library(libs.hikaricp)
    library(libs.caffeine)
    library(libs.commons.lang3)
    library(libs.commons.codec)

    library(libs.json.simple)
}

bukkit {
    name = "EvenMoreFish"
    author = "Oheers"
    main = "com.oheers.fish.EvenMoreFish"
    version = project.version.toString()
    description = project.description.toString()
    website = "https://github.com/Oheers/EvenMoreFish"
    foliaSupported = true

    depend = listOf(
        "Vault"
    )
    softDepend = listOf(
        "PlayerPoints",
        "WorldGuard",
        "PlaceholderAPI",
        "RedProtect",
        "mcMMO",
        "AureliumSkills",
        "ItemsAdder",
        "Denizen",
        "EcoItems",
        "Oraxen",
        "HeadDatabase",
        "GriefPrevention"
    )
    loadBefore = listOf("AntiAC")
    apiVersion = "1.16"

    commands {
        register("evenmorefish") {
            usage = "/<command> [name]"
            aliases = listOf("emf")
        }
    }

    permissions {
        register("emf.*") {
            children = listOf(
                "emf.admin",
                "emf.user"
            )
        }

        register("emf.admin") {
            children = listOf(
                "emf.admin.update.notify",
                "emf.admin.migrate"
            )
        }

        register("emf.admin.update.notify") {
            description = "Allows users to be notified about updates."
        }

        register("emf.admin.migrate") {
            description = "Allows users to use the migrate command."
        }

        register("emf.user") {
            children = listOf(
                "emf.toggle",
                "emf.top",
                "emf.shop",
                "emf.use_rod",
                "emf.sellall"
            )
        }

        register("emf.sellall") {
            description = "Allows users to use sellall."
        }
        register("emf.toggle") {
            description = "Allows users to toggle emf."
        }

        register("emf.top") {
            description = "Allows users to use /emf top."
        }

        register("emf.shop") {
            description = "Allows users to use /emf shop."
        }

        register("emf.use_rod") {
            description = "Allows users to use emf rods."
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    clean {
        doFirst {
            for (file in File(project.projectDir, "src/main/resources/addons").listFiles()!!) {
                file.delete()
            }
        }

    }

    shadowJar {
        minimize()

        exclude("META-INF/**")

        archiveFileName.set("even-more-fish-${project.version}.jar")
        archiveClassifier.set("shadow")

        relocate("de.tr7zw.changeme.nbtapi", "com.oheers.fish.utils.nbt")
        relocate("org.bstats", "com.oheers.fish.libs.bstats")
        relocate("com.github.Anon8281.universalScheduler", "com.oheers.fish.libs.universalScheduler")
        relocate("co.aikar.commands", "com.oheers.fish.libs.acf")
        relocate("co.aikar.locales", "com.oheers.fish.libs.locales")
    }

}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

