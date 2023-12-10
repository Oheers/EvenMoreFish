rootProject.name = "even-more-fish"

include(":even-more-fish-api")
include(":even-more-fish-addons-j8")
include(":even-more-fish-addons-j17")
include(":even-more-fish-plugin")


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("spigot-api", "org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
            library("vault-api", "com.github.MilkBowl:VaultAPI:1.7.1")
            library("placeholder-api", "me.clip:placeholderapi:2.11.3")
            library("authlib", "com.mojang:authlib:1.5.21")
            library("bstats", "org.bstats:bstats-bukkit:3.0.2")
            version("worldguard", "7.0.5")//We must use 7.0.5 until we upgrade to java 17
            library("worldguard-core", "com.sk89q.worldguard","worldguard-core").versionRef("worldguard")
            library("worldguard-bukkit", "com.sk89q.worldguard","worldguard-bukkit").versionRef("worldguard")
            version("worldedit", "7.2.15")
            library("worldedit-core", "com.sk89q.worldedit","worldedit-core").versionRef("worldedit")
            library("worldedit-bukkit", "com.sk89q.worldedit","worldedit-bukkit").versionRef("worldedit")
            version("redprotect", "7.7.3")
            library("redprotect-core", "br.net.fabiozumbi12.RedProtect","RedProtect-Core").versionRef("redprotect")
            library("redprotect-spigot", "br.net.fabiozumbi12.RedProtect","RedProtect-Spigot-1.13").versionRef("redprotect")
            library("mcmmo", "com.gmail.nossr50.mcMMO:mcMMO:2.1.196")
            library("aurelium-skills", "com.github.Archy-X:AureliumSkills:Beta1.2.8")
            library("headdatabase-api", "com.arcaniax:HeadDatabase-API:1.3.1")
            library("griefprevention", "com.github.TechFortress:GriefPrevention:16.17.1")//We must use 16.17.1 until we upgrade to java 17
            library("caffeine", "com.github.ben-manes.caffeine:caffeine:2.9.3")//We must use 2.9.3 until we upgrade to java 17

            library("itemsadder-api", "com.github.LoneDev6:API-ItemsAdder:3.5.0b")
            library("nbt-api", "de.tr7zw:item-nbt-api:2.12.2")
            library("denizens-api", "com.denizenscript:denizen:1.2.5-SNAPSHOT") // We must use 1.2.6 until we upgrade to java 17
            library("oraxen", "com.github.oraxen:oraxen:1.159.0")

            library("ecoitems-api", "com.willfp:EcoItems:5.6.1")
            library("ecoitems-libreforge", "com.willfp:libreforge:4.21.1")
            library("ecoitems-eco", "com.willfp:eco:6.65.1")

            library("commons-lang3", "org.apache.commons:commons-lang3:3.13.0")
            library("commons-codec", "commons-codec:commons-codec:1.16.0")
            library("annotations", "org.jetbrains:annotations:24.0.1")

            version("flyway", "9.19.4")
            library("flyway-core", "org.flywaydb","flyway-core").versionRef("flyway")
            library("flyway-mysql", "org.flywaydb","flyway-mysql").versionRef("flyway")

            library("friendlyid", "com.devskiller.friendly-id:friendly-id:1.1.0")
            library("hikaricp", "com.zaxxer:HikariCP:4.0.3") //We must use 4.0.3 until we upgrade to java 17
            library("json-simple", "com.googlecode.json-simple:json-simple:1.1.1")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}