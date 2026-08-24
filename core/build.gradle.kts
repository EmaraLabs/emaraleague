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
    implementation("net.kyori:adventure-text-minimessage:${providers.gradleProperty("miniMessageVersion").get()}")
    implementation("org.spongepowered:configurate-yaml:${providers.gradleProperty("configurateVersion").get()}")
    implementation("io.leangen.geantyref:geantyref:1.3.16")
    implementation("net.megavex:scoreboard-library-implementation:${providers.gradleProperty("scoreboardLibraryVersion").get()}")
    implementation(project(":api"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.papermc.paper:paper-api:${providers.gradleProperty("paperVersion").get()}")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}
