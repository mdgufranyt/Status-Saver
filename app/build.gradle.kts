plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.mg.statussaver"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mg.statussaver"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.activity:activity-compose:1.10.1")

    // Compose
    implementation("androidx.compose.ui:ui:1.8.3")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.8.3")
    implementation("androidx.compose.foundation:foundation:1.8.3")
    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("androidx.documentfile:documentfile:1.1.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.8.3")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.2")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha17")
    implementation("androidx.compose.material:material-icons-extended:1.6.3")


    // Hilt (DI)
    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp("com.google.dagger:hilt-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0") // Add video support for thumbnails

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Testing (optional)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.8.3")

    // for ViewerScreen
    implementation("com.google.accompanist:accompanist-pager:0.34.0")
    implementation("androidx.media3:media3-exoplayer:1.3.0")
    implementation("androidx.media3:media3-ui:1.3.0")

     // Google Admob
    implementation("com.google.android.gms:play-services-ads:24.4.0")
}
