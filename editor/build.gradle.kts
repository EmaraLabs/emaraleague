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
    implementation(project(":infrastructure:database"))
    implementation(project(":infrastructure:cache"))
    implementation(project(":infrastructure:config"))
    implementation(project(":infrastructure:logging"))
    implementation(project(":infrastructure:security"))
}
