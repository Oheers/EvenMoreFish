plugins {
    id("com.oheers.evenmorefish.java-conventions")
}


repositories {
    maven("https://repo.nexomc.com/snapshots/")
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.nexo)
    compileOnly(project(":even-more-fish-api"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
