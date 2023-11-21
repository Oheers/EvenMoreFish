plugins {
    id("com.oheers.evenmorefish.java-conventions")
}


dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.annotations)
    compileOnly(libs.commons.lang3)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
