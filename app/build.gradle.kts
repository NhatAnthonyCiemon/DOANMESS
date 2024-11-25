plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.doanmess"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.doanmess"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    implementation("com.google.firebase:firebase-storage")
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation(platform("com.google.firebase:firebase-bom:33.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")

    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.firebase:firebase-messaging-directboot:20.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.arthenica:mobile-ffmpeg-full:4.4")
    implementation("com.google.android.exoplayer:exoplayer:2.15.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.play.services.location)
    implementation(libs.androidx.media3.ui) // Thêm Glide
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0") // Thêm Glide
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")


//
}