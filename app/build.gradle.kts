plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
}

android {
    namespace = "com.ibrahim.nano_health_task"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.ibrahim.nano_health_task"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Lifecycle viewmodel compose (for viewModel() in Compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Image loading (Coil) for Compose
    implementation("io.coil-kt:coil-compose:2.4.0")

    // ExoPlayer for video playback and caching
    implementation("com.google.android.exoplayer:exoplayer:2.18.6")
    implementation("com.google.android.exoplayer:extension-okhttp:2.18.6")


    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // Coroutines test for unit tests
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Accompanist swipe refresh for pull-to-refresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.30.1")

    // Material icons (extended) for play/pause icons
    implementation("androidx.compose.material:material-icons-extended:1.5.0")

    // Hilt: use version catalog aliases
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.ext.compiler)
    // Hilt navigation for Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Retrofit + OkHttp for network calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}