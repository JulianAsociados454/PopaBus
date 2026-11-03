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
    
    // Máquina de estados: Variables a nivel de clase
    private var viajeRegistrado = false
    private var idBusAbordado: String? = null
    private var horaInicioViaje: Long = 0L
    private var contadorLejania: Int = 0
    private var idRegistroActividad: String? = null

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
        // Tiempo máximo de seguridad: 120 minutos, luego se libera automáticamente
        wakeLock?.acquire(120 * 60 * 1000L)

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
                val locPasajero = locationResult.lastLocation ?: return
                val resultadosDistancia = FloatArray(1)

                // FASE 2: El Descenso y Cálculo Matemático
                if (viajeRegistrado) {
                    val idBus = idBusAbordado ?: return
                    val locBus = flotaBuses[idBus]
                    
                    if (locBus == null) {
                        // El bus se desconectó temporalmente de Firebase. Ignoramos para evitar falsos positivos
                        return
                    }

                    Location.distanceBetween(
                        locPasajero.latitude, locPasajero.longitude,
                        locBus.latitude, locBus.longitude,
                        resultadosDistancia
                    )
                    
                    val distancia = resultadosDistancia[0]

                    if (distancia > 100f) { // Margen de seguridad: 100 metros
                        contadorLejania++
                        Log.d("RastreoAbordaje", "Pasajero lejos del bus $idBus: $contadorLejania/12 (${distancia}m)")

                        // 12 ciclos * 5 segundos = ~1 minuto continuo lejos del bus
                        if (contadorLejania >= 12) {
                            val duracionMinutos = (System.currentTimeMillis() - horaInicioViaje) / 60000
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
                            val idRegistro = idRegistroActividad ?: return

                            Log.d("RastreoAbordaje", "Descenso detectado. Duración: $duracionMinutos min.")

                            val updates = mapOf<String, Any>(
                                "estado" to "finalizado",
                                "duracionMinutos" to duracionMinutos
                            )
                            
                            database.getReference("actividad_usuario").child(uid).child(idRegistro)
                                .updateChildren(updates).addOnCompleteListener {
                                    Log.d("RastreoAbordaje", "Viaje finalizado guardado en Firebase.")
                                    // AHORA SÍ: Liberar WakeLock, remover updates y morir.
                                    detenerYLimpiarServicio()
                                }
                            
                            // Bloqueo para evitar registrar múltiples descensos mientras se guarda
                            viajeRegistrado = false
                        }
                    } else {
                        // Vuelve a estar a menos de 100m (falsa alarma, túnel, etc.)
                        contadorLejania = 0
                    }
                    
                    return
                }

                // FASE 1: Abordaje
                // Evaluar la proximidad con la flota de buses
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
                            if (!viajeRegistrado) {
                                viajeRegistrado = true
                                idBusAbordado = idBus
                                horaInicioViaje = System.currentTimeMillis()
                                
                                val uid = FirebaseAuth.getInstance().currentUser?.uid
                                if (uid != null) {
                                    val nuevaActividadRef = database.getReference("actividad_usuario").child(uid).push()
                                    idRegistroActividad = nuevaActividadRef.key
                                    
                                    val latAbordaje = locPasajero.latitude
                                    val lngAbordaje = locPasajero.longitude
                                    registrarAbordajeAutomatico(idBus, latAbordaje, lngAbordaje)
                                }
                            }
                            break // Ignoramos el resto del ciclo para no duplicar buses
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

    private fun registrarAbordajeAutomatico(idBus: String, latAbordaje: Double, lngAbordaje: Double) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val idRegistro = idRegistroActividad ?: return

        // Extraemos la ruta a la que pertenece ese bus de la colección "buses"
        database.getReference("buses").child(idBus).child("id_ruta").get().addOnSuccessListener { snapshotRuta ->
            val idRuta = snapshotRuta.getValue(String::class.java) ?: "DESCONOCIDA"

            // Buscar paradero más cercano
            database.getReference("paraderos").get().addOnSuccessListener { snapshotParaderos ->
                var nombreParaderoCercano = "Paradero desconocido"
                var distanciaMinima = Float.MAX_VALUE
                val resultados = FloatArray(1)

                for (hijo in snapshotParaderos.children) {
                    val nombre = hijo.child("nombre").getValue(String::class.java) ?: continue
                    val latObj = hijo.child("latitud").value
                    val lngObj = hijo.child("longitud").value

                    val latParadero = (latObj as? Number)?.toDouble() ?: latObj?.toString()?.toDoubleOrNull() ?: continue
                    val lngParadero = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()?.toDoubleOrNull() ?: continue

                    Location.distanceBetween(latAbordaje, lngAbordaje, latParadero, lngParadero, resultados)
                    if (resultados[0] < distanciaMinima) {
                        distanciaMinima = resultados[0]
                        nombreParaderoCercano = nombre
                    }
                }

                guardarActividadEnCurso(uid, idRegistro, idBus, idRuta, nombreParaderoCercano)

            }.addOnFailureListener {
                guardarActividadEnCurso(uid, idRegistro, idBus, idRuta, "Ubicación sin paradero")
            }
        }.addOnFailureListener {
            Log.e("RastreoAbordaje", "Error obteniendo ruta del bus", it)
            guardarActividadEnCurso(uid, idRegistro, idBus, "DESCONOCIDA", "Ubicación sin paradero")
        }
    }

    private fun guardarActividadEnCurso(uid: String, idRegistro: String, idBus: String, idRuta: String, paradero: String) {
        val actividad = mapOf<String, Any>(
            "id_actividad" to idRegistro,
            "id_bus" to idBus,
            "id_ruta" to idRuta,
            "fecha_abordaje" to horaInicioViaje,
            "metodo_registro" to "automatico",
            "estado" to "en_curso",
            "paradero_origen" to paradero
        )

        database.getReference("actividad_usuario").child(uid).child(idRegistro)
            .updateChildren(actividad).addOnSuccessListener {
                Log.d("RastreoAbordaje", "Abordaje automático consolidado: $idBus en la ruta $idRuta (Paradero: $paradero)")
            }.addOnFailureListener {
                Log.e("RastreoAbordaje", "Error guardando abordaje", it)
            }
    }

    private fun detenerYLimpiarServicio() {
        // Detener las lecturas de GPS para que no sigan ejecutándose ni consumiendo batería
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        // Liberar el seguro del procesador de forma inmediata
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        // Destruir el servicio (el sistema llamará a onDestroy() completando la limpieza)
        stopSelf()
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