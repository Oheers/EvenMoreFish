plugins {
    id("com.oheers.evenmorefish.java-conventions")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly(project(":even-more-fish-api"))

    compileOnly(libs.paper.api)
    compileOnly(libs.placeholder.api)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
