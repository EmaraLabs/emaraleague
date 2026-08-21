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
    implementation("net.kyori:adventure-api:${providers.gradleProperty("adventureVersion").get()}")
    implementation("net.kyori:adventure-text-minimessage:${providers.gradleProperty("miniMessageVersion").get()}")
    implementation("com.github.ben-manes.caffeine:caffeine:${providers.gradleProperty("caffeineVersion").get()}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.papermc.paper:paper-api:${providers.gradleProperty("paperVersion").get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}
