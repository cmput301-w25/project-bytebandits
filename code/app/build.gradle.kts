plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

}

android {
    namespace = "com.github.bytebandits.bithub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.github.bytebandits.bithub"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SHA1_FINGERPRINT", "\"91:34:7C:16:A1:6A:51:07:B4:0D:1F:DB:66:43:9B:C5:3C:56:F7:49\"")

    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(platform(libs.firebase.bom))
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.gson)
    implementation(libs.firebase.firestore)
    implementation(libs.gms.play.services.location)
    implementation("com.google.maps.android:android-maps-utils:3.11.2")
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation("org.mockito:mockito-core:5.7.0")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.rules)
}

secrets {
    // To add your Maps API key to this project:
    // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"
}
