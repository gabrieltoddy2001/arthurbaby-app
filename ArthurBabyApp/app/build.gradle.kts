import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

/**
 * Descobre o IP do PC na rede local (Wi-Fi/Ethernet) a cada build, para o celular
 * físico acessar o backend sem precisar de "adb reverse".
 * É um ValueSource para o configuration cache reavaliar o IP quando você troca de rede.
 */
abstract class LanIpValueSource : ValueSource<String, ValueSourceParameters.None> {
    override fun obtain(): String? {
        val ignorar = listOf("virtual", "vethernet", "vmware", "hyper-v", "wsl", "docker", "loopback", "bluetooth")
        val candidatos = NetworkInterface.getNetworkInterfaces().toList()
            .filter { it.isUp && !it.isLoopback && !it.isVirtual }
            .filter { nic -> ignorar.none { nic.displayName.lowercase().contains(it) || nic.name.lowercase().contains(it) } }
            .flatMap { it.inetAddresses.toList() }
            .filterIsInstance<Inet4Address>()
            .filter { it.isSiteLocalAddress }
            .map { it.hostAddress }
        // Prioriza as faixas típicas de roteador doméstico
        return candidatos.firstOrNull { it.startsWith("192.168.") }
            ?: candidatos.firstOrNull { it.startsWith("10.") }
            ?: candidatos.firstOrNull()
    }
}

// Permite fixar o IP manualmente em local.properties (api.host=192.168.x.x), se a detecção errar
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val apiPort: String = localProps.getProperty("api.port") ?: "8080"
val apiHost: String = localProps.getProperty("api.host")
    ?: providers.of(LanIpValueSource::class) {}.orNull
    ?: "10.0.2.2"

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

        // URL do backend no celular físico (IP do PC na rede) e no emulador (10.0.2.2 = localhost do PC)
        buildConfigField("String", "API_URL_DEVICE", "\"http://$apiHost:$apiPort/\"")
        buildConfigField("String", "API_URL_EMULATOR", "\"http://10.0.2.2:$apiPort/\"")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // Retrofit + Gson + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}