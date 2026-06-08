package com.example.myapplication.data.remote

import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // El emulador llega al host por la IP especial 10.0.2.2; un celular físico llega por la
    // IP de la PC en la Wi-Fi. Detectamos el entorno en runtime para que el MISMO APK funcione
    // en ambos a la vez. Ambas IPs están permitidas en network_security_config.xml.
    // Si cambias de red Wi-Fi, actualiza LAN_HOST con la nueva IP de la PC (ipconfig).
    private const val EMULATOR_HOST = "10.0.2.2"
    private const val LAN_HOST = "192.168.100.13"
    private const val PORT = 5114

    private val isEmulator: Boolean
        get() = Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.lowercase().contains("vbox") ||
            Build.FINGERPRINT.lowercase().contains("emulator") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            Build.PRODUCT.contains("sdk") ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu")

    private val BASE_URL: String
        get() = "http://${if (isEmulator) EMULATOR_HOST else LAN_HOST}:$PORT/"

    val api: EparkApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EparkApi::class.java)
    }
}
