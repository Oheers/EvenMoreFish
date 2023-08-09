plugins {
    id("com.oheers.evenmorefish.java-conventions")
}



dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(project(":even-more-fish-plugin"))
}
