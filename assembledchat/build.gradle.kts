import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

android {
    namespace = "com.assembled.chat"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    
    // For library modules, use lint.targetSdk instead of defaultConfig.targetSdk
    lint {
        targetSdk = 34
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // WebView
    implementation("androidx.webkit:webkit:1.9.0")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configure Maven publishing with Vanniktech plugin for Central Portal
mavenPublishing {
    // Publication coordinates
    coordinates(
        groupId = "io.github.assembledhq",
        artifactId = "assembledchat",
        version = "1.0.3"
    )
    
    // POM configuration
    pom {
        name.set("Assembled Chat Android SDK")
        description.set("Android SDK for integrating Assembled Chat into your application")
        url.set("https://github.com/assembledhq/assembled-chat-android-sdk")
        
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        
        developers {
            developer {
                id.set("assembled")
                name.set("Assembled")
                organization.set("Assembled")
                organizationUrl.set("https://www.assembled.com")
            }
        }
        
        scm {
            connection.set("scm:git:git://github.com/assembledhq/assembled-chat-android-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/assembledhq/assembled-chat-android-sdk.git")
            url.set("https://github.com/assembledhq/assembled-chat-android-sdk")
        }
    }
    
    // Publish to Central Portal (new system)
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    
    // Configure signing
    signAllPublications()
}

