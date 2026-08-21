plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(providers.gradleProperty("javaVersion").get().toInt()))
    }
}

dependencies {
    implementation("com.zaxxer:HikariCP:${providers.gradleProperty("hikariCpVersion").get()}")
    implementation("org.jetbrains.exposed:exposed-core:${providers.gradleProperty("exposedVersion").get()}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${providers.gradleProperty("exposedVersion").get()}")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.2")
}
