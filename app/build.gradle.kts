plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.subu.weddingcraft"
    compileSdk = 36

    buildFeatures{
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.subu.weddingcraft"
        minSdk = 26
        targetSdk = 36
        versionCode = 28
        versionName = "2.8"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation (libs.room.runtime)
    annotationProcessor (libs.room.compiler)
    implementation (libs.gson)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.analytics)
    implementation (libs.firebase.auth)
    implementation (libs.firebase.firestore)
    implementation (libs.firebase.messaging)
    implementation(libs.firebase.storage.ktx)
    implementation (libs.firebase.functions)
    implementation (libs.firebase.appcheck)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug) // remove later in production
    implementation (libs.google.firebase.appcheck.debug)
    implementation (libs.integrity)
}