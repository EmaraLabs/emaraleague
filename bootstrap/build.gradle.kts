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
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.papermc.paper:paper-api:${providers.gradleProperty("paperVersion").get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
}

tasks.jar {
    archiveFileName.set("EmaraLeague-${project.version}.jar")
    manifest {
        attributes(
            "Main-Class" to "com.emaralabs.emaraleague.EmaraLeaguePlugin",
            "Implementation-Title" to "EmaraLeague",
            "Implementation-Version" to project.version
        )
    }
}
