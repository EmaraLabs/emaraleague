plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(providers.gradleProperty("javaVersion").get().toInt()))
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:${providers.gradleProperty("slf4jVersion").get()}")
}
