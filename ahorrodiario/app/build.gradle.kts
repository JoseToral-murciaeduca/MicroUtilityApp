plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.apmobitech.ahorrodiario"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.apmobitech.ahorrodiario"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.play.services.maps3d)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // Retrofit para las llamadas a la API de la luz y la gasolina
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Conversor de JSON a objetos Kotlin
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Librería para cargar imágenes desde URLs de internet
    implementation("io.coil-kt:coil:2.6.0")
}