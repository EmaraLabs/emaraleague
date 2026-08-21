plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(providers.gradleProperty("javaVersion").get().toInt()))
    }
}

dependencies {
    implementation("org.spongepowered:configurate-yaml:${providers.gradleProperty("configurateVersion").get()}")
    implementation("org.spongepowered:configurate-hocon:${providers.gradleProperty("configurateVersion").get()}")
    implementation(project(":api"))
}
