plugins {
    id("com.oheers.evenmorefish.java-conventions")
}

version = 1.7

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":even-more-fish-api"))

    compileOnly(libs.paper.api)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
