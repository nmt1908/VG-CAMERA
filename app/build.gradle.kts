plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.vgcamera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vgcamera"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
        versionName = "2.1"

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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.icons.extended)
    implementation(libs.lottie.compose)

    implementation ("com.otaliastudios:zoomlayout:1.9.0")
    implementation ("androidx.media3:media3-exoplayer:1.3.1")
    implementation ("androidx.media3:media3-ui:1.3.1")
    implementation ("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.preference)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("androidx.camera:camera-core:1.5.0-beta01")
    implementation ("androidx.camera:camera-camera2:1.5.0-beta01")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")
    
    // ML Kit Face Mesh Detection (Standalone version)
    implementation ("com.google.mlkit:face-detection:16.1.6")
    implementation ("com.google.mlkit:face-mesh-detection:16.0.0-beta3")
    implementation ("com.google.mlkit:vision-common:17.3.0")
    
    //OKHTTP3
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}