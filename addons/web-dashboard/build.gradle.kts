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
    implementation(project(":core"))
    implementation(project(":api"))
    implementation("net.kyori:adventure-api:${providers.gradleProperty("adventureVersion").get()}")
}
