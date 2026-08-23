# ProGuard Obfuscation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Setup ProGuard obfuscation for production release — protect code from decompilation, reduce JAR size, maintain functionality.

**Architecture:** ProGuard Gradle plugin processes Shadow JAR after build. Keep public API, obfuscate implementation. Preserve annotations, enums, and Bukkit entry points.

**Tech Stack:** ProGuard 7.x, Gradle, Shadow JAR.

---

## Global Constraints

- Java 21 bytecode support
- Keep public API (EmaraLeagueAPI, EmaraAddon)
- Keep Bukkit entry points (JavaPlugin, Listener, CommandExecutor)
- Keep annotations (@EventHandler, @Override)
- Keep enums (used in switch statements)
- Keep reflection-accessed classes (Configurate, SQLite)
- Preserve debugging info (line numbers for stack traces)

---

## Task 1: ProGuard Configuration

**Files:**
- Create: `bootstrap/proguard-rules.pro`
- Modify: `bootstrap/build.gradle.kts`

**ProGuard rules:**
- Keep: `com.emaralabs.emaraleague.api.**` (public API)
- Keep: `com.emaralabs.emaraleague.EmaraLeaguePlugin` (main class)
- Keep: Bukkit/Paper classes (provided by server)
- Keep: Annotations, enums, inner classes
- Obfuscate: Everything else (core, modules, infrastructure)

```proguard
# Keep public API
-keep public class com.emaralabs.emaraleague.api.** { *; }

# Keep main plugin class
-keep public class com.emaralabs.emaraleague.EmaraLeaguePlugin { *; }

# Keep Bukkit entry points
-keep class * extends org.bukkit.plugin.java.JavaPlugin { *; }
-keep class * implements org.bukkit.event.Listener { *; }
-keep class * implements org.bukkit.command.CommandExecutor { *; }
-keep class * implements org.bukkit.command.TabCompleter { *; }

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep enums
-keepclassmembers enum * { *; }

# Keep LineNumberTable for stack traces
-keepattributes LineNumberTable
-keepattributes SourceFile

# Keep Configurate-accessed classes (reflection)
-keep class com.emaralabs.emaraleague.core.config.** { *; }

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable { *; }

# Keep native methods
-keepclassmembers class * { native <methods>; }

# Dontwarn for missing dependencies (Paper provides at runtime)
-dontwarn org.bukkit.**
-dontwarn io.papermc.**
-dontwarn net.kyori.**
-dontwarn org.spongepowered.**
-dontwarn com.zaxxer.**
-dontwarn org.sqlite.**
-dontwarn org.mariadb.**
-dontwarn org.jetbrains.**
-dontwarn com.github.benmanes.**
-dontwarn org.slf4j.**
```

- [ ] **Step 1: Write ProGuard rules file**

- [ ] **Step 2: Add ProGuard plugin to build.gradle.kts**

```kotlin
plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.guardsquare.proguard") version "7.6.0"
}

tasks {
    proguard {
        configurations("proguard-rules.pro")
        injars(tasks.shadowJar)
        outjars(layout.buildDirectory.file("libs/EmaraLeague-${project.version}-obfuscated.jar"))
        libraryjars("${System.getProperty("java.home")}/jmods/java.base.jmod")
        libraryjars("${System.getProperty("java.home")}/jmods/java.logging.jmod")
        libraryjars("${System.getProperty("java.home")}/jmods/java.sql.jmod")
        libraryjars("${System.getProperty("java.home")}/jmods/java.desktop.jmod")
        libraryjars("${System.getProperty("java.home")}/jmods/java.naming.jmod")
        libraryjars("${System.getProperty("java.home")}/jmods/java.management.jmod")
        libraryjars("${System.getProperty("java.home")}/jmods/jdk.unsupported.jmod")
    }
}
```

- [ ] **Step 3: Build and verify**

- [ ] **Step 4: Test obfuscated JAR**

- [ ] **Step 5: Commit**

---

## Task 2: Verify Obfuscation

**Verification steps:**
- JAR size reduced (28MB → ~20MB)
- Classes renamed (a.class, b.class, etc.)
- Public API preserved (EmaraLeagueAPI, EmaraAddon)
- Plugin loads and functions correctly

- [ ] **Step 1: Check JAR size**
- [ ] **Step 2: Check class names**
- [ ] **Step 3: Check API classes preserved**
- [ ] **Step 4: Test in-game**

---

## Self-Review

1. **Spec coverage:** ProGuard obfuscation ✅. API preservation ✅. Functionality maintained ✅.
2. **Placeholder scan:** No TBD — every step has actual config.
3. **Type consistency:** ProGuard rules match Java 21 + Bukkit requirements.
4. **Scope check:** 2 tasks. Task 1 is configuration, Task 2 is verification.

---

## Execution Handoff

Plan complete. Proceeding with inline execution.
