plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.bookreview"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.bookreview"
        minSdk = 25
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
    buildFeatures {
        compose = true
    }
}

// Room genera el esquema de la base de datos en /schemas en cada compilación.
// Conviene versionarlo en git: es lo que se usa para escribir migraciones
// cuando la tabla "resenas" cambie de forma en el futuro.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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
    implementation(libs.androidx.compose.material.icons.core)
    // Semana 4: PhotoCamera no está en el set "core" de íconos, solo en
    // el extendido (miles de íconos, por eso Compose lo separó en otro
    // artefacto). Usa la misma versión que material3 vía el BOM.
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation Compose: navegación entre Búsqueda / Detalle / Mis Reseñas / Ajustes
    implementation(libs.androidx.navigation.compose)

    // Room: persistencia local de reseñas
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // DataStore: preferencia simple de modo oscuro
    implementation(libs.androidx.datastore.preferences)

    // Retrofit: se deja configurado hoy, se conecta a Open Library la próxima semana
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Corrutinas (Repository y ViewModel las usan para trabajo asíncrono)
    implementation(libs.kotlinx.coroutines.android)

    // Coil: carga de imágenes (portadas de libros) en Compose
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // ViewModel + integración con Compose y Navigation (SavedStateHandle)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
