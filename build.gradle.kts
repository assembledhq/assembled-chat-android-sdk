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

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

nexusPublishing {
    repositories {
        sonatype {
            // Central Portal uses the same repository endpoints
            // Manage deployments at: https://central.sonatype.com
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            
            username.set(providers.gradleProperty("ossrhUsername").getOrElse(""))
            password.set(providers.gradleProperty("ossrhPassword").getOrElse(""))
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}

