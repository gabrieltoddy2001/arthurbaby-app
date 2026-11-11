import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Endereço da API para celular físico: defina "api.url" no local.properties
// (ex.: api.url=http://192.168.1.110:8080/). No emulador o app usa 10.0.2.2 automaticamente.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val apiUrl: String = (localProps.getProperty("api.url") ?: "http://10.0.2.2:8080/")
    .let { if (it.endsWith("/")) it else "$it/" }

android {
    namespace = "br.com.arthurbaby"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "br.com.arthurbaby"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_URL", "\"$apiUrl\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)


    // Glide para carregar imagens
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Retrofit + Gson + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}