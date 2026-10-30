package com.ranger.popabus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.ranger.popabus.network.RetrofitClient
import com.ranger.popabus.network.UbicacionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ServicioGpsConductor : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var idRecorrido: String? = null
    private var idBusReal: String = "BUS_DESCONOCIDO"

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var mockReceiver: BroadcastReceiver? = null
    private var usandoMock = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        crearCanalNotificacion()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        idRecorrido = intent?.getStringExtra("ID_RECORRIDO")
        idBusReal = intent?.getStringExtra("ID_BUS") ?: "BUS_DESCONOCIDO"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, crearNotificacion(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, crearNotificacion())
        }
        iniciarRastreoGps()
        return START_STICKY
    }

    private fun enviarUbicacionAlBackend(lat: Double, lng: Double, velocidad: Double) {
        val hash = calcularGeohash(lat, lng)
        val recorridoActual = idRecorrido

        serviceScope.launch {
            try {
                val respuesta = RetrofitClient.apiService.enviarUbicacion(
                    UbicacionRequest(
                        id_bus = idBusReal,
                        id_recorrido = recorridoActual ?: "",
                        latitud = lat,
                        longitud = lng,
                        geohash = hash,
                        velocidad = velocidad
                    )
                )
                if (respuesta.isSuccessful) {
                    Log.d("GPS", "✅ Ubicación enviada a Render: $lat, $lng")
                }
            } catch (e: Exception) {
                Log.e("GPS", "❌ Error enviando ubicación: ${e.message}")
            }
        }
    }

    private fun iniciarRastreoGps() {
        // --- 1. RECEPTOR ADB MOCK ---
        mockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                usandoMock = true
                val lat = intent.getFloatExtra("lat", 0f).toDouble()
                val lng = intent.getFloatExtra("lng", 0f).toDouble()

                if (lat != 0.0 && lng != 0.0) {
                    enviarUbicacionAlBackend(lat, lng, 0.0)
                }
            }
        }
        val filter = IntentFilter("com.ranger.popabus.MOCK_LOCATION")
        ContextCompat.registerReceiver(this, mockReceiver, filter, ContextCompat.RECEIVER_EXPORTED)

        // --- 2. MOTOR GPS REAL ---
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(2000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (usandoMock) return
                for (location in locationResult.locations) {

                    // 🛑 FILTRO DE PRECISIÓN (El truco de tu amigo)
                    // Si la precisión del GPS es peor a 20 metros, ignoramos la coordenada.
                    if (location.accuracy > 20) {
                        Log.d("GPS", "Coordenada ignorada por baja precisión: ${location.accuracy}m")
                        continue
                    }

                    enviarUbicacionAlBackend(location.latitude, location.longitude, location.speed.toDouble())
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) { }
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("CANAL_GPS_POPABUS", "Servicio GPS Conductor", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun crearNotificacion(): Notification {
        return NotificationCompat.Builder(this, "CANAL_GPS_POPABUS")
            .setContentTitle("POPABUS: Viaje en curso")
            .setContentText("Transmitiendo ubicación a Render...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        mockReceiver?.let { unregisterReceiver(it) }
        serviceScope.cancel()
    }
}