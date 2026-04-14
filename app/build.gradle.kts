import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kapt)
}

room {
    schemaDirectory("$projectDir/schemas")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.coddit.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.coddit.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "ALGOLIA_APP_ID", "\"${localProperties.getProperty("ALGOLIA_APP_ID") ?: ""}\"")
        buildConfigField("String", "ALGOLIA_SEARCH_KEY", "\"${localProperties.getProperty("ALGOLIA_SEARCH_KEY") ?: ""}\"")
        buildConfigField("String", "SAFE_BROWSING_API_KEY", "\"${localProperties.getProperty("SAFE_BROWSING_API_KEY") ?: ""}\"")
        buildConfigField("String", "GITHUB_CLIENT_ID", "\"${localProperties.getProperty("GITHUB_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "GITHUB_CLIENT_SECRET", "\"${localProperties.getProperty("GITHUB_CLIENT_SECRET") ?: ""}\"")
        // Cloudinary configuration
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localProperties.getProperty("CLOUDINARY_CLOUD_NAME") ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${localProperties.getProperty("CLOUDINARY_API_KEY") ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${localProperties.getProperty("CLOUDINARY_API_SECRET") ?: ""}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)

    // Networking & JSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    // Cloudinary
    implementation(libs.cloudinary.android)

    // Background Jobs
    implementation(libs.androidx.work.runtime.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}