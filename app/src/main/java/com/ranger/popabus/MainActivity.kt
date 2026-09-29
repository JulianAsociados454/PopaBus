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
import com.google.maps.android.compose.Polyline
import androidx.compose.material3.Switch
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.OutlinedButton
import android.location.Location
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedButton

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
// --- NUEVOS MODELOS PARA EL SPRINT 4 ---

// Representa un punto para dibujar la línea (Polyline) de la ruta por las calles
data class CoordenadaRuta(
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)

// Representa la relación y el orden de un paradero dentro de una ruta (Diccionario de Datos)
data class ParaderoRuta(
    val orden: Int = 0
)

// Representa la ruta completa (Colección 'rutas')
data class RutaOficial(
    val id: String = "",
    val nombre_ruta: String = "",
    val origen: String = "",
    val destino: String = "",
    val estado: String = "activa",
    // El diccionario de paraderos como definimos en las reglas de negocio
    val paraderos: Map<String, ParaderoRuta> = emptyMap(),
    // La geometría estructurada para que Compose pueda dibujar la línea (Polyline)
    val geometria: List<CoordenadaRuta> = emptyList()
)

// --- NUEVO MODELO PARA EL SPRINT 11 ---

// Representa el perfil de un usuario (Colección 'usuarios')
data class UsuarioOficial(
    val id: String = "", // Este será el UID de Firebase Auth
    val nombre_completo: String = "",
    val rol: String = "pasajero",
    val activo: Boolean = true,
    val fecha_registro: Long = 0L
)

// --- NUEVOS MODELOS PARA EL SPRINT 6 ---

