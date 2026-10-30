package com.ranger.popabus

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.ranger.popabus.network.ActividadRespuesta
import com.ranger.popabus.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ServicioRastreoPasajero : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var wakeLock: PowerManager.WakeLock? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null

    private val flotaBuses = mutableMapOf<String, Location>()
    private val contadoresProximidad = mutableMapOf<String, Int>()

    private var yaRegistrado = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CANAL_RASTREO_PASAJERO",
                "Rastreo de Abordaje",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificacion = NotificationCompat.Builder(this, "CANAL_RASTREO_PASAJERO")
            .setContentTitle("POPABUS: Calculando abordaje")
            .setContentText("Buscando en qué bus estás viajando...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(2, notificacion, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(2, notificacion)
        }

        iniciarRastreoEspacial()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun iniciarRastreoEspacial() {
        // 1. Adquirir WakeLock para evitar que el sistema duerma el servicio
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PopaBus::RastreoAbordaje")
        wakeLock?.acquire(30 * 60 * 1000L) // Máximo 30 minutos

        // 2. Escuchar la posición de los buses vía Render (Polling cada 5 segundos)
        pollingJob = serviceScope.launch {
            while (true) {
                try {
                    val respuesta = RetrofitClient.apiService.obtenerBusesActivos()
                    if (respuesta.isSuccessful && respuesta.body()?.status == "ok") {
                        val mapaTemporal = mutableMapOf<String, Location>()
                        for (bus in respuesta.body()!!.buses) {
                            val locBus = Location("render").apply {
                                latitude = bus.latitud
                                longitude = bus.longitud
                            }
                            mapaTemporal[bus.id_bus] = locBus
                        }
                        flotaBuses.clear()
                        flotaBuses.putAll(mapaTemporal)
                    }
                } catch (e: Exception) {
                    Log.e("RastreoAbordaje", "Error obteniendo buses: ${e.message}")
                }
                delay(5000) // Consultar cada 5 segundos
            }
        }

        // 3. Solicitar ubicación del pasajero cada 5 segundos
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (yaRegistrado) return
                val locPasajero = locationResult.lastLocation ?: return
                val resultadosDistancia = FloatArray(1)

                // 4. Evaluar la proximidad con la flota de buses
                for ((idBus, locBus) in flotaBuses) {
                    Location.distanceBetween(
                        locPasajero.latitude, locPasajero.longitude,
                        locBus.latitude, locBus.longitude,
                        resultadosDistancia
                    )

                    val distancia = resultadosDistancia[0]

                    if (distancia < 20f) { // Menos de 20 metros de proximidad
                        val conteoActual = contadoresProximidad.getOrDefault(idBus, 0) + 1
                        contadoresProximidad[idBus] = conteoActual
                        Log.d("RastreoAbordaje", "Pasajero cerca de $idBus: $conteoActual/12 (${distancia}m)")

                        // 12 ciclos * 5 segundos = ~60 segundos constantes junto al bus
                        if (conteoActual >= 12) {
                            registrarAbordajeAutomatico(idBus)
                            break
                        }
                    } else {
                        contadoresProximidad[idBus] = 0
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("RastreoAbordaje", "Permiso denegado", e)
            stopSelf()
        }
    }

    private fun registrarAbordajeAutomatico(idBus: String) {
        yaRegistrado = true
        // Usamos el usuario actual, o "anonimo" si no hay login
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"

        serviceScope.launch {
            try {
                RetrofitClient.apiService.crearActividad(
                    ActividadRespuesta(
                        id_usuario = uid,
                        id_bus = idBus,
                        id_ruta = "RUTA_DESCONOCIDA", // Luego podemos buscar la ruta real
                        fecha_abordaje = System.currentTimeMillis(),
                        metodo_registro = "automatico"
                    )
                )
                Log.d("RastreoAbordaje", "Abordaje guardado en Render")
            } catch (e: Exception) {
                Log.e("RastreoAbordaje", "Error al guardar abordaje: ${e.message}")
            } finally {
                stopSelf() // Apagamos el servicio
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        pollingJob?.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        serviceScope.cancel()
    }
}