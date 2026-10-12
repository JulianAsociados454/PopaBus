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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ServicioRastreoPasajero : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var wakeLock: PowerManager.WakeLock? = null
    
    private val database = FirebaseDatabase.getInstance()
    private var firebaseListener: ValueEventListener? = null
    
    // Almacenamos coordenadas de la flota y el contador de proximidad
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

        // Usamos ID 2 para que no reemplace la notificación del Conductor (ID 1)
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
        // Tiempo máximo de seguridad: 30 minutos, luego se libera automáticamente
        wakeLock?.acquire(30 * 60 * 1000L)

        // 2. Escuchar la posición de todos los buses activos en Firebase
        val refBusesActivos = database.getReference("BusesActivos")
        firebaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mapaTemporal = mutableMapOf<String, Location>()
                for (hijo in snapshot.children) {
                    val idBus = hijo.key ?: continue
                    val latObj = hijo.child("latitud").value
                    val lngObj = hijo.child("longitud").value

                    val lat = (latObj as? Number)?.toDouble() ?: latObj?.toString()?.toDoubleOrNull()
                    val lng = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()?.toDoubleOrNull()

                    if (lat != null && lng != null) {
                        val locBus = Location("firebase").apply {
                            latitude = lat
                            longitude = lng
                        }
                        mapaTemporal[idBus] = locBus
                    }
                }
                flotaBuses.clear()
                flotaBuses.putAll(mapaTemporal)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        refBusesActivos.addValueEventListener(firebaseListener!!)

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
                        // Si se aleja o es un falso positivo, el contador se reinicia a 0
                        contadoresProximidad[idBus] = 0
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("RastreoAbordaje", "Permiso denegado al iniciar rastreo espacial", e)
            stopSelf()
        }
    }

    private fun registrarAbordajeAutomatico(idBus: String) {
        yaRegistrado = true
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Extraemos la ruta a la que pertenece ese bus de la colección "buses"
        database.getReference("buses").child(idBus).child("id_ruta").get().addOnSuccessListener { snapshot ->
            val idRuta = snapshot.getValue(String::class.java) ?: "DESCONOCIDA"

            val refActividad = database.getReference("actividad_usuario").child(uid).push()
            
            val nuevaActividad = ActividadUsuario(
                id_actividad = refActividad.key ?: "",
                id_bus = idBus,
                id_ruta = idRuta,
                fecha_abordaje = System.currentTimeMillis(),
                metodo_registro = "automatico"
            )

            refActividad.setValue(nuevaActividad).addOnSuccessListener {
                Log.d("RastreoAbordaje", "Abordaje automático consolidado: $idBus en la ruta $idRuta")
                stopSelf() // Apagamos el servicio automáticamente
            }.addOnFailureListener {
                stopSelf()
            }
        }.addOnFailureListener {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberación de recursos y ahorro de batería
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        firebaseListener?.let {
            database.getReference("BusesActivos").removeEventListener(it)
        }
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}
