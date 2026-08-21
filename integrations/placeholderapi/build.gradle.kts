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
    compileOnly("com.github.PlaceholderAPI:PlaceholderAPI:${providers.gradleProperty("placeholderApiVersion").get()}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.papermc.paper:paper-api:${providers.gradleProperty("paperVersion").get()}")
    testImplementation("com.github.PlaceholderAPI:PlaceholderAPI:${providers.gradleProperty("placeholderApiVersion").get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
}
