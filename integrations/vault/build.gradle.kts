plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(providers.gradleProperty("javaVersion").get().toInt()))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("paperVersion").get()}")
    implementation(project(":api"))
    implementation(project(":core"))
    compileOnly("net.milkbowl.vault:VaultAPI:${providers.gradleProperty("vaultVersion").get()}")
}
