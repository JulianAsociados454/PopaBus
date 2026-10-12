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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class ServicioGpsConductor : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var idRecorrido: String? = null
    private var idBusReal: String = "BUS_DESCONOCIDO"

    // Mantenemos tus superpoderes de ADB para las pruebas
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

        // Atrapamos el ID del bus (Si por algún motivo llega vacío, previene colapsos)
        idBusReal = intent?.getStringExtra("ID_BUS") ?: "BUS_DESCONOCIDO"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, crearNotificacion(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, crearNotificacion())
        }
        iniciarRastreoGps()
        return START_STICKY
    }
    private fun iniciarRastreoGps() {
        val database = FirebaseDatabase.getInstance()
        // Usamos el ID dinámico que pertenece a este conductor específico
        val busRef = database.getReference("BusesActivos/$idBusReal")

        // --- 1. RECEPTOR ADB MOCK ---
        mockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                usandoMock = true
                val lat = intent.getFloatExtra("lat", 0f).toDouble()
                val lng = intent.getFloatExtra("lng", 0f).toDouble()

                if (lat != 0.0 && lng != 0.0) {
                    // Nota: calcularGeohash y PosicionGpsOficial ya existen en tu proyecto
                    val hash = calcularGeohash(lat, lng)

                    val datosBus = mapOf(
                        "latitud" to lat, "longitud" to lng,
                        "geohash" to hash, "timestamp" to ServerValue.TIMESTAMP
                    )
                    busRef.setValue(datosBus)

                    if (idRecorrido != null) {
                        val refPosiciones = database.getReference("posiciones_gps").child(idRecorrido!!)
                        refPosiciones.push().setValue(
                            PosicionGpsOficial(
                                id_recorrido = idRecorrido!!, latitud = lat, longitud = lng,
                                geohash = hash, velocidad = 0.0, hora_lectura = System.currentTimeMillis()
                            )
                        )
                    }
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
                    val hash = calcularGeohash(location.latitude, location.longitude)

                    val datosBus = mapOf(
                        "latitud" to location.latitude, "longitud" to location.longitude,
                        "geohash" to hash, "timestamp" to ServerValue.TIMESTAMP
                    )
                    busRef.setValue(datosBus)

                    if (idRecorrido != null) {
                        val refPosiciones = database.getReference("posiciones_gps").child(idRecorrido!!)
                        refPosiciones.push().setValue(
                            PosicionGpsOficial(
                                id_recorrido = idRecorrido!!, latitud = location.latitude,
                                longitud = location.longitude, geohash = hash,
                                velocidad = location.speed.toDouble(), hora_lectura = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            // Se maneja de forma silenciosa. Si llegamos aquí, la Activity ya garantizó los permisos.
        }
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CANAL_GPS_POPABUS", "Servicio GPS Conductor", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun crearNotificacion(): Notification {
        return NotificationCompat.Builder(this, "CANAL_GPS_POPABUS")
            .setContentTitle("POPABUS: Viaje en curso")
            .setContentText("Transmitiendo ubicación a los pasajeros...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Ícono nativo de Android
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // No se puede deslizar para borrar
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiamos los motores si se detiene el servicio
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        mockReceiver?.let { unregisterReceiver(it) }
    }
}