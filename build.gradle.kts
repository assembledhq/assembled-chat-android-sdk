buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    }
}

// Central Portal publishing is configured per-module
// See assembledchat/build.gradle.kts for publishing configuration

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}

