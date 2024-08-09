rootProject.name = "even-more-fish"

include(":even-more-fish-api")
include(":even-more-fish-addons-j17")
include(":even-more-fish-plugin")


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("spigot-api", "org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
            library("vault-api", "com.github.MilkBowl:VaultAPI:1.7.1")
            library("placeholder-api", "me.clip:placeholderapi:2.11.6")
            library("bstats", "org.bstats:bstats-bukkit:3.0.2")

            version("worldguard", "7.0.5")//We must use 7.0.5 until we upgrade to java 17
            library("worldguard-core", "com.sk89q.worldguard","worldguard-core").versionRef("worldguard")
            library("worldguard-bukkit", "com.sk89q.worldguard","worldguard-bukkit").versionRef("worldguard")
            bundle("worldguard", listOf("worldguard-core", "worldguard-bukkit"))

            version("worldedit", "7.2.15")
            library("worldedit-core", "com.sk89q.worldedit","worldedit-core").versionRef("worldedit")
            library("worldedit-bukkit", "com.sk89q.worldedit","worldedit-bukkit").versionRef("worldedit")
            bundle("worldedit", listOf("worldedit-core", "worldedit-bukkit"))

            version("redprotect", "7.7.3")
            library("redprotect-core", "br.net.fabiozumbi12.RedProtect","RedProtect-Core").versionRef("redprotect")
            library("redprotect-spigot", "br.net.fabiozumbi12.RedProtect","RedProtect-Spigot-1.13").versionRef("redprotect")
            bundle("redprotect", listOf("redprotect-core", "redprotect-spigot"))

            library("mcmmo", "com.gmail.nossr50.mcMMO:mcMMO:2.1.196")
            library("aurelium-skills", "com.github.Archy-X:AureliumSkills:Beta1.2.8")
            library("aura-skills", "dev.aurelium:auraskills-api-bukkit:2.0.7")
            library("headdatabase-api", "com.arcaniax:HeadDatabase-API:1.3.1")
            library("griefprevention", "com.github.TechFortress:GriefPrevention:16.17.1")


            library("itemsadder-api", "com.github.LoneDev6:API-ItemsAdder:3.6.1")
            library("nbt-api", "de.tr7zw:item-nbt-api:2.13.1")
            library("denizen-api", "com.denizenscript:denizen:1.3.1-SNAPSHOT")
            library("oraxen", "io.th0rgal:oraxen:1.173.0") // We must use 1.173.0 as later versions require Java 21

            library("ecoitems-api", "com.willfp:EcoItems:5.6.1")
            library("ecoitems-libreforge", "com.willfp:libreforge:4.21.1")
            library("ecoitems-eco", "com.willfp:eco:6.65.1")
            bundle("ecoitems", listOf("ecoitems-api", "ecoitems-libreforge", "ecoitems-eco"))

            library("commons-lang3", "org.apache.commons:commons-lang3:3.14.0")
            library("commons-codec", "commons-codec:commons-codec:1.17.0")
            library("caffeine", "com.github.ben-manes.caffeine:caffeine:3.1.8")
            library("annotations", "org.jetbrains:annotations:24.1.0")

            version("flyway", "10.17.0")
            library("flyway-core", "org.flywaydb","flyway-core").versionRef("flyway")
            library("flyway-mysql", "org.flywaydb","flyway-mysql").versionRef("flyway")
            bundle("flyway", listOf("flyway-core", "flyway-mysql"))

            library("friendlyid", "com.devskiller.friendly-id:friendly-id:1.1.0")
            library("hikaricp", "com.zaxxer:HikariCP:5.1.0")
            library("json-simple", "com.googlecode.json-simple:json-simple:1.1.1")

            library("universalscheduler", "com.github.Anon8281:UniversalScheduler:0.1.6")
            library("playerpoints", "org.black_ixx:playerpoints:3.2.7")
            library("cmi-api", "CMI-API:CMI-API:9.7.0.1")
            library("essx-api", "net.essentialsx:EssentialsX:2.20.1")

            library("acf", "co.aikar:acf-paper:0.5.1-SNAPSHOT")
            library("inventorygui", "de.themoep:inventorygui:1.6.3-SNAPSHOT")

            plugin("shadow", "com.gradleup.shadow").version("8.3.0")
            plugin("bukkit-yml", "net.minecrell.plugin-yml.bukkit").version("0.6.0")

            version("adventure", "4.17.0")
            library("adventure-api", "net.kyori","adventure-api").versionRef("adventure")
            library("adventure-minimessage", "net.kyori","adventure-text-minimessage").versionRef("adventure")
            library("adventure-legacy", "net.kyori","adventure-text-serializer-legacy").versionRef("adventure")
            bundle("adventure", listOf("adventure-api", "adventure-minimessage", "adventure-legacy"))

            library("boostedyaml", "dev.dejvokep:boosted-yaml:1.3.7")

            plugin("grgit", "org.ajoberstar.grgit").version("5.2.2")

        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}