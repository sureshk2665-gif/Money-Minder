plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.moneyminder.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.moneyminder.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
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
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/*.SF"
            excludes += "/META-INF/*.DSA"
            excludes += "/META-INF/*.RSA"
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Apache POI for Excel export
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5") {
        exclude(group = "org.apache.xmlgraphics", module = "batik-all")
        exclude(group = "org.apache.xmlgraphics", module = "batik-bridge")
        exclude(group = "de.rototor.pdfbox", module = "graphics2d")
        exclude(group = "org.apache.xmlgraphics", module = "batik-anim")
        exclude(group = "org.apache.xmlgraphics", module = "batik-codec")
        exclude(group = "org.apache.xmlgraphics", module = "batik-css")
        exclude(group = "org.apache.xmlgraphics", module = "batik-dom")
        exclude(group = "org.apache.xmlgraphics", module = "batik-ext")
        exclude(group = "org.apache.xmlgraphics", module = "batik-gvt")
        exclude(group = "org.apache.xmlgraphics", module = "batik-parser")
        exclude(group = "org.apache.xmlgraphics", module = "batik-script")
        exclude(group = "org.apache.xmlgraphics", module = "batik-svg-dom")
        exclude(group = "org.apache.xmlgraphics", module = "batik-svggen")
        exclude(group = "org.apache.xmlgraphics", module = "batik-transcoder")
        exclude(group = "org.apache.xmlgraphics", module = "batik-util")
        exclude(group = "org.apache.xmlgraphics", module = "batik-xml")
    }

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
