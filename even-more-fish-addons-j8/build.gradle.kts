plugins {
    id("com.oheers.evenmorefish.java-conventions")
}

repositories {
    maven("https://maven.citizensnpcs.co/repo")
}
dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.commons.lang3)
    compileOnly(libs.itemsadder.api)
    compileOnly(libs.denizen.api)
    compileOnly(libs.headdatabase.api)

    compileOnly(project(":even-more-fish-api"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
