plugins {
    id("com.oheers.evenmorefish.java-conventions")
}

group = "com.oheers.evenmorefish"
version = "2.0.0-SNAPSHOT"

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.annotations)
    compileOnly(libs.commons.lang3)
    compileOnly(libs.universalscheduler)
    compileOnly(libs.boostedyaml)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

publishing {
    repositories { // Copied directly from CodeMC's docs
        maven {
            url = uri("https://repo.codemc.io/repository/Oheers/")

            val mavenUsername = System.getenv("JENKINS_USERNAME")
            val mavenPassword = System.getenv("JENKINS_PASSWORD")

            if (mavenUsername != null && mavenPassword != null) {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }
    publications {
        create<MavenPublication>("api") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}