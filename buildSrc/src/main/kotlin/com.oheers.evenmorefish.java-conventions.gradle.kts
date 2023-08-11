plugins {
    id("java")
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://jitpack.io")
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = groupId
            artifactId = artifactId
            version = version

            from(components["java"])
        }
    }
}

tasks {
    if (project.name.contains("addons")) {
        val addonName = defaultAddonName(project.name)
        jar {
            archiveFileName.set(addonName)
        }
        build {
            doLast {
                copy {
                    from(File(project.buildDir, "libs/${addonName}"))
                    into(File(rootProject.project(":even-more-fish-plugin").projectDir, "src/main/resources/addons"))
                }
            }
        }
    }
}

fun defaultAddonName(project: String): String {
    val jvmVersion = project.split("-")[4].uppercase()
    return "EMF-Addons-${jvmVersion}.jar"
}

