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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth

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

// Representa el historial de viajes (Colección 'recorridos')
data class RecorridoOficial(
    val id_usuario: String = "",
    val id_bus: String = "",
    val id_ruta: String = "",
    val hora_inicio: Long = 0L,
    val hora_fin: Long? = null, // Nullable porque al iniciar no ha terminado
    val estado: String = "en_curso"
)

// Representa el histórico de GPS (Colección 'posiciones_gps')
data class PosicionGpsOficial(
    val id_recorrido: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val geohash: String = "",
    val velocidad: Double? = null,
    val hora_lectura: Long = 0L
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
                // EVOLUCIÓN: Ahora controlamos pantallas completas, no solo roles
                var pantallaActual by remember { mutableStateOf("LOGIN") }

                when (pantallaActual) {
                    "LOGIN" -> PantallaLogin(
                        onIrARegistro = { pantallaActual = "REGISTRO" },
                        onLoginExitoso = { rolRecibido ->
                            // El .trim() elimina espacios en blanco accidentales de Firebase
                            if (rolRecibido.trim().lowercase() == "conductor") {
                                pantallaActual = "CONDUCTOR"
                            } else {
                                pantallaActual = "PASAJERO"
                            }
                        }
                    )
                    "REGISTRO" -> PantallaRegistro(
                        onVolverLogin = { pantallaActual = "LOGIN" },
                        onRegistroExitoso = { uidGenerado, nombreIngresado, rolElegido ->
                            // 1. Instanciamos la base de datos
                            val database = FirebaseDatabase.getInstance()

                            // 2. Apuntamos a la colección 'usuarios' y creamos un "hijo" con el UID exacto de Auth
                            val refUsuario = database.getReference("usuarios").child(uidGenerado)

                            // 3. Armamos el paquete de datos según tu diccionario de datos (MER)
                            val datosUsuario = mapOf(
                                "nombre_completo" to nombreIngresado,
                                "rol" to rolElegido,
                                "activo" to true,
                                "fecha_registro" to ServerValue.TIMESTAMP
                            )

                            // 4. Guardamos en la base de datos y avanzamos de pantalla
                            refUsuario.setValue(datosUsuario).addOnCompleteListener { tarea ->
                                if (tarea.isSuccessful) {
                                    // Cuando se guarde exitosamente, enviamos al usuario al menú
                                    pantallaActual = "SELECCION_ROL"
                                }
                            }
                        }
                    )
                    "SELECCION_ROL" -> PantallaSeleccionRol(
                        onConductorSeleccionado = { pantallaActual = "CONDUCTOR" },
                        onPasajeroSeleccionado = { pantallaActual = "PASAJERO" }
                    )
                    "PASAJERO" -> MapaPopabus(
                        onCerrarSesion = {
                            FirebaseAuth.getInstance().signOut()
                            pantallaActual = "LOGIN"
                        }
                    )
                    "CONDUCTOR" -> PantallaConductor(
                        onCerrarSesion = {
                            FirebaseAuth.getInstance().signOut()
                            pantallaActual = "LOGIN"
                        }
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
@Composable
fun PantallaLogin(onIrARegistro: () -> Unit, onLoginExitoso: (String) -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) } // Para mostrar retroalimentación visual

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (correo.isNotEmpty() && contrasena.isNotEmpty()) {
                    cargando = true
                    // 1. Firebase Auth valida las credenciales
                    auth.signInWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    // 2. Si Auth es exitoso, buscamos el rol en Realtime Database
                                    database.getReference("usuarios").child(uid).child("rol").get()
                                        .addOnSuccessListener { snapshot ->
                                            cargando = false
                                            // Extraemos el rol (o asumimos "pasajero" por defecto)
                                            val rol = snapshot.getValue(String::class.java) ?: "pasajero"
                                            Toast.makeText(context, "Bienvenido", Toast.LENGTH_SHORT).show()
                                            // 3. Enviamos el rol al enrutador de MainActivity
                                            onLoginExitoso(rol)
                                        }
                                        .addOnFailureListener { error ->
                                            cargando = false
                                            Toast.makeText(context, "Error al leer perfil: ${error.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
                            } else {
                                cargando = false
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Por favor, llena todos los campos.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando // Deshabilita el botón mientras carga para evitar dobles clics
        ) {
            Text(if (cargando) "Validando..." else "Ingresar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onIrARegistro) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }
    }
}


@Composable
fun PantallaRegistro(onVolverLogin: () -> Unit, onRegistroExitoso: (String, String, String) -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() } // Instancia de Firebase Auth

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf("pasajero") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Selección de Rol
        Text("¿Qué tipo de usuario eres?", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { rolSeleccionado = "pasajero" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (rolSeleccionado == "pasajero") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Pasajero")
            }
            Button(
                onClick = { rolSeleccionado = "conductor" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (rolSeleccionado == "conductor") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Conductor")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // LÓGICA DE REGISTRO EN FIREBASE
                if (correo.isNotEmpty() && contrasena.isNotEmpty() && nombre.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                Toast.makeText(context, "¡Usuario creado exitosamente!", Toast.LENGTH_SHORT).show()
                                // Enviamos los datos para usarlos en el siguiente paso (Días 8-9)
                                onRegistroExitoso(user?.uid ?: "", nombre, rolSeleccionado)
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Por favor, llena todos los campos.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarme")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onVolverLogin) {
            Text("¿Ya tienes cuenta? Inicia sesión aquí")
        }
    }
}



@SuppressLint("MissingPermission")
@Composable
fun PantallaConductor(onCerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Estados de la interfaz
    var transmitiendo by remember { mutableStateOf(false) }
    var permisoConcedido by remember { mutableStateOf(false) }
    var ultimaUbicacion by remember { mutableStateOf("Esperando GPS...") }

    // NUEVO ESTADO: Controla el Ciclo de Vida del Viaje
    var enRecorrido by remember { mutableStateOf(false) }

    // Instancias de Firebase
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }

    // Guarda el ID del viaje generado por Firebase
    var idRecorridoActual by remember { mutableStateOf<String?>(null) }
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
// 1. RECEPTOR DE COMANDOS ADB (LINUX)
            mockReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    usandoMock = true
                    val lat = intent.getFloatExtra("lat", 0f).toDouble()
                    val lng = intent.getFloatExtra("lng", 0f).toDouble()

                    if (lat != 0.0 && lng != 0.0) {
                        ultimaUbicacion = "📡 MOCK ADB:\n$lat, \n$lng"
                        val hash = calcularGeohash(lat, lng)
                        val timestampActual = ServerValue.TIMESTAMP

                        // A. ACTUALIZAR TIEMPO REAL (BusesActivos)
                        val datosBus = mapOf(
                            "latitud" to lat,
                            "longitud" to lng,
                            "geohash" to hash,
                            "timestamp" to timestampActual
                        )
                        busRef.setValue(datosBus)

                        // B. BIFURCACIÓN HISTÓRICA (posiciones_gps)
                        if (idRecorridoActual != null) {
                            val refPosiciones = database.getReference("posiciones_gps").child(idRecorridoActual!!)
                            val nuevaPosicion = PosicionGpsOficial(
                                id_recorrido = idRecorridoActual!!,
                                latitud = lat,
                                longitud = lng,
                                geohash = hash,
                                velocidad = 0.0, // Mock por defecto
                                hora_lectura = System.currentTimeMillis()
                            )
                            refPosiciones.push().setValue(nuevaPosicion)
                        }
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
                    if (usandoMock) return

                    for (location in locationResult.locations) {
                        ultimaUbicacion = "${location.latitude}, \n${location.longitude}"

                        val hash = calcularGeohash(location.latitude, location.longitude)
                        val timestampActual = ServerValue.TIMESTAMP

                        // A. ACTUALIZAR TIEMPO REAL (BusesActivos)
                        val datosBus = mapOf(
                            "latitud" to location.latitude,
                            "longitud" to location.longitude,
                            "geohash" to hash,
                            "timestamp" to timestampActual
                        )
                        busRef.setValue(datosBus)

                        // B. BIFURCACIÓN HISTÓRICA (posiciones_gps)
                        if (idRecorridoActual != null) {
                            val refPosiciones = database.getReference("posiciones_gps").child(idRecorridoActual!!)
                            val nuevaPosicion = PosicionGpsOficial(
                                id_recorrido = idRecorridoActual!!,
                                latitud = location.latitude,
                                longitud = location.longitude,
                                geohash = hash,
                                velocidad = location.speed.toDouble(),
                                hora_lectura = System.currentTimeMillis()
                            )
                            refPosiciones.push().setValue(nuevaPosicion)
                        }
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





// NUEVA UI DEL CONDUCTOR
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Panel del Conductor", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Text(if (permisoConcedido) "✅ Permisos de GPS OK" else "⚠️ Falta permiso GPS")
        Spacer(modifier = Modifier.height(16.dp))

        Text("Coordenada actual:")
        Text(ultimaUbicacion, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))

        // LÓGICA VISUAL DEL RECORRIDO
        if (!enRecorrido) {
            // Botón para INICIAR
            Button(
                onClick = {
                    // 1. Obtenemos el UID real del conductor
                    val uid = auth.currentUser?.uid ?: "CONDUCTOR_DESCONOCIDO"

                    // 2. Apuntamos a la colección 'recorridos'
                    val refRecorridos = database.getReference("recorridos")
                    val nuevoRecorridoRef = refRecorridos.push() // Firebase genera un ID único

                    val idGenerado = nuevoRecorridoRef.key

                    // 3. Armamos el paquete según el diccionario de datos
                    val nuevoRecorrido = RecorridoOficial(
                        id_usuario = uid,
                        id_bus = "BUS_1",
                        id_ruta = "RUTA_1",
                        hora_inicio = System.currentTimeMillis(), // Hora exacta
                        hora_fin = null,
                        estado = "en_curso"
                    )

                    // 4. Guardamos en Firebase
                    nuevoRecorridoRef.setValue(nuevoRecorrido).addOnSuccessListener {
                        idRecorridoActual = idGenerado // Memoria del viaje activo
                        enRecorrido = true
                        transmitiendo = true // Arranca el GPS
                        Toast.makeText(context, "Viaje Oficial Iniciado", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = permisoConcedido,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            ) {
                Text("▶️ Iniciar Recorrido Oficial")
            }
        } else {
            // ... (Tu código de cuando está en recorrido sigue igual) ...else {
            // Interfaz cuando el viaje está EN CURSO
            Text("🟢 Viaje en Curso", color = Color.Green, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    enRecorrido = false
                    transmitiendo = false // El GPS se apaga al terminar
                    ultimaUbicacion = "Viaje finalizado."
                    // Etapa 6: Aquí cerraremos el registro en Firebase
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            ) {
                Text("🛑 Finalizar Recorrido")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Botón de Cerrar Sesión
        Button(
            onClick = {
                transmitiendo = false
                onCerrarSesion()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Cerrar Sesión")
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapaPopabus(onCerrarSesion: () -> Unit) {
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

// 3. EL MAPA Y EL RADAR VISUAL
    val mapProperties = MapProperties(isMyLocationEnabled = permisoConcedido)

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. El Mapa va al fondo
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {
            Marker(
                state = MarkerState(position = LatLng(paraderoCentro.latitud, paraderoCentro.longitud)),
                title = paraderoCentro.nombre,
                snippet = paraderoCentro.direccion
            )

            busesEnZona.forEach { (idBus, destino) ->
                key(idBus) {
                    MarcadorBusAnimado(
                        idBus = idBus,
                        destinoLatLng = destino,
                        icono = iconoBus
                    )
                }
            }
        }

        // 2. El Radar va en la parte superior (TopCenter)
        Text(
            text = "Buses en radar: ${busesEnZona.size}",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(32.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Red
        )

        // 3. El Botón de Cerrar Sesión va en la parte inferior (BottomCenter)
        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar Sesión")
        }
    } // Fin del Box
} // Fin de la función MapaPopabus