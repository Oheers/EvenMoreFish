import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.bukkit.yml)
    alias(libs.plugins.shadow)
    alias(libs.plugins.grgit)
}

group = "com.oheers.evenmorefish"
version = "2.0.0"

description = "A fishing extension bringing an exciting new experience to fishing."

repositories {
    mavenCentral()
    // Adventure Snapshots
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
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
    maven("https://repo.minebench.de/")
    maven("https://repo.firedev.uk/repository/maven-public/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    api(project(":even-more-fish-api"))

    compileOnly(libs.spigot.api)
    compileOnly(libs.vault.api)
    compileOnly(libs.placeholder.api)

    compileOnly(libs.worldguard.core) {
        exclude("com.sk89q.worldedit", "worldedit-core")
    }
    compileOnly(libs.worldguard.bukkit) {
        exclude("org.spigotmc", "spigot-api")
    }
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
    compileOnly(libs.aura.skills)
    compileOnly(libs.aurelium.skills) {
        exclude(libs.acf.get().group, libs.acf.get().name)
    }

    compileOnly(libs.griefprevention)
    compileOnly(libs.mcmmo)
    compileOnly(libs.headdatabase.api)
    compileOnly(libs.playerpoints)

    implementation(libs.nbt.api)
    implementation(libs.bstats)
    implementation(libs.universalscheduler)
    implementation(libs.acf)
    implementation(libs.inventorygui)
    implementation(libs.bundles.adventure)
    implementation(libs.vanishchecker)
    implementation(libs.boostedyaml)

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

    depend = listOf()
    softDepend = listOf(
        "Vault",
        "PlayerPoints",
        "WorldGuard",
        "PlaceholderAPI",
        "RedProtect",
        "mcMMO",
        "AureliumSkills",
        "AuraSkills",
        "ItemsAdder",
        "Denizen",
        "EcoItems",
        "Oraxen",
        "HeadDatabase",
        "GriefPrevention"
    )
    loadBefore = listOf("AntiAC")
    apiVersion = "1.18"

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
                "emf.sellall",
                "emf.help",
                "emf.next",
                "emf.applybaits"
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

        register("emf.next") {
            description = "Allows users to see when the next competition will be."
        }

        register("emf.help") {
            description = "Allows users to see the help messages."
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

    }
}

tasks {
    build {
        dependsOn(shadowJar)

        doLast {
            val file = project.layout.buildDirectory.file("libs/even-more-fish-plugin-${version}.jar").get()
            file.asFile.delete()
        }
    }

    clean {
        doFirst {
            val jitpack: Boolean = System.getenv("JITPACK").toBoolean()
            if (jitpack)
                return@doFirst

            for (file in File(project.projectDir, "src/main/resources/addons").listFiles()!!) {
                file.delete()
            }

        }

    }

    shadowJar {
        val buildNumberOrDate = getBuildNumberOrDate()
        manifest {
            val buildNumber: String? by project

            attributes["Specification-Title"] = "EvenMoreFish"
            attributes["Specification-Version"] = project.version
            attributes["Implementation-Title"] = grgit.branch.current().name
            attributes["Implementation-Version"] = buildNumberOrDate
        }

        minimize {
            exclude(dependency("net.kyori:.*"))
        }

        exclude("META-INF/**")

        archiveFileName.set("even-more-fish-${project.version}-${buildNumberOrDate}.jar")
        archiveClassifier.set("shadow")

        relocate("de.tr7zw.changeme.nbtapi", "com.oheers.fish.utils.nbtapi")
        relocate("org.bstats", "com.oheers.fish.libs.bstats")
        relocate("com.github.Anon8281.universalScheduler", "com.oheers.fish.libs.universalScheduler")
        relocate("co.aikar.commands", "com.oheers.fish.libs.acf")
        relocate("co.aikar.locales", "com.oheers.fish.libs.locales")
        relocate("de.themoep.inventorygui", "com.oheers.fish.libs.inventorygui")
        relocate("net.kyori.adventure", "com.oheers.fish.libs.adventure")
        relocate("uk.firedev.vanishchecker", "com.oheers.fish.libs.vanishchecker")
        relocate("dev.dejvokep.boostedyaml", "com.oheers.fish.libs.boostedyaml")

    }

    compileJava {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.encoding = "UTF-8"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

fun getBuildNumberOrDate(): String? {
    val currentBranch = grgit.branch.current().name
    if (currentBranch.equals("head", ignoreCase = true) || currentBranch.equals("master", ignoreCase = true)) {
        val buildNumber: String? by project
        if (buildNumber == null)
            return "RELEASE"

        return buildNumber
    }

    val time = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())
        .format(Instant.now())

    return time
}

