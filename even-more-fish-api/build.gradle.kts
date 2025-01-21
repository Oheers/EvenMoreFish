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
    repositories {
        // We can add CodeMC here when we're ready
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