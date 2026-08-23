# EmaraLeague ProGuard Rules
# Preserve public API while obfuscating implementation

# ── Keep Public API ──────────────────────────────────────────────
-keep public class com.emaralabs.emaraleague.api.** { *; }

# ── Keep Main Plugin Class ───────────────────────────────────────
-keep public class com.emaralabs.emaraleague.EmaraLeaguePlugin { *; }

# ── Keep Bukkit Entry Points ─────────────────────────────────────
-keep class * extends org.bukkit.plugin.java.JavaPlugin { *; }
-keep class * implements org.bukkit.event.Listener { *; }
-keep class * implements org.bukkit.command.CommandExecutor { *; }
-keep class * implements org.bukkit.command.TabCompleter { *; }

# ── Keep Annotations ─────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ── Keep Enums ───────────────────────────────────────────────────
-keepclassmembers enum * { *; }

# ── Keep Line Numbers for Stack Traces ───────────────────────────
-keepattributes LineNumberTable
-keepattributes SourceFile

# ── Keep Configurate-accessed Classes (Reflection) ───────────────
-keep class com.emaralabs.emaraleague.core.config.** { *; }

# ── Keep Serializable ────────────────────────────────────────────
-keepclassmembers class * implements java.io.Serializable { *; }

# ── Keep Native Methods ──────────────────────────────────────────
-keepclassmembers class * { native <methods>; }

# ── Dontwarn for Missing Dependencies ────────────────────────────
# These are provided by Paper server at runtime
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
-dontwarn org.apache.commons.logging.**

# ── Optimization ─────────────────────────────────────────────────
-optimizationpasses 3
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-allowaccessmodification

# ── Keep Parameter Names (for debugging) ─────────────────────────
-keepparameternames
