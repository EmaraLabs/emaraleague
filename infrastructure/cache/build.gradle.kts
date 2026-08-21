plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(providers.gradleProperty("javaVersion").get().toInt()))
    }
}

dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine:${providers.gradleProperty("caffeineVersion").get()}")
    implementation(project(":api"))
}
