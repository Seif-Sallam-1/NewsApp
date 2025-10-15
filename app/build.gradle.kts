plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.newapp"
    compileSdk = 36

    buildFeatures{
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.newapp"
        minSdk = 29
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit for networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Note: 3.0.0 is a beta, 2.9.0 is the last stable
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0") // Updated to a more recent stable version

    // Swipe to refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // --- CORRECTED FIREBASE DEPENDENCIES ---
    // Import the Firebase Bill of Materials (BOM) - This manages versions for you.
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Updated to a recent stable BOM

    // Add the specific Firebase products you want to use.
    // The BOM ensures their versions are compatible.
    implementation("com.google.firebase:firebase-auth-ktx") // Use the -ktx version for Kotlin extensions
    implementation("com.google.firebase:firebase-firestore-ktx") // Use the -ktx version

    // REMOVED: You had libs.firebase.database.ktx but don't seem to need it. Add it back if you do.
    // REMOVED: The non-ktx versions are not needed when you use -ktx.

    // --- KOTLIN COROUTINES ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // --- LIFECYCLE SCOPE ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2") // Updated to a more recent stable version
}
