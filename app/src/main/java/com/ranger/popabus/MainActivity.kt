package com.ranger.popabus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ranger.popabus.ui.theme.PopaBusTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color

// Representa la ubicación en tiempo real (BusesActivos)
data class BusActivo(
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val geohash: String = "",
    val timestamp: Long = 0L
)

// Representa la información estática (Colección 'buses')
data class BusOficial(
    val id: String = "",
    val placa: String = "",
    val modelo: String = "",
    val id_ruta: String = "",
    val id_conductor_actual: String = "",
    val estado: String = "activo"
)

// Representa los paraderos (Colección 'paraderos')
data class ParaderoOficial(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val geohash: String = "",
    val estado: String = "activo"
)

fun obtenerIconoBus(context: Context, vectorResId: Int): BitmapDescriptor? {
    MapsInitializer.initialize(context)
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

// NUEVA FUNCIÓN MATEMÁTICA: Interpola suavemente de la posición actual (start) a la destino (end)
// NUEVA FUNCIÓN MATEMÁTICA (CALIBRADA)
suspend fun animarMarcador(
    posicionActual: LatLng,
    posicionDestino: LatLng,
    onProgress: (LatLng) -> Unit

) {
    val durationMs = 6000L // Aumentamos a 6 segundos por cada tramo de la ruta
    val frames = 180 // Aumentamos a 180 pasos para que sea ultra suave (30 cuadros por segundo)
    val stepDelay = durationMs / frames

    val latDiff = posicionDestino.latitude - posicionActual.latitude
    val lngDiff = posicionDestino.longitude - posicionActual.longitude

    var currentLat = posicionActual.latitude
    var currentLng = posicionActual.longitude

    val latStep = latDiff / frames
    val lngStep = lngDiff / frames

    for (i in 1..frames) {
        currentLat += latStep
        currentLng += lngStep
        onProgress(LatLng(currentLat, currentLng))
        delay(stepDelay)
    }

    onProgress(posicionDestino)
}
// COMPONENTE: Cada bus gestiona su propia animación
@Composable
fun MarcadorBusAnimado(idBus: String, destinoLatLng: LatLng, icono: BitmapDescriptor?) {
    // Memoria individual para cada bus
    var ubicacionActual by remember { mutableStateOf(destinoLatLng) }

    LaunchedEffect(destinoLatLng) {
        if (ubicacionActual != destinoLatLng) {
            animarMarcador(
                posicionActual = ubicacionActual,
                posicionDestino = destinoLatLng,
                onProgress = { ubicacionActual = it }
            )
        }
    }

    Marker(
        state = MarkerState(position = ubicacionActual),
        title = "Bus: $idBus",
        icon = icono
    )
}




// FUNCIÓN NATIVA PARA GEOHASHING (Sin dependencias externas)
fun calcularGeohash(latitude: Double, longitude: Double, precision: Int = 9): String {
    val base32 = "0123456789bcdefghjkmnpqrstuvwxyz"
    var isEven = true
    var latMin = -90.0
    var latMax = 90.0
    var lonMin = -180.0
    var lonMax = 180.0
    var bit = 0
    var ch = 0
    val geohash = StringBuilder()

    while (geohash.length < precision) {
        if (isEven) {
            val mid = (lonMin + lonMax) / 2
            if (longitude >= mid) {
                ch = ch or (1 shl (4 - bit))
                lonMin = mid
            } else {
                lonMax = mid
            }
        } else {
            val mid = (latMin + latMax) / 2
            if (latitude >= mid) {
                ch = ch or (1 shl (4 - bit))
                latMin = mid
            } else {
                latMax = mid
            }
        }
        isEven = !isEven
        if (bit < 4) {
            bit++
        } else {
            geohash.append(base32[ch])
            bit = 0
            ch = 0
        }
    }
    return geohash.toString()
}





class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ya podemos eliminar probarConexionFirebase() de aquí
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.d("FirebaseOffline", "La memoria local ya estaba encendida")
        }



        setContent {
            PopaBusTheme {
                // Estado que controla qué pantalla estamos viendo
                var rolSeleccionado by remember { mutableStateOf("NINGUNO") }

                // Ruteo simple de Compose
                when (rolSeleccionado) {
                    "NINGUNO" -> PantallaSeleccionRol(
                        onConductorSeleccionado = { rolSeleccionado = "CONDUCTOR" },
                        onPasajeroSeleccionado = { rolSeleccionado = "PASAJERO" }
                    )
                    "PASAJERO" -> MapaPopabus() // Tu código intacto
                    "CONDUCTOR" -> PantallaConductor(
                        onVolver = { rolSeleccionado = "NINGUNO" }
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaSeleccionRol(onConductorSeleccionado: () -> Unit, onPasajeroSeleccionado: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a POPABUS", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onConductorSeleccionado) {
            Text("Entrar como Conductor")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onPasajeroSeleccionado) {
            Text("Entrar como Pasajero")
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun PantallaConductor(onVolver: () -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Estados de la interfaz
    var transmitiendo by remember { mutableStateOf(false) }
    var permisoConcedido by remember { mutableStateOf(false) }
    var ultimaUbicacion by remember { mutableStateOf("Esperando GPS...") }

    // Solicitud de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permisos ->
            permisoConcedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }
    )

    // Pedir permisos de ubicación nada más entrar al panel
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // MOTOR DE TRANSMISIÓN: Se activa cuando 'transmitiendo' es true
// MOTOR DE TRANSMISIÓN: Se activa cuando 'transmitiendo' es true
    DisposableEffect(transmitiendo, permisoConcedido) {
        var locationCallback: LocationCallback? = null
        var mockReceiver: BroadcastReceiver? = null
        var usandoMock = false // Bandera para saber si Linux tomó el control

        if (transmitiendo && permisoConcedido) {
            val database = FirebaseDatabase.getInstance()
            val busRef = database.getReference("BusesActivos/Bus_01")

            // 1. RECEPTOR DE COMANDOS ADB (LINUX)
            mockReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    usandoMock = true // Apagamos temporalmente el hardware real
                    val lat = intent.getFloatExtra("lat", 0f).toDouble()
                    val lng = intent.getFloatExtra("lng", 0f).toDouble()

                    if (lat != 0.0 && lng != 0.0) {
                        ultimaUbicacion = "📡 MOCK ADB:\n$lat, \n$lng"
                        val hash = calcularGeohash(lat, lng) // <-- NUEVA LÍNEA
                        val datosBus = mapOf(
                            "latitud" to lat,
                            "longitud" to lng,
                            "geohash" to hash, // <-- NUEVA LÍNEA
                            "timestamp" to ServerValue.TIMESTAMP
                        )
                        busRef.setValue(datosBus)
                    }
                }
            }

            // Registramos el receptor para que escuche el canal "MOCK_LOCATION"
            val filter = IntentFilter("com.ranger.popabus.MOCK_LOCATION")
            ContextCompat.registerReceiver(context, mockReceiver, filter, ContextCompat.RECEIVER_EXPORTED)

            // 2. CONFIGURACIÓN DEL GPS REAL
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (usandoMock) return // Si ADB está controlando, ignoramos el GPS real

                    for (location in locationResult.locations) {
                        ultimaUbicacion = "${location.latitude}, \n${location.longitude}"

                        // 1. PRIMERO calculamos el hash (DEBE estar antes del mapOf)
                        val hash = calcularGeohash(location.latitude, location.longitude)

                        // 2. DESPUÉS lo agregamos al mapa
                        val datosBus = mapOf(
                            "latitud" to location.latitude,
                            "longitud" to location.longitude,
                            "geohash" to hash,
                            "timestamp" to ServerValue.TIMESTAMP
                        )

                        // 3. ENVIAMOS a Firebase
                        busRef.setValue(datosBus)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

        // Cleanup: Detener el GPS y destruir el receptor de ADB al apagar
        onDispose {
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            mockReceiver?.let { context.unregisterReceiver(it) }
        }
    }





    // UI del Conductor
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Panel del Conductor", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text(if (permisoConcedido) "✅ Permisos de GPS OK" else "⚠️ Falta permiso GPS")
        Spacer(modifier = Modifier.height(32.dp))

        Text("Coordenada actual:")
        Text(ultimaUbicacion, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { transmitiendo = !transmitiendo },
            enabled = permisoConcedido
        ) {
            Text(if (transmitiendo) "🛑 Detener Transmisión" else "▶️ Iniciar Transmisión")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            transmitiendo = false // Forzamos apagar antes de salir
            onVolver()
        }) {
            Text("Volver al Inicio")
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapaPopabus() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var permisoConcedido by remember { mutableStateOf(false) }

    val popayan = LatLng(2.4419, -76.6063)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(popayan, 14f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permisos ->
            permisoConcedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (permisoConcedido) {
                Toast.makeText(context, "Ubicando pasajero...", Toast.LENGTH_SHORT).show()
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val miUbicacion = LatLng(location.latitude, location.longitude)
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(miUbicacion, 16f),
                                durationMs = 2000
                            )
                        }
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    val paraderoCentro = remember {
        ParaderoOficial(
            id = "P1",
            nombre = "Paradero Centro",
            direccion = "Ruta L1, L2",
            latitud = 2.4430,
            longitud = -76.6055
        )
    }

    // 1. ESTADO PARA MÚLTIPLES BUSES (Corregido para ser 100% reactivo)
    var busesEnZona by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }
    val iconoBus = remember { obtenerIconoBus(context, R.drawable.ic_bus) }

    // 2. MOTOR DE RECEPCIÓN ESPACIAL MULTIPLE
// 2. MOTOR DE RECEPCIÓN ESPACIAL (A prueba de balas + Logs)
    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val prefijoPasajero = calcularGeohash(popayan.latitude, popayan.longitude, 4)

        Log.d("FiltroEspacial", "1. Pasajero buscando buses con Geohash que empiece por: $prefijoPasajero")

        val query = database.getReference("BusesActivos")
            .orderByChild("geohash")
            .startAt(prefijoPasajero)
            .endAt(prefijoPasajero + "\uf8ff")

        query.keepSynced(true)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val busesRecibidos = mutableMapOf<String, LatLng>()
                Log.d("FiltroEspacial", "2. Firebase respondió con ${snapshot.childrenCount} buses")

                for (busSnapshot in snapshot.children) {
                    val idBus = busSnapshot.key ?: continue

                    // Lectura segura: Extraemos el valor sin importar si es texto o número en Firebase
                    val latObj = busSnapshot.child("latitud").value
                    val lngObj = busSnapshot.child("longitud").value
                    val geohashBus = busSnapshot.child("geohash").value

                    // Lo forzamos a convertirse en Decimal de forma segura
                    val lat = (latObj as? Number)?.toDouble() ?: latObj?.toString()?.toDoubleOrNull()
                    val lng = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()?.toDoubleOrNull()

                    Log.d("FiltroEspacial", "-> Procesando $idBus | Lat: $lat, Lng: $lng, Hash: $geohashBus")

                    if (lat != null && lng != null) {
                        busesRecibidos[idBus] = LatLng(lat, lng)
                    } else {
                        Log.w("FiltroEspacial", "-> IGNORADO $idBus: Coordenadas inválidas")
                    }
                }

                busesEnZona = busesRecibidos
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error en consulta espacial", error.toException())
            }
        }

        query.addValueEventListener(listener)
        onDispose { query.removeEventListener(listener) }
    }

    // 3. EL MAPA
    val mapProperties = MapProperties(isMyLocationEnabled = permisoConcedido)

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties
    ) {
// Marcador del paradero actualizado
        Marker(
            state = MarkerState(position = LatLng(paraderoCentro.latitud, paraderoCentro.longitud)),
            title = paraderoCentro.nombre,
            snippet = paraderoCentro.direccion
        )

        // 4. DIBUJAMOS TODOS LOS BUSES ACTIVOS SIMULTÁNEAMENTE
        busesEnZona.forEach { (idBus, destino) ->
            // La función key() obliga a Compose a recordar la animación de CADA bus por separado
            key(idBus) {
                MarcadorBusAnimado(
                    idBus = idBus,
                    destinoLatLng = destino,
                    icono = iconoBus
                )
            }
        }
    }
}
