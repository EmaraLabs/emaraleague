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
}

tasks.shadowJar {
    archiveFileName.set("EmaraLeague-${project.version}.jar")
    relocate("com.github.benmanes.caffeine", "com.emaralabs.emaraleague.libs.caffeine")
    relocate("org.spongepowered.configurate", "com.emaralabs.emaraleague.libs.configurate")
    relocate("com.zaxxer.hikari", "com.emaralabs.emaraleague.libs.hikari")
    relocate("org.jetbrains.exposed", "com.emaralabs.emaraleague.libs.exposed")
}
