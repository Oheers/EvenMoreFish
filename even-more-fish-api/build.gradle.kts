plugins {
    id("com.oheers.evenmorefish.java-conventions")
}

version = 2.0

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
