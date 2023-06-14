rootProject.name = "even-more-fish"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("spigot-api", "org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
            library("vault-api", "com.github.MilkBowl:VaultAPI:1.7.1")
            library("placeholder-api", "me.clip:placeholderapi:2.11.3")
            library("authlib", "com.mojang:authlib:1.5.21")
            library("bstats", "org.bstats:bstats-bukkit:3.0.2")
            version("worldguard", "7.0.8")
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
            library("griefprevention", "com.github.TechFortress:GriefPrevention:16.17.1")
            library("caffeine", "com.github.ben-manes.caffeine:caffeine:2.9.3")

            library("itemsadder-api", "com.github.LoneDev6:API-ItemsAdder:3.4.1-r4")
            library("nbt-api", "de.tr7zw:item-nbt-api:2.11.3")

            version("flyway", "9.19.4")
            library("flyway-core", "org.flywaydb","flyway-core").versionRef("flyway")
            library("flyway-mysql", "org.flywaydb","flyway-mysql").versionRef("flyway")

            library("friendlyid", "com.devskiller.friendly-id:friendly-id:1.1.0")
            library("hikaricp", "com.zaxxer:HikariCP:4.0.3")
            //            library("commands-paper","co.aikar:acf-paper:0.5.1-SNAPSHOT") maybe later

        }
    }
}