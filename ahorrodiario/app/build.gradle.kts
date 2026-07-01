plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) // <-- AÑADE ESTA LÍNEA AQUÍ
    id("kotlin-kapt")
}

android {
    namespace = "com.apmobitech.ahorrodiario"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.apmobitech.ahorrodiario"
        minSdk = 24
        targetSdk = 37
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) // o JVM_17 si usas Java 17
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.core.ktx)
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
    // Base de datos Room
    val room_version = "2.5.2"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    // MPAndroid Chart (Con el bloqueo aislado)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") {
        exclude(group = "com.intellij", module = "annotations")
    }
    // Efecto de carga Shimmer (Faceboo)
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    // WorkManager para tareas en segundo plano
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    // Google AdMob para monetización
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    configurations.configureEach {
        // Si la configuración NO es de kapt, aplicamos el bloqueo
        if (!name.lowercase().contains("kapt")) {
            exclude(group = "com.intellij", module = "annotations")
        }
    }
}