// Representa un reporte de problema en el sistema (Colección 'incidencias')
data class IncidenciaOficial(
    val id_usuario: String = "",
    val descripcion: String = "",
    val estado: String = "pendiente",
    val fecha_reporte: Long = 0L,
    // --- NUEVO SPRINT 10: COORDENADAS DEL REPORTE ---
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)
// Representa una consulta realizada por un pasajero (Colección 'historial_busquedas')
data class HistorialBusquedaOficial(
    val id_usuario: String = "",
    val destino_buscado: String = "",
    val rutas_resultado: Map<String, Boolean> = emptyMap(),
    val fecha_busqueda: Long = 0L
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
fun MarcadorBusAnimado(
    idBus: String,
    destinoLatLng: LatLng,
    icono: BitmapDescriptor?,
    textoEta: String // <-- NUEVO SPRINT 9: Recibimos el texto predictivo
) {
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
        snippet = "ETA: $textoEta", // <-- NUEVO SPRINT 9: Mostramos el tiempo de llegada al tocar el bus
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




// NUEVA FUNCIÓN MATEMÁTICA (SPRINT 9): Calcula la distancia real en metros entre dos puntos
fun calcularDistanciaMetros(origen: LatLng, destino: LatLng): Float {
    val resultados = FloatArray(1)
    Location.distanceBetween(
        origen.latitude, origen.longitude,
        destino.latitude, destino.longitude,
        resultados
    )
    return resultados[0]
}

// NUEVA FUNCIÓN LÓGICA (SPRINT 9): Convierte la distancia en un texto predictivo de ETA
fun obtenerTextoETA(distanciaMetros: Float): String {
    // Asumimos velocidad urbana en Popayán: ~15 km/h (250 metros por minuto)
    val velocidadMetrosPorMinuto = 250f
    val minutos = (distanciaMetros / velocidadMetrosPorMinuto).toInt()

    return when {
        distanciaMetros < 50 -> "¡Llegando!"
        minutos < 1 -> "< 1 min"
        else -> "~$minutos min"
    }
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
                            val rolNormalizado = rolRecibido.trim().lowercase()
                            when (rolNormalizado) {
                                "conductor" -> pantallaActual = "CONDUCTOR"
                                "administrador" -> pantallaActual = "ADMINISTRADOR"
                                else -> pantallaActual = "PASAJERO"
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

                    "ADMINISTRADOR" -> PantallaAdministrador(
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
                                    // 2. Si Auth es exitoso, buscamos el PERFIL COMPLETO para evitar el caché
                                    database.getReference("usuarios").child(uid).get()
                                        .addOnSuccessListener { snapshot ->
                                            cargando = false

                                            // Extraemos el estado de la cuenta (true por defecto para mayor seguridad)
                                            val activo = snapshot.child("activo").getValue(Boolean::class.java) ?: true

                                            if (!activo) {
                                                // 🛑 BLOQUEO DE SEGURIDAD: Expulsión inmediata
                                                auth.signOut()
                                                Toast.makeText(context, "ACCESO DENEGADO: Cuenta suspendida", Toast.LENGTH_LONG).show()
                                            } else {
                                                // ✅ ACCESO PERMITIDO: Extraemos el rol fresco de la nube
                                                val rol = snapshot.child("rol").getValue(String::class.java) ?: "pasajero"
                                                Toast.makeText(context, "Bienvenido", Toast.LENGTH_SHORT).show()
                                                // 3. Enviamos el rol al enrutador de MainActivity
                                                onLoginExitoso(rol)
                                            }
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
            enabled = !cargando // Deshabilita el botón mientras carga
        ) {
            Text(if (cargando) "Validando..." else "Ingresar")
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onIrARegistro) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }



        // --- NUEVO SPRINT 8: MODO INVITADO ---
        Spacer(modifier = Modifier.height(16.dp))

        Text("o", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                // Saltamos la autenticación de Firebase y pasamos "invitado"
                onLoginExitoso("invitado")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar sin registrarse")
        }
        // ---- INICIO BOTÓN DE PRUEBA ----

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    try {
                        val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.probarConexion()
                        if (respuesta.isSuccessful) {
                            val datos = respuesta.body()
                            Toast.makeText(
                                context,
                                "✅ Backend dice: ${datos?.mensaje} (${datos?.autor})",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(context, " Error HTTP: ${respuesta.code()}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, " Fallo: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("RetrofitTest", "Error al conectar con backend", e)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🧪 Probar conexión con Backend")
        }

        // ---- FIN BOTÓN DE PRUEBA ----



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

    // --- NUEVO SPRINT 10: CÁMARA DEL MAPA TÁCTICO ---
    val popayan = LatLng(2.4419, -76.6063)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(popayan, 15f)
    }
    val mapProperties = MapProperties(isMyLocationEnabled = permisoConcedido)


    // --- NUEVO SPRINT 10: ESTADOS PARA LOS COMPAÑEROS DE RUTA ---
    var busesEnZona by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }
    // --- NUEVO SPRINT 10: ESTADOS PARA INCIDENCIAS ---
    var incidenciasActivas by remember { mutableStateOf<List<IncidenciaOficial>>(emptyList()) }
    val iconoBus = remember { obtenerIconoBus(context, R.drawable.ic_bus) }




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


    // --- NUEVO SPRINT 10: MOTOR DE RADAR (ESCUCHA A LOS COMPAÑEROS) ---
    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val prefijoCentro = calcularGeohash(popayan.latitude, popayan.longitude, 4)

        val query = database.getReference("BusesActivos")
            .orderByChild("geohash")
            .startAt(prefijoCentro)
            .endAt(prefijoCentro + "\uf8ff")

        query.keepSynced(true)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val busesRecibidos = mutableMapOf<String, LatLng>()
                for (busSnapshot in snapshot.children) {
                    val idBus = busSnapshot.key ?: continue

                    val latObj = busSnapshot.child("latitud").value
                    val lngObj = busSnapshot.child("longitud").value

                    val lat = (latObj as? Number)?.toDouble() ?: latObj?.toString()?.toDoubleOrNull()
                    val lng = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()?.toDoubleOrNull()

                    if (lat != null && lng != null) {
                        busesRecibidos[idBus] = LatLng(lat, lng)
                    }
                }
                busesEnZona = busesRecibidos
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error en radar del conductor", error.toException())
            }
        }
        query.addValueEventListener(listener)
        onDispose { query.removeEventListener(listener) }
    }

    // MOTOR DE TRANSMISIÓN: Se activa cuando 'transmitiendo' es true
// MOTOR DE TRANSMISIÓN: Se activa cuando 'transmitiendo' es true
    // --- NUEVO SPRINT 10: MOTOR DE ALERTAS EN LA VÍA ---
    DisposableEffect(Unit) {
        val refIncidencias = FirebaseDatabase.getInstance().getReference("incidencias")
        val listenerAlertas = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertas = mutableListOf<IncidenciaOficial>()
                for (hijo in snapshot.children) {
                    val inc = hijo.getValue(IncidenciaOficial::class.java)
                    // Solo mostramos las alertas que estén pendientes y tengan coordenadas
                    if (inc != null && inc.estado == "pendiente" && inc.latitud != 0.0) {
                        alertas.add(inc)
                    }
                }
                incidenciasActivas = alertas
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        refIncidencias.addValueEventListener(listenerAlertas)
        onDispose { refIncidencias.removeEventListener(listenerAlertas) }
    }







// --- NUEVA UI TÁCTICA DEL CONDUCTOR (SPRINT 10) ---
    Box(modifier = Modifier.fillMaxSize()) {

// 1. EL MAPA DE FONDO
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {
            // --- NUEVO SPRINT 10: DIBUJAR A LOS COMPAÑEROS ---
            busesEnZona.forEach { (idBus, destino) ->
                key(idBus) {
                    // Reutilizamos el componente inteligente del Sprint 9
                    MarcadorBusAnimado(
                        idBus = idBus,
                        destinoLatLng = destino,
                        icono = iconoBus,
                        textoEta = "Compañero" // Para el conductor, solo es un indicador de que es otro bus
                    )
                }
            }
            // --- NUEVO SPRINT 10: DIBUJAR INCIDENCIAS EN LA VÍA ---
            incidenciasActivas.forEach { incidencia ->
                Marker(
                    state = MarkerState(position = LatLng(incidencia.latitud, incidencia.longitud)),
                    title = "⚠️ Alerta Reportada",
                    snippet = incidencia.descripcion,
                    // Usamos un pin color naranja para que el conductor lo distinga de los paraderos
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }

        // 2. PANEL DE CONTROL FLOTANTE (Abajo)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (!enRecorrido) "Panel Táctico" else "🟢 Viaje en Curso",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (!enRecorrido) Color.Black else Color(0xFF006400) // Verde oscuro si está en viaje
                )
                Spacer(modifier = Modifier.height(16.dp))

                // LÓGICA VISUAL DEL RECORRIDO (Tus botones originales)
                if (!enRecorrido) {
// Botón para INICIAR
                    Button(
                        onClick = {
                            val uid = auth.currentUser?.uid ?: "CONDUCTOR_DESCONOCIDO"
                            val refBuses = database.getReference("buses")

                            // --- LA MAGIA: Buscamos qué bus tiene asignado este conductor ---
                            refBuses.orderByChild("id_conductor_actual").equalTo(uid).get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    // Encontramos el bus asignado al conductor
                                    val busSnapshot = snapshot.children.first()
                                    val busIdReal = busSnapshot.key ?: "BUS_DESCONOCIDO"
                                    val rutaIdReal = busSnapshot.child("id_ruta").getValue(String::class.java) ?: "RUTA_DESCONOCIDA"

                                    val refRecorridos = database.getReference("recorridos")
                                    val nuevoRecorridoRef = refRecorridos.push()
                                    val idGenerado = nuevoRecorridoRef.key

                                    // Armamos el viaje con los IDs VERDADEROS de la base de datos
                                    val nuevoRecorrido = RecorridoOficial(
                                        id_usuario = uid,
                                        id_bus = busIdReal, // <-- ¡DATO REAL!
                                        id_ruta = rutaIdReal, // <-- ¡DATO REAL!
                                        hora_inicio = System.currentTimeMillis(),
                                        hora_fin = null,
                                        estado = "en_curso"
                                    )

                                    nuevoRecorridoRef.setValue(nuevoRecorrido).addOnSuccessListener {
                                        idRecorridoActual = idGenerado
                                        enRecorrido = true
                                        transmitiendo = true
                                        ultimaUbicacion = "Transmitiendo en 2do plano... 🛡️"

                                        // Arrancamos el servicio pasándole el ID REAL del bus
                                        val intent = Intent(context, ServicioGpsConductor::class.java)
                                        intent.putExtra("ID_RECORRIDO", idGenerado)
                                        intent.putExtra("ID_BUS", busIdReal) // <-- LE ENVIAMOS EL ID AL SERVICIO
                                        ContextCompat.startForegroundService(context, intent)

                                        Toast.makeText(context, "Viaje Oficial Iniciado", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Si el conductor no tiene bus asignado, no lo dejamos transmitir a la deriva
                                    Toast.makeText(context, "No tienes ningún bus asignado en el sistema", Toast.LENGTH_LONG).show()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error buscando bus asignado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = permisoConcedido,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                    ) {
                        Text("▶️ Iniciar Recorrido Oficial")
                    }
                } else {
                    Button(
                        onClick = {
                            if (idRecorridoActual != null) {
                                val refRecorrido = database.getReference("recorridos").child(idRecorridoActual!!)
                                val actualizaciones = mapOf(
                                    "hora_fin" to ServerValue.TIMESTAMP,
                                    "estado" to "finalizado"
                                )

                                refRecorrido.updateChildren(actualizaciones).addOnSuccessListener {
                                    Toast.makeText(context, "Viaje Finalizado", Toast.LENGTH_SHORT).show()
                                    // Detener servicio invulnerable
                                    val intent = Intent(context, ServicioGpsConductor::class.java)
                                    context.stopService(intent)
                                }
                            }

                            enRecorrido = false
                            transmitiendo = false
                            idRecorridoActual = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🛑 Finalizar Recorrido")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de Cerrar Sesión
                Button(
                    onClick = {
                        transmitiendo = false
                        val intent = Intent(context, ServicioGpsConductor::class.java)
                        context.stopService(intent)
                        onCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar Sesión")
                }
            }
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

    // --- NUEVO SPRINT 9: ESTADO PARA GUARDAR LA UBICACIÓN DEL PASAJERO ---
    // Inicia en null porque al abrir la app aún no sabemos dónde está
    var ubicacionPasajero by remember { mutableStateOf<LatLng?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permisos ->
            permisoConcedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (permisoConcedido) {
                Toast.makeText(context, "Ubicando pasajero...", Toast.LENGTH_SHORT).show()
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val miUbicacion = LatLng(location.latitude, location.longitude)

                        // 1. GUARDAMOS LA UBICACIÓN EN LA MEMORIA DE COMPOSE
                        ubicacionPasajero = miUbicacion

                        // 2. CENTRAMOS LA CÁMARA (Tu código original)
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

    // ... (El resto del código hacia abajo, como LaunchedEffect, se mantiene idéntico)

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }



// 1. ESTADO PARA MÚLTIPLES BUSES (Corregido para ser 100% reactivo)
    var busesEnZona by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }
    val iconoBus = remember { obtenerIconoBus(context, R.drawable.ic_bus) }

    // --- NUEVO SPRINT 4: ESTADOS DE INFRAESTRUCTURA ---
    var rutasOficiales by remember { mutableStateOf<List<RutaOficial>>(emptyList()) }
    var paraderosOficiales by remember { mutableStateOf<List<ParaderoOficial>>(emptyList()) }

    // NUEVO ESTADO: Control de capas para la UI
    var mostrarRutas by remember { mutableStateOf(true) }

    // --- ESTADOS PARA INCIDENCIAS ---
    var mostrarDialogoIncidencia by remember { mutableStateOf(false) }
    val auth = remember { FirebaseAuth.getInstance() }
    // --- ESTADO PARA EL BUSCADOR ---
    var textoBusqueda by remember { mutableStateOf("") }

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
    // --- NUEVO SPRINT 4: MOTOR DE INFRAESTRUCTURA (Rutas y Paraderos) ---
    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val refRutas = database.getReference("rutas")
        val refParaderos = database.getReference("paraderos")

        // Listener para Paraderos
        val listenerParaderos = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaParaderos = mutableListOf<ParaderoOficial>()
                for (hijo in snapshot.children) {
                    val p = hijo.getValue(ParaderoOficial::class.java)
                    if (p != null) {
                        listaParaderos.add(p.copy(id = hijo.key ?: ""))
                    }
                }
                paraderosOficiales = listaParaderos
                Log.d("Infraestructura", "Descargados ${listaParaderos.size} paraderos.")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Infraestructura", "Error leyendo paraderos", error.toException())
            }
        }

// Listener para Rutas (A prueba de balas)
        val listenerRutas = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaRutas = mutableListOf<RutaOficial>()
                for (hijo in snapshot.children) {
                    val idRuta = hijo.key ?: ""
                    val nombre = hijo.child("nombre_ruta").getValue(String::class.java) ?: ""
                    val origen = hijo.child("origen").getValue(String::class.java) ?: ""
                    val destino = hijo.child("destino").getValue(String::class.java) ?: ""

                    // 1. Inmunidad a errores: forzamos minúsculas y quitamos espacios
                    val estado = hijo.child("estado").getValue(String::class.java)?.trim()?.lowercase() ?: "activa"

                    val geometriaNode = hijo.child("geometria")
                    val geometriaList = mutableListOf<CoordenadaRuta>()

                    for (coord in geometriaNode.children) {
                        // 2. Extracción segura: lee igual si es texto o número en la base de datos
                        val latObj = coord.child("latitud").value
                        val lngObj = coord.child("longitud").value
                        val lat = (latObj as? Number)?.toDouble() ?: latObj?.toString()?.toDoubleOrNull() ?: 0.0
                        val lng = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()?.toDoubleOrNull() ?: 0.0

                        // Solo agregamos puntos que no sean cero
                        if(lat != 0.0 && lng != 0.0){
                            geometriaList.add(CoordenadaRuta(lat, lng))
                        }
                    }

                    // 3. Log espía para ver qué encontró exactamente
                    Log.d("Infraestructura", "-> Ruta: $nombre | Estado: $estado | Puntos válidos: ${geometriaList.size}")

                    listaRutas.add(RutaOficial(
                        id = idRuta,
                        nombre_ruta = nombre,
                        origen = origen,
                        destino = destino,
                        estado = estado,
                        geometria = geometriaList
                    ))
                }
                rutasOficiales = listaRutas
                Log.d("Infraestructura", "Descargadas ${listaRutas.size} rutas.")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Infraestructura", "Error leyendo rutas", error.toException())
            }
        }

        refParaderos.addValueEventListener(listenerParaderos)
        refRutas.addValueEventListener(listenerRutas)

        onDispose {
            refParaderos.removeEventListener(listenerParaderos)
            refRutas.removeEventListener(listenerRutas)
        }
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
            // --- NUEVO SPRINT 4: RENDERIZADO DE RUTAS OFICIALES (Polylines) ---
            if (mostrarRutas) {
                rutasOficiales.forEach { ruta ->
                    if (ruta.estado == "activa" && ruta.geometria.isNotEmpty()) {
                        val puntosLinea = ruta.geometria.map { LatLng(it.latitud, it.longitud) }
                        com.google.maps.android.compose.Polyline(
                            points = puntosLinea,
                            color = Color.Red,
                            width = 25f
                        )
                    }
                }
            }


// --- NUEVO SPRINT 4: RENDERIZADO DE PARADEROS OFICIALES ---
            paraderosOficiales.forEach { paradero ->
                // El diccionario de datos exige que el estado sea "activo"
                if (paradero.estado == "activo") {
                    Marker(
                        state = MarkerState(position = LatLng(paradero.latitud, paradero.longitud)),
                        title = paradero.nombre,
                        snippet = paradero.direccion,
                        // Pintamos el paradero de color verde para distinguirlo en la interfaz
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
            }

// 4. DIBUJAMOS TODOS LOS BUSES ACTIVOS SIMULTÁNEAMENTE
            busesEnZona.forEach { (idBus, destino) ->
                key(idBus) {
                    // --- NUEVO SPRINT 9: CÁLCULO DE ETA EN TIEMPO REAL ---
                    val eta = if (ubicacionPasajero != null) {
                        // Medimos la distancia entre el pasajero y este bus específico
                        val distancia = calcularDistanciaMetros(ubicacionPasajero!!, destino)
                        obtenerTextoETA(distancia)
                    } else {
                        "Calculando..."
                    }

                    MarcadorBusAnimado(
                        idBus = idBus,
                        destinoLatLng = destino,
                        icono = iconoBus,
                        textoEta = eta // Le inyectamos el tiempo estimado al marcador
                    )
                }
            }

        }

// 2. EL BUSCADOR Y EL RADAR (TopCenter)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra de Búsqueda
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("¿A dónde vas?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp)),
                singleLine = true,
                trailingIcon = {
                    TextButton(
                        onClick = {
                            if (textoBusqueda.isNotEmpty()) {
                                // 1. Conexión a Firebase para Historial
                                val database = FirebaseDatabase.getInstance()
                                val refHistorial = database.getReference("historial_busquedas")
                                val uid = auth.currentUser?.uid ?: ""

                                // 2. Armar paquete de captura silenciosa
                                val nuevaBusqueda = HistorialBusquedaOficial(
                                    id_usuario = uid,
                                    destino_buscado = textoBusqueda,
                                    rutas_resultado = emptyMap(), // Queda vacío por ahora hasta el motor de búsqueda final
                                    fecha_busqueda = System.currentTimeMillis()
                                )

                                // 3. Guardar en BD y limpiar buscador
                                refHistorial.push().setValue(nuevaBusqueda)
                                Toast.makeText(context, "Buscando rutas hacia: $textoBusqueda", Toast.LENGTH_SHORT).show()
                                textoBusqueda = ""
                            }
                        }
                    ) {
                        Text("🔍 Buscar")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // El Radar original (ahora debajo del buscador)
            Text(
                text = "Buses en radar: ${busesEnZona.size}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Red,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        // --- NUEVO SPRINT 4: INTERRUPTOR FLOTANTE PARA OCULTAR RUTAS ---
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ver Rutas", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = mostrarRutas,
                onCheckedChange = { mostrarRutas = it }
            )
        }
        // LÓGICA DEL DIÁLOGO DE INCIDENCIAS
        if (mostrarDialogoIncidencia) {
            DialogoReportarIncidencia(
                onDismiss = { mostrarDialogoIncidencia = false },
                onEnviar = { descripcion ->
                    val database = FirebaseDatabase.getInstance()
                    val refIncidencias = database.getReference("incidencias")
                    // Extraemos el UID si está logueado, sino va vacío (para anónimos)
                    val uid = auth.currentUser?.uid ?: ""

                    val nuevaIncidencia = IncidenciaOficial(
                        id_usuario = uid,
                        descripcion = descripcion,
                        estado = "pendiente",
                        fecha_reporte = System.currentTimeMillis()
                    )

                    refIncidencias.push().setValue(nuevaIncidencia).addOnSuccessListener {
                        Toast.makeText(context, "Incidencia reportada. ¡Gracias!", Toast.LENGTH_SHORT).show()
                        mostrarDialogoIncidencia = false
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error al enviar reporte", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

// LÓGICA DEL DIÁLOGO DE INCIDENCIAS
        if (mostrarDialogoIncidencia) {
            DialogoReportarIncidencia(
                onDismiss = { mostrarDialogoIncidencia = false },
                onEnviar = { descripcion ->
                    val database = FirebaseDatabase.getInstance()
                    val refIncidencias = database.getReference("incidencias")
                    val uid = auth.currentUser?.uid ?: ""

                    // Extraemos la ubicación del pasajero (o usamos Popayán por defecto si falla)
                    val lat = ubicacionPasajero?.latitude ?: popayan.latitude
                    val lng = ubicacionPasajero?.longitude ?: popayan.longitude

                    val nuevaIncidencia = IncidenciaOficial(
                        id_usuario = uid,
                        descripcion = descripcion,
                        estado = "pendiente",
                        fecha_reporte = System.currentTimeMillis(),
                        latitud = lat, // Guardamos la coordenada en Firebase
                        longitud = lng
                    )

                    refIncidencias.push().setValue(nuevaIncidencia).addOnSuccessListener {
                        Toast.makeText(context, "Incidencia reportada. ¡Gracias!", Toast.LENGTH_SHORT).show()
                        mostrarDialogoIncidencia = false
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error al enviar reporte", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // --- NUEVO SPRINT 8: RESTRICCIÓN PARA INVITADOS ---
        // El botón de reportar solo aparece si hay un usuario autenticado
        if (auth.currentUser != null) {
            Button(
                onClick = { mostrarDialogoIncidencia = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text("⚠️ Reportar")
            }
        }

        // 3. El Botón de salida dinámico
        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(if (auth.currentUser != null) "Cerrar Sesión" else "Salir del Mapa")
        }
    } // Fin del Box
} // Fin de la función MapaPopabus


@Composable
fun PantallaAdministrador(onCerrarSesion: () -> Unit) {
    // Memoria interna para navegar dentro del panel de admin sin tocar el MainActivity
    var subPantalla by remember { mutableStateOf("MENU") }

    when (subPantalla) {
        "MENU" -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Panel de Administración", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))

                // --- NUEVO SPRINT 11: BOTÓN DE GESTIÓN HUMANA ---
                Button(onClick = { subPantalla = "USUARIOS" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("👥 Gestión Humana")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Al hacer clic, cambiamos a la pantalla de paraderos
                Button(onClick = { subPantalla = "PARADEROS" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("🚌 Gestionar Paraderos")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { subPantalla = "RUTAS" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("🗺️ Gestionar Rutas")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { subPantalla = "BUSES" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("🚐 Gestionar Buses")
                }
                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onCerrarSesion,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cerrar Sesión")
                }
            }
        }
        "PARADEROS" -> PantallaGestionParaderos(onVolver = { subPantalla = "MENU" })
        "RUTAS" -> PantallaGestionRutas(onVolver = { subPantalla = "MENU" })
        "BUSES" -> PantallaGestionBuses(onVolver = { subPantalla = "MENU" })

        // --- NUEVO SPRINT 11: RUTA A GESTIÓN DE USUARIOS ---
        "USUARIOS" -> PantallaGestionUsuarios(onVolver = { subPantalla = "MENU" })
    }
}

@Composable
fun PantallaGestionParaderos(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refParaderos = database.getReference("paraderos")

    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }

    // Estado para la lista en tiempo real
    var listaParaderos by remember { mutableStateOf<List<ParaderoOficial>>(emptyList()) }

    // Descargamos los paraderos de Firebase en tiempo real
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val paraderos = mutableListOf<ParaderoOficial>()
                for (hijo in snapshot.children) {
                    val p = hijo.getValue(ParaderoOficial::class.java)
                    if (p != null) {
                        paraderos.add(p.copy(id = hijo.key ?: ""))
                    }
                }
                listaParaderos = paraderos
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        refParaderos.addValueEventListener(listener)
        onDispose { refParaderos.removeEventListener(listener) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de Paraderos", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Formulario alineado a tu diccionario de datos
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre (Ej: Parque Caldas)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = latitud, onValueChange = { latitud = it }, label = { Text("Latitud (Ej: 2.4419)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = longitud, onValueChange = { longitud = it }, label = { Text("Longitud (Ej: -76.6063)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val lat = latitud.toDoubleOrNull()
                val lng = longitud.toDoubleOrNull()

                if (nombre.isNotEmpty() && lat != null && lng != null) {
                    val nuevoParaderoRef = refParaderos.push() // Firebase genera el _id
                    val geohash = calcularGeohash(lat, lng) // Reutilizamos tu algoritmo nativo

                    val nuevoParadero = ParaderoOficial(
                        nombre = nombre,
                        direccion = direccion,
                        latitud = lat,
                        longitud = lng,
                        geohash = geohash,
                        estado = "activo"
                    )

                    nuevoParaderoRef.setValue(nuevoParadero).addOnSuccessListener {
                        Toast.makeText(context, "Paradero Guardado", Toast.LENGTH_SHORT).show()
                        nombre = ""
                        direccion = ""
                        latitud = ""
                        longitud = ""
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Llene nombre y coordenadas numéricas válidas", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Crear Paradero")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Paraderos Registrados: ${listaParaderos.size}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Lista visual para ver lo que hay en Firebase
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(listaParaderos) { paradero ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📍 ${paradero.nombre}", style = MaterialTheme.typography.titleMedium)
                        Text("Dir: ${paradero.direccion}", style = MaterialTheme.typography.bodyMedium)
                        Text("Geo: ${paradero.latitud}, ${paradero.longitud}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
            Text("Volver al Menú")
        }
    }
}

@Composable
fun PantallaGestionRutas(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refRutas = database.getReference("rutas")

    // Estados para el formulario
    var nombreRuta by remember { mutableStateOf("") }
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }

    // Estado para la lista en tiempo real
    var listaRutas by remember { mutableStateOf<List<RutaOficial>>(emptyList()) }

    // Descargamos las rutas de Firebase en tiempo real
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rutas = mutableListOf<RutaOficial>()
                for (hijo in snapshot.children) {
                    val r = hijo.getValue(RutaOficial::class.java)
                    if (r != null) {
                        rutas.add(r.copy(id = hijo.key ?: ""))
                    }
                }
                listaRutas = rutas
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        refRutas.addValueEventListener(listener)
        onDispose { refRutas.removeEventListener(listener) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de Rutas", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Formulario alineado a tu diccionario de datos
        OutlinedTextField(value = nombreRuta, onValueChange = { nombreRuta = it }, label = { Text("Nombre (Ej: Línea 1)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = origen, onValueChange = { origen = it }, label = { Text("Origen (Ej: Norte)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = destino, onValueChange = { destino = it }, label = { Text("Destino (Ej: Centro)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (nombreRuta.isNotEmpty() && origen.isNotEmpty() && destino.isNotEmpty()) {
                    val nuevaRutaRef = refRutas.push() // Firebase genera el _id

                    val nuevaRuta = RutaOficial(
                        nombre_ruta = nombreRuta,
                        origen = origen,
                        destino = destino,
                        estado = "activa"
                        // paraderos y geometria inician vacíos, listos para ser llenados luego
                    )

                    nuevaRutaRef.setValue(nuevaRuta).addOnSuccessListener {
                        Toast.makeText(context, "Ruta Creada", Toast.LENGTH_SHORT).show()
                        nombreRuta = ""
                        origen = ""
                        destino = ""
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Crear Ruta Base")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Rutas Registradas: ${listaRutas.size}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Lista visual para ver lo que hay en Firebase
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(listaRutas) { ruta ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🗺️ ${ruta.nombre_ruta}", style = MaterialTheme.typography.titleMedium)
                        Text("De: ${ruta.origen} ➔ A: ${ruta.destino}", style = MaterialTheme.typography.bodyMedium)
                        Text("Estado: ${ruta.estado}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
            Text("Volver al Menú")
        }
    }
}


@Composable
fun PantallaGestionBuses(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refBuses = database.getReference("buses")

    // Estados para el formulario alineados a tu diccionario de datos
    var placa by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var idRuta by remember { mutableStateOf("") }
    var idConductor by remember { mutableStateOf("") }

    // Estado para la lista en tiempo real
    var listaBuses by remember { mutableStateOf<List<BusOficial>>(emptyList()) }

    // Descargamos los buses de Firebase en tiempo real
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val buses = mutableListOf<BusOficial>()
                for (hijo in snapshot.children) {
                    val b = hijo.getValue(BusOficial::class.java)
                    if (b != null) {
                        buses.add(b.copy(id = hijo.key ?: ""))
                    }
                }
                listaBuses = buses
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        refBuses.addValueEventListener(listener)
        onDispose { refBuses.removeEventListener(listener) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de Vehículos", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Formulario alineado a tu diccionario de datos
        OutlinedTextField(value = placa, onValueChange = { placa = it }, label = { Text("Placa (Ej: TJU-123)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = modelo, onValueChange = { modelo = it }, label = { Text("Modelo/Año") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = idRuta, onValueChange = { idRuta = it }, label = { Text("ID de la Ruta (Opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = idConductor, onValueChange = { idConductor = it }, label = { Text("UID del Conductor (Opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (placa.isNotEmpty()) {
                    val nuevoBusRef = refBuses.push() // Firebase genera el _id

                    val nuevoBus = BusOficial(
                        placa = placa,
                        modelo = modelo,
                        id_ruta = idRuta,
                        id_conductor_actual = idConductor,
                        estado = "activo"
                    )

                    nuevoBusRef.setValue(nuevoBus).addOnSuccessListener {
                        Toast.makeText(context, "Bus Registrado", Toast.LENGTH_SHORT).show()
                        placa = ""
                        modelo = ""
                        idRuta = ""
                        idConductor = ""
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "La placa es obligatoria", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Registrar Bus")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Vehículos Registrados: ${listaBuses.size}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Lista visual para ver lo que hay en Firebase
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(listaBuses) { bus ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🚐 Placa: ${bus.placa}", style = MaterialTheme.typography.titleMedium)
                        Text("Modelo: ${bus.modelo} | Estado: ${bus.estado}", style = MaterialTheme.typography.bodyMedium)
                        Text("Ruta Asignada: ${bus.id_ruta.ifEmpty { "Ninguna" }}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("Conductor UID: ${bus.id_conductor_actual.ifEmpty { "Ninguno" }}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
            Text("Volver al Menú")
        }
    }
}


@Composable
fun DialogoReportarIncidencia(onDismiss: () -> Unit, onEnviar: (String) -> Unit) {
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚠️ Reportar Incidencia") },
        text = {
            Column {
                Text("Describe el problema (accidente, trancón, desvío, etc.):", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Detalles...") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (descripcion.isNotEmpty()) onEnviar(descripcion) }) {
                Text("Enviar Reporte")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
// --- NUEVA PANTALLA PARA EL SPRINT 11 ---
// --- PANTALLA GESTIÓN DE USUARIOS (SPRINT 11) ---
// --- PANTALLA GESTIÓN DE USUARIOS (SPRINT 11) ---
@Composable
fun PantallaGestionUsuarios(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refUsuarios = database.getReference("usuarios")

    // Estado para la lista de usuarios en tiempo real
    var listaUsuarios by remember { mutableStateOf<List<UsuarioOficial>>(emptyList()) }

    // Descargamos los usuarios de Firebase en tiempo real
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuarios = mutableListOf<UsuarioOficial>()
                for (hijo in snapshot.children) {
                    val u = hijo.getValue(UsuarioOficial::class.java)
                    if (u != null) {
                        usuarios.add(u.copy(id = hijo.key ?: ""))
                    }
                }
                listaUsuarios = usuarios
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        refUsuarios.addValueEventListener(listener)
        onDispose { refUsuarios.removeEventListener(listener) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión Humana", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Usuarios Registrados: ${listaUsuarios.size}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Lista visual con BOTONES DE EDICIÓN
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(listaUsuarios) { usuario ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("👤 ${usuario.nombre_completo}", style = MaterialTheme.typography.titleMedium)
                        Text("Rol: ${usuario.rol.uppercase()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Estado: ${if (usuario.activo) "🟢 Activo" else "🔴 Suspendido"}", style = MaterialTheme.typography.bodySmall)
                        Text("UID: ${usuario.id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- NUEVOS BOTONES DE ACCIÓN (UPDATE) ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 1. Botón para Suspender / Activar
                            OutlinedButton(
                                onClick = {
                                    val nuevoEstado = !usuario.activo // Invertimos el estado actual
                                    refUsuarios.child(usuario.id).child("activo").setValue(nuevoEstado)
                                        .addOnSuccessListener {
                                            val msj = if (nuevoEstado) "Cuenta Activada" else "Cuenta Suspendida"
                                            Toast.makeText(context, msj, Toast.LENGTH_SHORT).show()
                                        }
                                }
                            ) {
                                Text(if (usuario.activo) "🔴 Suspender" else "🟢 Activar")
                            }

                            // 2. Botón para Cambiar Rol
                            OutlinedButton(
                                onClick = {
                                    // 🛑 REGLA DE SEGURIDAD CORPORATIVA: No se toca a los administradores
                                    if (usuario.rol.lowercase() == "administrador") {
                                        Toast.makeText(context, "SEGURIDAD: Los administradores solo se gestionan desde la consola web", Toast.LENGTH_LONG).show()
                                    } else {
                                        // ✅ Ciclo seguro: solo alterna entre pasajero y conductor
                                        val nuevoRol = if (usuario.rol.lowercase() == "pasajero") "conductor" else "pasajero"

                                        refUsuarios.child(usuario.id).child("rol").setValue(nuevoRol)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Rol cambiado a $nuevoRol", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            ) {
                                Text("🔄 Rol")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
            Text("Volver al Menú")
        }
    }
}