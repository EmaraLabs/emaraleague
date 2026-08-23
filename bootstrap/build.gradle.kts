buildscript {
    dependencies {
        // Shadow 8.1.1 ships ASM 9.5 which cannot parse Java 21 (major 65) bytecode.
        // Override with ASM 9.7 which supports Java 21+.
        classpath("org.ow2.asm:asm:9.7")
        classpath("org.ow2.asm:asm-commons:9.7")
    }
}

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    implementation(project(":editor"))
    implementation(project(":modules:duels"))
    implementation(project(":modules:spleef"))
    implementation(project(":modules:sumo"))
    implementation(project(":modules:tnt-run"))
    implementation(project(":integrations:vault"))
    implementation(project(":integrations:placeholderapi"))
    implementation(project(":integrations:luckperms"))
    implementation(project(":integrations:playerpoints"))
    implementation(project(":infrastructure:database"))
    implementation(project(":infrastructure:cache"))
    implementation(project(":infrastructure:config"))
    implementation(project(":infrastructure:logging"))
    implementation(project(":infrastructure:security"))
    implementation("org.slf4j:slf4j-api:${providers.gradleProperty("slf4jVersion").get()}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.papermc.paper:paper-api:${providers.gradleProperty("paperVersion").get()}")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveFileName.set("EmaraLeague-${project.version}.jar")

    relocate("com.github.benmanes.caffeine", "com.emaralabs.emaraleague.libs.caffeine")
    relocate("com.zaxxer.hikari", "com.emaralabs.emaraleague.libs.hikari")
    relocate("org.jetbrains.exposed", "com.emaralabs.emaraleague.libs.exposed")
    relocate("org.sqlite", "com.emaralabs.emaraleague.libs.sqlite")
    relocate("org.mariadb.jdbc", "com.emaralabs.emaraleague.libs.mariadb")
    relocate("org.spongepowered.configurate", "com.emaralabs.emaraleague.libs.configurate")
    relocate("io.leangen.geantyref", "com.emaralabs.emaraleague.libs.geantyref")
    relocate("org.slf4j", "com.emaralabs.emaraleague.libs.slf4j")
    relocate("org.apache.commons.logging", "com.emaralabs.emaraleague.libs.commonslogging")

    mergeServiceFiles()
}

// ProGuard obfuscation for production release
// Run with: ./gradlew :bootstrap:proguard
// Note: ProGuard plugin requires manual setup — use CLI tool or CI pipeline
// For now, obfuscation is optional for v1.0 release (debug build acceptable)
tasks.register("obfuscate") {
    description = "Obfuscates the shadow JAR using ProGuard"
    group = "build"
    dependsOn(tasks.shadowJar)

    doLast {
        val inputJar = layout.buildDirectory.file("libs/EmaraLeague-${project.version}.jar")
        val outputJar = layout.buildDirectory.file("libs/EmaraLeague-${project.version}-obfuscated.jar")
        val proguardRules = file("proguard-rules.pro")

        if (!proguardRules.exists()) {
            logger.warn("ProGuard rules not found, skipping obfuscation")
            return@doLast
        }

        logger.lifecycle("Obfuscation requires ProGuard CLI tool or CI pipeline setup")
        logger.lifecycle("Input: ${inputJar.get()}")
        logger.lifecycle("Output: ${outputJar.get()}")
        logger.lifecycle("Rules: $proguardRules")
    }
}
