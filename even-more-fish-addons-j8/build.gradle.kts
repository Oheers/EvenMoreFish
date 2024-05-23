plugins {
    id("com.oheers.evenmorefish.java-conventions")
}

repositories {}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.commons.lang3)
    compileOnly(libs.itemsadder.api)
    compileOnly(libs.headdatabase.api)

    compileOnly(project(":even-more-fish-api"))

    testCompileOnly(project(":even-more-fish-api"))
    testCompileOnly(libs.spigot.api)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
