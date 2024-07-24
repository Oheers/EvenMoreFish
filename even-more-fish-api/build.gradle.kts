plugins {
    id("com.oheers.evenmorefish.java-conventions")
}


dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.annotations)
    compileOnly(libs.commons.lang3)
    compileOnly(libs.universalscheduler)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
