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
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.clickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material3.OutlinedButton
import android.location.Location
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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

data class ActividadUsuario(
    val id_actividad: String = "",
    val id_bus: String = "",
    val id_ruta: String = "",
    val fecha_abordaje: Long = 0L,
    val metodo_registro: String = "manual" // Puede ser "manual" o "automatico"
)


// Representa una consulta realizada por un pasajero (Colección 'historial_busquedas')
data class HistorialBusquedaOficial(
    val id_usuario: String = "",
    val destino_buscado: String = "",
    val rutas_resultado: Map<String, Boolean> = emptyMap(),
    val fecha_busqueda: Long = 0L
)

data class InformeDiario(
    val id_recorrido: String = "",
    val id_conductor: String = "",
    val tiempo_total_minutos: Long = 0L,
    val total_alertas_emitidas: Int = 0,
    val fecha_cierre: Long = 0L
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

fun obtenerIconoBusGris(context: Context, vectorResId: Int): BitmapDescriptor? {
    MapsInitializer.initialize(context)
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    
    val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Aplicamos la matriz de saturación cero para volver el ícono blanco y negro
    val paint = android.graphics.Paint()
    val matrix = android.graphics.ColorMatrix()
    matrix.setSaturation(0f)
    paint.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
    
    // Dibujamos el vector usando una capa con el filtro de color aplicado
    canvas.saveLayer(null, paint)
    vectorDrawable.draw(canvas)
    canvas.restore()
    
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

// Enum centralizado para la gestión robusta de roles
enum class RolUsuario(val key: String) {
    PASAJERO("pasajero"),
    CONDUCTOR("conductor"),
    ADMINISTRADOR("administrador");

    companion object {
        fun desdeString(raw: String?): RolUsuario {
            if (raw.isNullOrBlank()) return PASAJERO
            val cleaned = raw.trim().lowercase()
            return when (cleaned) {
                "conductor", "driver", "chofer" -> CONDUCTOR
                "administrador", "admin" -> ADMINISTRADOR
                "pasajero", "passenger", "user" -> PASAJERO
                else -> PASAJERO
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.d("FirebaseOffline", "La memoria local ya estaba encendida")
        }

        setContent {
            PopaBusTheme {
                var pantallaActual by remember { mutableStateOf("LOGIN") }

                when (pantallaActual) {
                    "LOGIN" -> PantallaLogin(
                        onIrARegistro = { pantallaActual = "REGISTRO" },
                        onLoginExitoso = { rolRecibido ->
                            val rolEnum = RolUsuario.desdeString(rolRecibido)
                            when (rolEnum) {
                                RolUsuario.CONDUCTOR -> pantallaActual = "CONDUCTOR"
                                RolUsuario.ADMINISTRADOR -> pantallaActual = "ADMINISTRADOR"
                                RolUsuario.PASAJERO -> pantallaActual = "PASAJERO"
                            }
                        }
                    )
                    "REGISTRO" -> PantallaRegistro(
                        onVolverLogin = { pantallaActual = "LOGIN" },
                        onRegistroExitoso = { uidGenerado, nombreIngresado ->
                            val database = FirebaseDatabase.getInstance()
                            val refUsuario = database.getReference("usuarios").child(uidGenerado)

                            val datosUsuario = mapOf(
                                "nombre_completo" to nombreIngresado,
                                "rol" to RolUsuario.PASAJERO.key,
                                "activo" to true,
                                "fecha_registro" to ServerValue.TIMESTAMP
                            )

                            refUsuario.setValue(datosUsuario).addOnCompleteListener { tarea ->
                                if (tarea.isSuccessful) {
                                    // Pasa directamente al mapa de Pasajero sin preguntar el rol dos veces
                                    pantallaActual = "PASAJERO"
                                }
                            }
                        }
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
fun PantallaLogin(onIrARegistro: () -> Unit, onLoginExitoso: (String) -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

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
                    auth.signInWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    database.getReference("usuarios").child(uid).get()
                                        .addOnSuccessListener { snapshot ->
                                            cargando = false
                                            if (!snapshot.exists()) {
                                                // Autorrecuperación si no existe el nodo /usuarios/{uid}
                                                database.getReference("buses").orderByChild("id_conductor_actual").equalTo(uid).get()
                                                    .addOnSuccessListener { busSnap ->
                                                        val esConductor = busSnap.exists()
                                                        val rolDefinido = if (esConductor) "conductor" else "pasajero"
                                                        val datosAuto = mapOf(
                                                            "nombre_completo" to (auth.currentUser?.email?.substringBefore("@") ?: "Usuario"),
                                                            "rol" to rolDefinido,
                                                            "activo" to true,
                                                            "fecha_registro" to ServerValue.TIMESTAMP
                                                        )
                                                        database.getReference("usuarios").child(uid).setValue(datosAuto)
                                                        Toast.makeText(context, "Bienvenido", Toast.LENGTH_SHORT).show()
                                                        onLoginExitoso(rolDefinido)
                                                    }
                                                    .addOnFailureListener {
                                                        onLoginExitoso("pasajero")
                                                    }
                                            } else {
                                                val activo = snapshot.child("activo").getValue(Boolean::class.java) ?: true
                                                if (!activo) {
                                                    auth.signOut()
                                                    Toast.makeText(context, "ACCESO DENEGADO: Cuenta suspendida", Toast.LENGTH_LONG).show()
                                                } else {
                                                    val rolRaw = snapshot.child("rol").getValue(String::class.java)
                                                    val rolEnum = RolUsuario.desdeString(rolRaw)
                                                    Toast.makeText(context, "Bienvenido", Toast.LENGTH_SHORT).show()
                                                    onLoginExitoso(rolEnum.key)
                                                }
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
            enabled = !cargando
        ) {
            Text(if (cargando) "Validando..." else "Ingresar")
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onIrARegistro) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("o", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                onLoginExitoso("pasajero")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar sin registrarse")
        }
    }
}


@Composable
fun PantallaRegistro(onVolverLogin: () -> Unit, onRegistroExitoso: (String, String) -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Cuenta de Pasajero", style = MaterialTheme.typography.headlineMedium)
        Text("Regístrate para consultar rutas y buses en tiempo real", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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

        Button(
            onClick = {
                if (correo.isNotEmpty() && contrasena.isNotEmpty() && nombre.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                Toast.makeText(context, "¡Usuario creado exitosamente!", Toast.LENGTH_SHORT).show()
                                onRegistroExitoso(user?.uid ?: "", nombre)
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

    // --- ESTADOS PARA INFORME DIARIO ---
    var horaInicioViaje by remember { mutableStateOf(0L) }
    var contadorAlertas by remember { mutableStateOf(0) }

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
            permisoConcedido = (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true) || 
                           (permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
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

                            Log.e("DepuradorBuses", "INICIANDO CONSULTA: Buscando id_conductor_actual == [$uid]")

                            refBuses.orderByChild("id_conductor_actual").equalTo(uid).get().addOnSuccessListener { snapshot ->
                                Log.e("DepuradorBuses", "RESPUESTA FIREBASE: snapshot.exists() = ${snapshot.exists()}, childrenCount = ${snapshot.childrenCount}")
                                
                                if (snapshot.exists()) {
                                    // Encontramos el bus asignado al conductor
                                    val busSnapshot = snapshot.children.first()
                                    val busIdReal = busSnapshot.key ?: "BUS_DESCONOCIDO"
                                    val rutaIdReal = busSnapshot.child("id_ruta").getValue(String::class.java) ?: "RUTA_DESCONOCIDA"

                                    val refRecorridos = database.getReference("recorridos")
                                    val nuevoRecorridoRef = refRecorridos.push()
                                    val idGenerado = nuevoRecorridoRef.key

                                    val nuevoRecorrido = RecorridoOficial(
                                        id_usuario = uid,
                                        id_bus = busIdReal,
                                        id_ruta = rutaIdReal,
                                        hora_inicio = System.currentTimeMillis(),
                                        hora_fin = null,
                                        estado = "en_curso"
                                    )

                                    nuevoRecorridoRef.setValue(nuevoRecorrido).addOnSuccessListener {
                                        idRecorridoActual = idGenerado
                                        enRecorrido = true
                                        transmitiendo = true
                                        ultimaUbicacion = "Transmitiendo en 2do plano... 🛡️"
                                        horaInicioViaje = System.currentTimeMillis()
                                        contadorAlertas = 0

                                        val intent = Intent(context, ServicioGpsConductor::class.java)
                                        intent.putExtra("ID_RECORRIDO", idGenerado)
                                        intent.putExtra("ID_BUS", busIdReal)
                                        ContextCompat.startForegroundService(context, intent)

                                        Toast.makeText(context, "Viaje Oficial Iniciado", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Log.e("DepuradorBuses", "RESULTADO CERO: Firebase dice que no existe un bus con conductor = [$uid]")
                                    Toast.makeText(context, "0 resultados encontrados para este conductor.", Toast.LENGTH_LONG).show()
                                }
                            }.addOnFailureListener { excepcion ->
                                Log.e("DepuradorBuses", "FALLO CATASTRÓFICO BASE DE DATOS", excepcion)
                                Toast.makeText(context, "Fallo: ${excepcion.message}", Toast.LENGTH_LONG).show()
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
                                val uidConductor = auth.currentUser?.uid ?: "desconocido"
                                val tiempoActual = System.currentTimeMillis()

                                // 1. CERRAR EL VIAJE ORIGINAL (Para no dañar el historial)
                                val refRecorrido = database.getReference("recorridos").child(idRecorridoActual!!)
                                val actualizaciones = mapOf(
                                    "hora_fin" to tiempoActual,
                                    "estado" to "finalizado"
                                )
                                refRecorrido.updateChildren(actualizaciones)

                                // 2. CÁLCULO MATEMÁTICO E INFORME DIARIO (Evitando división por cero o nulos)
                                val minutosTotales = if (horaInicioViaje > 0L) {
                                    (tiempoActual - horaInicioViaje) / 60000
                                } else {
                                    0L
                                }

                                val nuevoInforme = InformeDiario(
                                    id_recorrido = idRecorridoActual!!,
                                    id_conductor = uidConductor,
                                    tiempo_total_minutos = minutosTotales,
                                    total_alertas_emitidas = contadorAlertas,
                                    fecha_cierre = tiempoActual
                                )

                                // 3. ENVIAR EL INFORME A LA NUEVA COLECCIÓN
                                database.getReference("informes_diarios").push().setValue(nuevoInforme)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Cierre exitoso. Informe generado.", Toast.LENGTH_LONG).show()
                                    }

                                // 4. APAGAR EL ESCUDO GPS NATIVO
                                val intent = Intent(context, ServicioGpsConductor::class.java)
                                context.stopService(intent)
                            }

                            // 5. VACIAR LA MEMORIA LOCAL
                            enRecorrido = false
                            transmitiendo = false
                            ultimaUbicacion = "Viaje finalizado."
                            idRecorridoActual = null
                            horaInicioViaje = 0L
                            contadorAlertas = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                    ) {
                        Text("🛑 Finalizar Recorrido")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- PANEL DE REPORTES DEL CONDUCTOR ---
                Text("Reportar Incidencia en la Vía", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                contadorAlertas++
                                val uid = auth.currentUser?.uid ?: ""
                                val nuevaIncidencia = IncidenciaOficial(
                                    id_usuario = uid,
                                    descripcion = "Trancón",
                                    estado = "pendiente",
                                    fecha_reporte = System.currentTimeMillis()
                                )
                                database.getReference("incidencias").push().setValue(nuevaIncidencia).addOnSuccessListener {
                                    Toast.makeText(context, "Reporte enviado: Trancón", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Trancón")
                        }
                        
                        Button(
                            onClick = {
                                contadorAlertas++
                                val uid = auth.currentUser?.uid ?: ""
                                val nuevaIncidencia = IncidenciaOficial(
                                    id_usuario = uid,
                                    descripcion = "Desvío",
                                    estado = "pendiente",
                                    fecha_reporte = System.currentTimeMillis()
                                )
                                database.getReference("incidencias").push().setValue(nuevaIncidencia).addOnSuccessListener {
                                    Toast.makeText(context, "Reporte enviado: Desvío", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)) // Amarillo
                        ) {
                            Text("Desvío")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                contadorAlertas++
                                val uid = auth.currentUser?.uid ?: ""
                                val nuevaIncidencia = IncidenciaOficial(
                                    id_usuario = uid,
                                    descripcion = "Fallo",
                                    estado = "pendiente",
                                    fecha_reporte = System.currentTimeMillis()
                                )
                                database.getReference("incidencias").push().setValue(nuevaIncidencia).addOnSuccessListener {
                                    Toast.makeText(context, "Reporte enviado: Fallo", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Naranja
                        ) {
                            Text("Fallo")
                        }

                        Button(
                            onClick = {
                                contadorAlertas++
                                val uid = auth.currentUser?.uid ?: ""
                                val nuevaIncidencia = IncidenciaOficial(
                                    id_usuario = uid,
                                    descripcion = "Pánico",
                                    estado = "urgente",
                                    fecha_reporte = System.currentTimeMillis()
                                )
                                database.getReference("incidencias").push().setValue(nuevaIncidencia).addOnSuccessListener {
                                    Toast.makeText(context, "Alerta enviada: Pánico", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
                        ) {
                            Text("Pánico")
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapaPopabus(onCerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var permisoConcedido by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // ESTADO PARA LA VISTA ACTUAL DE AYUDA (Sugerencias, Acerca De, Preguntas)
    var vistaAyudaActual by remember { mutableStateOf<String?>(null) }

    // ESTADO PARA EL BOTÓN DE ABORDAJE MANUAL DEL PASAJERO
    var rastreandoAbordaje by remember { mutableStateOf(false) }

    val popayan = LatLng(2.4419, -76.6063)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(popayan, 14f)
    }

    // --- NUEVO SPRINT 9: ESTADO PARA GUARDAR LA UBICACIÓN DEL PASAJERO ---
    var ubicacionPasajero by remember { mutableStateOf<LatLng?>(null) }
    var mostrarDialogoPanico by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permisos ->
            permisoConcedido = (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true) || 
                           (permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
            if (permisoConcedido) {
                Toast.makeText(context, "Ubicando pasajero...", Toast.LENGTH_SHORT).show()
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val miUbicacion = LatLng(location.latitude, location.longitude)

                        ubicacionPasajero = miUbicacion

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

    var busesEnZona by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }
    val iconoBus = remember { obtenerIconoBus(context, R.drawable.ic_bus) }

    var rutasOficiales by remember { mutableStateOf<List<RutaOficial>>(emptyList()) }
    var paraderosOficiales by remember { mutableStateOf<List<ParaderoOficial>>(emptyList()) }

    var mostrarRutas by remember { mutableStateOf(true) }

    var mostrarDialogoIncidencia by remember { mutableStateOf(false) }
    val auth = remember { FirebaseAuth.getInstance() }
    var textoBusqueda by remember { mutableStateOf("") }

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

                    val latObj = busSnapshot.child("latitud").value
                    val lngObj = busSnapshot.child("longitud").value
                    val geohashBus = busSnapshot.child("geohash").value

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

    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val refRutas = database.getReference("rutas")
        val refParaderos = database.getReference("paraderos")

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

        val listenerRutas = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaRutas = mutableListOf<RutaOficial>()
                for (hijo in snapshot.children) {
                    val idRuta = hijo.key ?: ""
                    val nombre = hijo.child("nombre_ruta").getValue(String::class.java)
                        ?: hijo.child("nombre").getValue(String::class.java)
                        ?: ""
                    val origen = hijo.child("origen").getValue(String::class.java) ?: ""
                    val destino = hijo.child("destino").getValue(String::class.java) ?: ""

                    val estadoRaw = hijo.child("estado").getValue(String::class.java)?.trim()?.lowercase() ?: "activa"

                    val geometriaNode = if (hijo.child("geometria").exists()) hijo.child("geometria")
                    else if (hijo.child("puntos").exists()) hijo.child("puntos")
                    else if (hijo.child("coordenadas").exists()) hijo.child("coordenadas")
                    else hijo.child("geometria")

                    val geometriaList = mutableListOf<CoordenadaRuta>()

                    for (coord in geometriaNode.children) {
                        val latObj = coord.child("latitud").value
                            ?: coord.child("latitude").value
                            ?: coord.child("lat").value
                            ?: (coord.value as? Map<*, *>)?.get("latitud")
                            ?: (coord.value as? Map<*, *>)?.get("latitude")
                            ?: (coord.value as? Map<*, *>)?.get("lat")
                            ?: (coord.value as? List<*>)?.getOrNull(0)

                        val lngObj = coord.child("longitud").value
                            ?: coord.child("longitude").value
                            ?: coord.child("lng").value
                            ?: (coord.value as? Map<*, *>)?.get("longitud")
                            ?: (coord.value as? Map<*, *>)?.get("longitude")
                            ?: (coord.value as? Map<*, *>)?.get("lng")
                            ?: (coord.value as? List<*>)?.getOrNull(1)

                        val lat = (latObj as? Number)?.toDouble() ?: latObj?.toString()?.toDoubleOrNull() ?: 0.0
                        val lng = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()?.toDoubleOrNull() ?: 0.0

                        if (lat != 0.0 && lng != 0.0) {
                            geometriaList.add(CoordenadaRuta(lat, lng))
                        }
                    }

                    Log.d("Infraestructura", "-> Ruta: $nombre | Estado: $estadoRaw | Puntos válidos: ${geometriaList.size}")

                    listaRutas.add(RutaOficial(
                        id = idRuta,
                        nombre_ruta = nombre,
                        origen = origen,
                        destino = destino,
                        estado = estadoRaw,
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

    val mapProperties = MapProperties(isMyLocationEnabled = permisoConcedido)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Ayuda",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("Sugerencias") },
                    selected = false,
                    onClick = {
                        vistaAyudaActual = "SUGERENCIAS"
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Acerca De") },
                    selected = false,
                    onClick = {
                        vistaAyudaActual = "ACERCA"
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Preguntas Frecuentes") },
                    selected = false,
                    onClick = {
                        vistaAyudaActual = "PREGUNTAS"
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Actividad (Historial)") },
                    selected = false,
                    icon = { Text("⏱️") }, // Ícono simple representado por un emoji de reloj
                    onClick = {
                        vistaAyudaActual = "ACTIVIDAD"
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("🚨 SOS - Alerta de Pánico", color = Color.Red) },
                    selected = false,
                    onClick = {
                        mostrarDialogoPanico = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú principal"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. El Mapa va al fondo
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties
                ) {
                    if (mostrarRutas) {
                        val rutasVisibles = if (textoBusqueda.isBlank()) {
                            rutasOficiales
                        } else {
                            rutasOficiales.filter { r ->
                                r.nombre_ruta.contains(textoBusqueda, ignoreCase = true) ||
                                r.origen.contains(textoBusqueda, ignoreCase = true) ||
                                r.destino.contains(textoBusqueda, ignoreCase = true)
                            }
                        }

                        val coloresRutas = listOf(
                            Color(0xFFD32F2F),
                            Color(0xFF1976D2),
                            Color(0xFF388E3C),
                            Color(0xFFF57C00),
                            Color(0xFF7B1FA2)
                        )

                        rutasVisibles.forEachIndexed { index, ruta ->
                            val esActiva = ruta.estado.isEmpty() ||
                                           ruta.estado == "activa" ||
                                           ruta.estado == "activo" ||
                                           ruta.estado == "enabled" ||
                                           ruta.estado == "true"
                            if (esActiva && ruta.geometria.isNotEmpty()) {
                                val puntosLinea = ruta.geometria.map { LatLng(it.latitud, it.longitud) }
                                val colorRuta = coloresRutas[index % coloresRutas.size]
                                com.google.maps.android.compose.Polyline(
                                    points = puntosLinea,
                                    color = colorRuta,
                                    width = 18f
                                )
                            }
                        }
                    }

                    paraderosOficiales.forEach { paradero ->
                        if (paradero.estado == "activo") {
                            Marker(
                                state = MarkerState(position = LatLng(paradero.latitud, paradero.longitud)),
                                title = paradero.nombre,
                                snippet = paradero.direccion,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )
                        }
                    }

                    busesEnZona.forEach { (idBus, destino) ->
                        key(idBus) {
                            val eta = if (ubicacionPasajero != null) {
                                val distancia = calcularDistanciaMetros(ubicacionPasajero!!, destino)
                                obtenerTextoETA(distancia)
                            } else {
                                "Calculando..."
                            }

                            MarcadorBusAnimado(
                                idBus = idBus,
                                destinoLatLng = destino,
                                icono = iconoBus,
                                textoEta = eta
                            )
                        }
                    }
                }

                // 2. EL BUSCADOR, RADAR Y CONTROL DE RUTAS
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.92f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        label = { Text("¿A dónde vas?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp)),
                        singleLine = true,
                        trailingIcon = {
                            if (textoBusqueda.isNotEmpty()) {
                                IconButton(onClick = { textoBusqueda = "" }) {
                                    Text("❌")
                                }
                            } else {
                                TextButton(
                                    onClick = {
                                        if (textoBusqueda.isNotEmpty()) {
                                            val database = FirebaseDatabase.getInstance()
                                            val refHistorial = database.getReference("historial_busquedas")
                                            val uid = auth.currentUser?.uid ?: ""

                                            val nuevaBusqueda = HistorialBusquedaOficial(
                                                id_usuario = uid,
                                                destino_buscado = textoBusqueda,
                                                rutas_resultado = emptyMap(),
                                                fecha_busqueda = System.currentTimeMillis()
                                            )

                                            refHistorial.push().setValue(nuevaBusqueda)
                                        }
                                    }
                                ) {
                                    Text("🔍 Buscar")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "🚌 Buses en radar: ${busesEnZona.size}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🗺️ Ver Rutas", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = mostrarRutas,
                                    onCheckedChange = { mostrarRutas = it }
                                )
                            }
                        }
                    }

                    if (textoBusqueda.isNotBlank()) {
                        val rutasCoincidentes = rutasOficiales.filter { r ->
                            r.nombre_ruta.contains(textoBusqueda, ignoreCase = true) ||
                            r.origen.contains(textoBusqueda, ignoreCase = true) ||
                            r.destino.contains(textoBusqueda, ignoreCase = true)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (rutasCoincidentes.isNotEmpty()) {
                                    Text(
                                        "Rutas encontradas (${rutasCoincidentes.size}):",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    rutasCoincidentes.take(4).forEach { ruta ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                            onClick = {
                                                if (ruta.geometria.isNotEmpty()) {
                                                    val primerPunto = LatLng(ruta.geometria.first().latitud, ruta.geometria.first().longitud)
                                                    coroutineScope.launch {
                                                        cameraPositionState.animate(
                                                            update = CameraUpdateFactory.newLatLngZoom(primerPunto, 15f),
                                                            durationMs = 1500
                                                        )
                                                    }
                                                    Toast.makeText(context, "Centrado en: ${ruta.nombre_ruta}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("🗺️ ${ruta.nombre_ruta}", style = MaterialTheme.typography.titleMedium)
                                                    Text("De: ${ruta.origen} ➔ A: ${ruta.destino}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                                }
                                                Text("Ver 📍", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "❌ No se encontraron rutas para \"$textoBusqueda\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (mostrarDialogoIncidencia) {
                    DialogoReportarIncidencia(
                        onDismiss = { mostrarDialogoIncidencia = false },
                        onEnviar = { descripcion ->
                            val database = FirebaseDatabase.getInstance()
                            val refIncidencias = database.getReference("incidencias")
                            val uid = auth.currentUser?.uid ?: ""

                            val lat = ubicacionPasajero?.latitude ?: popayan.latitude
                            val lng = ubicacionPasajero?.longitude ?: popayan.longitude

                            val nuevaIncidencia = IncidenciaOficial(
                                id_usuario = uid,
                                descripcion = descripcion,
                                estado = "pendiente",
                                fecha_reporte = System.currentTimeMillis(),
                                latitud = lat,
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
                // --- TAREA 15: BOTÓN DE PÁNICO DEL PASAJERO ---
                if (mostrarDialogoPanico) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoPanico = false },
                        title = { Text("⚠️ ALERTA DE PÁNICO") },
                        text = { Text("¿Estás seguro de enviar una alerta de pánico prioritaria?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val database = FirebaseDatabase.getInstance()
                                    val refIncidencias = database.getReference("incidencias")
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"

                                    val alertaPanico = IncidenciaOficial(
                                        id_usuario = uid,
                                        descripcion = "PÁNICO PASAJERO",
                                        estado = "urgente",
                                        fecha_reporte = System.currentTimeMillis()
                                    )

                                    refIncidencias.push().setValue(alertaPanico).addOnSuccessListener {
                                        Toast.makeText(context, "Alerta enviada a la central", Toast.LENGTH_LONG).show()
                                        mostrarDialogoPanico = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Enviar Alerta")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoPanico = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // ----------------------------------------------

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

                    // BOTÓN DE ABORDAJE (Rastreo de bus actual)
                    Button(
                        onClick = {
                            rastreandoAbordaje = !rastreandoAbordaje
                            val intentRastreo = Intent(context, ServicioRastreoPasajero::class.java)

                            if (rastreandoAbordaje) {
                                ContextCompat.startForegroundService(context, intentRastreo)
                            } else {
                                context.stopService(intentRastreo)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 32.dp, start = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (rastreandoAbordaje) MaterialTheme.colorScheme.primary else Color.DarkGray
                        )
                    ) {
                        Text(if (rastreandoAbordaje) "⏹️ Detener Búsqueda" else "✅ Registrar Abordaje")
                    }
                }

                Button(
                    onClick = onCerrarSesion,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (auth.currentUser != null) "Cerrar Sesión" else "Salir del Mapa")
                }

                // INYECCIÓN DE LAS VISTAS DE AYUDA (Se superponen al mapa)
                if (vistaAyudaActual == "ACERCA") {
                    VistaAcercaDe(onDismiss = { vistaAyudaActual = null })
                } else if (vistaAyudaActual == "PREGUNTAS") {
                    VistaPreguntasFrecuentes(onDismiss = { vistaAyudaActual = null })
                } else if (vistaAyudaActual == "SUGERENCIAS") {
                    VistaSugerencias(onDismiss = { vistaAyudaActual = null })
                } else if (vistaAyudaActual == "ACTIVIDAD") {
                    val misAbordajesSimulados = listOf(
                        ActividadUsuario("1", "ABC-123", "Ruta 3", System.currentTimeMillis())
                    )
                    VistaActividad(
                        listaActividades = misAbordajesSimulados,
                        hayInternet = true,
                        onDismiss = { vistaAyudaActual = null }
                    )
                }

            } // Fin del Box
        } // Fin del Scaffold
    } // Fin del ModalNavigationDrawer
}

@Composable
fun VistaAcercaDe(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Acerca de POPABUS", style = MaterialTheme.typography.titleLarge) },
        text = { 
            Text("POPABUS es una plataforma de movilidad para la ciudad de Popayán, diseñada para conectar pasajeros y conductores en tiempo real, mejorando la experiencia del transporte público.") 
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun VistaPreguntasFrecuentes(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Preguntas Frecuentes", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("¿Cómo busco una ruta?", color = MaterialTheme.colorScheme.primary)
                Text("Usa la barra de búsqueda en la parte superior para ingresar el nombre de la ruta, origen o destino.")
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("¿Cómo reporto un problema?", color = MaterialTheme.colorScheme.primary)
                Text("Toca el botón '⚠️ Reportar' en la esquina inferior derecha para avisarnos de accidentes, desvíos o trancones.")
                Spacer(modifier = Modifier.height(12.dp))

                Text("¿Cómo veo los buses en tiempo real?", color = MaterialTheme.colorScheme.primary)
                Text("Los iconos de buses aparecerán y se moverán automáticamente en el mapa a medida que transmitan su ubicación.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun VistaSugerencias(onDismiss: () -> Unit) {
    var sugerencia by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buzón de Sugerencias", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("¿Tienes alguna sugerencia para mejorar POPABUS? ¡Nos encantaría leerte!")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sugerencia,
                    onValueChange = { sugerencia = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Tu sugerencia...") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (sugerencia.isNotBlank()) {
                    val database = FirebaseDatabase.getInstance()
                    val refIncidencias = database.getReference("incidencias")
                    val uid = auth.currentUser?.uid ?: ""

                    // Creamos la incidencia tipo "Sugerencia" (lat/lng en 0.0)
                    val nuevaSugerencia = IncidenciaOficial(
                        id_usuario = uid,
                        descripcion = "SUGERENCIA: $sugerencia",
                        estado = "pendiente",
                        fecha_reporte = System.currentTimeMillis(),
                        latitud = 0.0,
                        longitud = 0.0
                    )

                    refIncidencias.push().setValue(nuevaSugerencia).addOnSuccessListener {
                        Toast.makeText(context, "¡Gracias por tu sugerencia!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error al enviar", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun VistaActividad(listaActividades: List<ActividadUsuario>, hayInternet: Boolean, onDismiss: () -> Unit = {}) {
    // 1. Configuramos el ColorMatrix para la escala de grises
    val colorMatrix = remember(hayInternet) {
        val matrix = ColorMatrix()
        if (!hayInternet) {
            matrix.setToSaturation(0f) // 0f elimina toda la saturación (blanco y negro)
        }
        matrix
    }

    // Usamos un AlertDialog para mantener la consistencia con las otras vistas de ayuda
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Historial de Abordajes", 
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            // 2. Aplicamos el graphicsLayer a todo el contenedor principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // Si no hay internet, inyectamos el filtro a TODA la vista hija
                        if (!hayInternet) {
                            colorFilter = ColorFilter.colorMatrix(colorMatrix)
                        }
                    }
            ) {
                if (listaActividades.isEmpty()) {
                    // Estado vacío
                    Text(
                        text = "Aún no tienes actividad reciente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // Lista de tarjetas
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listaActividades) { actividad ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Bus: ${actividad.id_bus}", 
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Ruta: ${actividad.id_ruta}", 
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    // Formateo seguro de la fecha
                                    val fechaFormateada = remember(actividad.fecha_abordaje) {
                                        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                                        sdf.format(Date(actividad.fecha_abordaje))
                                    }
                                    
                                    Text(
                                        text = "Fecha: $fechaFormateada", 
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Método: ${actividad.metodo_registro.uppercase()}", 
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cerrar") 
            }
        }
    )
}


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
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { subPantalla = "REPORTES" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("📊 Buzón de Reportes")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { subPantalla = "TORRE_CONTROL" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("📡 Torre de Control (Mapa)")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { subPantalla = "INFORMES" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("📊 Gestión de Informes")
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
        "REPORTES" -> PantallaGestionReportes(onVolver = { subPantalla = "MENU" })
        "TORRE_CONTROL" -> MapaGeneralAdmin(onVolver = { subPantalla = "MENU" })
        "INFORMES" -> PantallaGestionInformes(onVolver = { subPantalla = "MENU" })

        // --- NUEVO SPRINT 11: RUTA A GESTIÓN DE USUARIOS ---
        "USUARIOS" -> PantallaGestionUsuarios(onVolver = { subPantalla = "MENU" })
    }
}

@Composable
fun PantallaGestionParaderos(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refParaderos = database.getReference("paraderos")

    var paraderoSeleccionado by remember { mutableStateOf<ParaderoOficial?>(null) }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { paraderoSeleccionado = paradero },
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

        if (paraderoSeleccionado != null) {
            val paradero = paraderoSeleccionado!!
            var editNombre by remember(paradero) { mutableStateOf(paradero.nombre) }
            var editDireccion by remember(paradero) { mutableStateOf(paradero.direccion) }

            AlertDialog(
                onDismissRequest = { paraderoSeleccionado = null },
                title = { Text("Editar Paradero", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editNombre,
                            onValueChange = { editNombre = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editDireccion,
                            onValueChange = { editDireccion = it },
                            label = { Text("Dirección") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Geo: ${paradero.latitud}, ${paradero.longitud}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (editNombre.isNotBlank() && editDireccion.isNotBlank()) {
                            val actualizaciones = mapOf(
                                "nombre" to editNombre,
                                "direccion" to editDireccion
                            )
                            refParaderos.child(paradero.id).updateChildren(actualizaciones).addOnSuccessListener {
                                Toast.makeText(context, "Paradero actualizado", Toast.LENGTH_SHORT).show()
                                paraderoSeleccionado = null
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Guardar Cambios")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            refParaderos.child(paradero.id).removeValue().addOnSuccessListener {
                                Toast.makeText(context, "Paradero eliminado definitivamente", Toast.LENGTH_SHORT).show()
                                paraderoSeleccionado = null
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("🗑️ Borrar Definitivamente")
                    }
                }
            )
        }

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

    var rutaSeleccionada by remember { mutableStateOf<RutaOficial?>(null) }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { rutaSeleccionada = ruta },
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

        if (rutaSeleccionada != null) {
            val ruta = rutaSeleccionada!!
            var editNombre by remember(ruta) { mutableStateOf(ruta.nombre_ruta) }
            var editOrigen by remember(ruta) { mutableStateOf(ruta.origen) }
            var editDestino by remember(ruta) { mutableStateOf(ruta.destino) }

            AlertDialog(
                onDismissRequest = { rutaSeleccionada = null },
                title = { Text("Editar Ruta", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editNombre,
                            onValueChange = { editNombre = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editOrigen,
                            onValueChange = { editOrigen = it },
                            label = { Text("Origen") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editDestino,
                            onValueChange = { editDestino = it },
                            label = { Text("Destino") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (editNombre.isNotBlank() && editOrigen.isNotBlank() && editDestino.isNotBlank()) {
                            val actualizaciones = mapOf(
                                "nombre_ruta" to editNombre,
                                "origen" to editOrigen,
                                "destino" to editDestino
                            )
                            refRutas.child(ruta.id).updateChildren(actualizaciones).addOnSuccessListener {
                                Toast.makeText(context, "Ruta actualizada", Toast.LENGTH_SHORT).show()
                                rutaSeleccionada = null
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Guardar Cambios")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            refRutas.child(ruta.id).removeValue().addOnSuccessListener {
                                Toast.makeText(context, "Ruta eliminada definitivamente", Toast.LENGTH_SHORT).show()
                                rutaSeleccionada = null
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("🗑️ Borrar Definitivamente")
                    }
                }
            )
        }

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

    var busSeleccionado by remember { mutableStateOf<BusOficial?>(null) }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { busSeleccionado = bus },
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

        if (busSeleccionado != null) {
            val bus = busSeleccionado!!
            var editPlaca by remember(bus) { mutableStateOf(bus.placa) }
            var editModelo by remember(bus) { mutableStateOf(bus.modelo) }
            var editRuta by remember(bus) { mutableStateOf(bus.id_ruta) }
            var editConductor by remember(bus) { mutableStateOf(bus.id_conductor_actual) }

            AlertDialog(
                onDismissRequest = { busSeleccionado = null },
                title = { Text("Editar Vehículo", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editPlaca,
                            onValueChange = { editPlaca = it },
                            label = { Text("Placa") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editModelo,
                            onValueChange = { editModelo = it },
                            label = { Text("Modelo/Año") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editRuta,
                            onValueChange = { editRuta = it },
                            label = { Text("ID de Ruta") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editConductor,
                            onValueChange = { editConductor = it },
                            label = { Text("UID de Conductor") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (editPlaca.isNotBlank()) {
                            val actualizaciones = mapOf(
                                "placa" to editPlaca,
                                "modelo" to editModelo,
                                "id_ruta" to editRuta,
                                "id_conductor_actual" to editConductor
                            )
                            refBuses.child(bus.id).updateChildren(actualizaciones).addOnSuccessListener {
                                Toast.makeText(context, "Vehículo actualizado", Toast.LENGTH_SHORT).show()
                                busSeleccionado = null
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "La placa no puede estar vacía", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Guardar Cambios")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            refBuses.child(bus.id).removeValue().addOnSuccessListener {
                                Toast.makeText(context, "Vehículo eliminado definitivamente", Toast.LENGTH_SHORT).show()
                                busSeleccionado = null
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("🗑️ Borrar Definitivamente")
                    }
                }
            )
        }

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
    
    // Estado para el CRUD profundo de un usuario específico
    var usuarioSeleccionado by remember { mutableStateOf<UsuarioOficial?>(null) }

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

        // Lista visual con funcionalidad CRUD
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(listaUsuarios) { usuario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { usuarioSeleccionado = usuario }, // Se asigna el usuario al tocar
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("👤 ${usuario.nombre_completo}", style = MaterialTheme.typography.titleMedium)
                        Text("Rol: ${usuario.rol.uppercase()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Estado: ${if (usuario.activo) "🟢 Activo" else "🔴 Suspendido"}", style = MaterialTheme.typography.bodySmall)
                        Text("UID: ${usuario.id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DIÁLOGO DE CRUD COMPLETO ---
        if (usuarioSeleccionado != null) {
            val usuario = usuarioSeleccionado!!

            AlertDialog(
                onDismissRequest = { usuarioSeleccionado = null },
                title = { Text("Perfil de Usuario", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("👤 Nombre: ${usuario.nombre_completo}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🔑 UID: ${usuario.id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val fechaFormateada = remember(usuario.fecha_registro) {
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
                            sdf.format(java.util.Date(usuario.fecha_registro))
                        }
                        Text("📅 Registro: $fechaFormateada", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Rol actual: ${usuario.rol.uppercase()}", style = MaterialTheme.typography.bodyMedium)
                        Text("Estado: ${if (usuario.activo) "🟢 Activo" else "🔴 Suspendido"}", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones de Actualización (Update)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Cambiar Estado
                            OutlinedButton(
                                onClick = {
                                    val nuevoEstado = !usuario.activo
                                    refUsuarios.child(usuario.id).updateChildren(mapOf("activo" to nuevoEstado))
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                                            usuarioSeleccionado = usuarioSeleccionado?.copy(activo = nuevoEstado)
                                        }
                                }
                            ) {
                                Text(if (usuario.activo) "🔴 Suspender" else "🟢 Activar")
                            }

                            // Cambiar Rol
                            OutlinedButton(
                                onClick = {
                                    if (usuario.rol.lowercase() == "administrador") {
                                        Toast.makeText(context, "Seguridad: No editable", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val nuevoRol = if (usuario.rol.lowercase() == "pasajero") "conductor" else "pasajero"
                                        refUsuarios.child(usuario.id).updateChildren(mapOf("rol" to nuevoRol))
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Rol actualizado", Toast.LENGTH_SHORT).show()
                                                usuarioSeleccionado = usuarioSeleccionado?.copy(rol = nuevoRol)
                                            }
                                    }
                                }
                            ) {
                                Text("🔄 Rol")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { usuarioSeleccionado = null }) {
                        Text("Cerrar")
                    }
                },
                // Botón de Borrado Fuerte (Hard Delete)
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            refUsuarios.child(usuario.id).removeValue().addOnSuccessListener {
                                Toast.makeText(context, "Usuario eliminado definitivamente", Toast.LENGTH_SHORT).show()
                                usuarioSeleccionado = null
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("🗑️ Borrar Definitivamente")
                    }
                }
            )
        }

        Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
            Text("Volver al Menú")
        }
    }
}

@Composable
fun PantallaGestionReportes(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refIncidencias = database.getReference("incidencias")

    // Estado para la lista en tiempo real
    var listaIncidencias by remember { mutableStateOf<List<IncidenciaOficial>>(emptyList()) }

    // Estado de las pestañas
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pasajeros", "Conductores")

    // Descargamos las incidencias de Firebase en tiempo real
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val incidencias = mutableListOf<IncidenciaOficial>()
                for (hijo in snapshot.children) {
                    val inc = hijo.getValue(IncidenciaOficial::class.java)
                    if (inc != null) {
                        incidencias.add(inc)
                    }
                }
                // Ordenamos para que las más recientes aparezcan primero
                listaIncidencias = incidencias.sortedByDescending { it.fecha_reporte }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        refIncidencias.addValueEventListener(listener)
        onDispose { refIncidencias.removeEventListener(listener) }
    }

    // --- FILTRO MATEMÁTICO DE MATRIZ OPERATIVA ---
    val matrizOperativaYPanicos = listOf("trancón", "desvío", "fallo", "pánico")

    val incidenciasFiltradas = listaIncidencias.filter { incidencia ->
        val descripcionNormalizada = incidencia.descripcion.lowercase()
        // Verificamos si la descripción contiene alguna palabra clave operativa
        val esOperativaOPanico = matrizOperativaYPanicos.any { keyword ->
            descripcionNormalizada.contains(keyword)
        }

        if (tabIndex == 0) {
            // Pestaña Pasajeros: NO es operativa, ni pánico (Sugerencias o quejas manuales)
            !esOperativaOPanico
        } else {
            // Pestaña Conductores / Operativa: SÍ es operativa o pánico
            esOperativaOPanico
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reportes e Incidencias", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // TAB ROW (Pestañas)
        androidx.compose.material3.TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                androidx.compose.material3.Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de incidencias filtradas
        if (incidenciasFiltradas.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No hay reportes en esta categoría", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(incidenciasFiltradas) { incidencia ->
                    val esUrgente = incidencia.estado.lowercase() == "urgente"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        // Fondo rojizo suave si es urgente, sino el default
                        colors = CardDefaults.cardColors(
                            containerColor = if (esUrgente) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        // Borde rojo intenso si es urgente
                        border = if (esUrgente) androidx.compose.foundation.BorderStroke(1.dp, Color.Red) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Título dinámico
                            Text(
                                text = if (esUrgente) "🚨 ALERTA: ${incidencia.estado.uppercase()}" else "📋 Reporte Normal",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (esUrgente) Color.Red else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Descripción
                            Text(
                                text = incidencia.descripcion,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Formateo de fecha
                            val fechaFormateada = remember(incidencia.fecha_reporte) {
                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(incidencia.fecha_reporte))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "📅 $fechaFormateada", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text(text = "Estado: ${incidencia.estado}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
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

@Composable
fun MapaGeneralAdmin(onVolver: () -> Unit) {
    val context = LocalContext.current
    var flotaActiva by remember { mutableStateOf<Map<String, PosicionGpsOficial>>(emptyMap()) }
    
    // Timer para forzar la recomposición cada cierto tiempo y actualizar los colores de conectividad
    var tiempoActual by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000) // Refresca la conectividad cada 5 segundos
            tiempoActual = System.currentTimeMillis()
        }
    }

    // Descargamos los buses en tiempo real desde Firebase
    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val refBuses = database.getReference("BusesActivos")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mapaTemporal = mutableMapOf<String, PosicionGpsOficial>()
                for (hijo in snapshot.children) {
                    val idBus = hijo.key ?: continue
                    
                    // Extraemos los datos de la colección (que internamente es BusActivo) 
                    // y los mapeamos a la clase solicitada PosicionGpsOficial
                    val latObj = hijo.child("latitud").value
                    val lngObj = hijo.child("longitud").value
                    val tsObj = hijo.child("timestamp").value
                    
                    val lat = (latObj as? Number)?.toDouble() ?: latObj?.toString()?.toDoubleOrNull() ?: 0.0
                    val lng = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()?.toDoubleOrNull() ?: 0.0
                    val ts = (tsObj as? Number)?.toLong() ?: tsObj?.toString()?.toLongOrNull() ?: 0L
                    
                    if (lat != 0.0 && lng != 0.0) {
                        mapaTemporal[idBus] = PosicionGpsOficial(
                            id_recorrido = "",
                            latitud = lat,
                            longitud = lng,
                            geohash = "",
                            velocidad = null,
                            hora_lectura = ts // Mapeamos timestamp a hora_lectura
                        )
                    }
                }
                flotaActiva = mapaTemporal
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error conectando con la flota: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        refBuses.addValueEventListener(listener)
        onDispose { refBuses.removeEventListener(listener) }
    }

    val popayan = LatLng(2.4419, -76.6063)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(popayan, 13f)
    }

    val iconoBusNormal = remember { obtenerIconoBus(context, R.drawable.ic_bus) }
    val iconoBusGris = remember { obtenerIconoBusGris(context, R.drawable.ic_bus) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            flotaActiva.forEach { (idBus, posicion) ->
                // Lógica de conectividad: Si la diferencia > 2 minutos (120,000 ms), está desconectado
                val isDesconectado = (tiempoActual - posicion.hora_lectura) > 120_000L
                
                Marker(
                    state = MarkerState(position = LatLng(posicion.latitud, posicion.longitud)),
                    title = "Vehículo: $idBus",
                    snippet = if (isDesconectado) "⚠️ Señal Perdida (+2 min)" else "🟢 Activo",
                    icon = if (isDesconectado) iconoBusGris else iconoBusNormal
                )
            }
        }

        // Botón flotante para salir de la torre de control
        androidx.compose.material3.FloatingActionButton(
            onClick = onVolver,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            containerColor = Color.White
        ) {
            Text("⬅️")
        }
    }
}

@Composable
fun PantallaGestionInformes(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refInformes = database.getReference("informes_diarios")

    var listaInformes by remember { mutableStateOf<List<InformeDiario>>(emptyList()) }
    var filtroTiempo by remember { mutableStateOf("Diario") }

    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val informes = mutableListOf<InformeDiario>()
                for (hijo in snapshot.children) {
                    val informe = hijo.getValue(InformeDiario::class.java)
                    if (informe != null) {
                        informes.add(informe)
                    }
                }
                listaInformes = informes.sortedByDescending { it.fecha_cierre }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        refInformes.addValueEventListener(listener)
        onDispose { refInformes.removeEventListener(listener) }
    }

    val currentTime = System.currentTimeMillis()
    val listaFiltrada = remember(listaInformes, filtroTiempo) {
        listaInformes.filter { informe ->
            when (filtroTiempo) {
                "Diario" -> informe.fecha_cierre >= (currentTime - 86400000L)
                "Semanal" -> informe.fecha_cierre >= (currentTime - 604800000L)
                "Mensual" -> informe.fecha_cierre >= (currentTime - 2592000000L)
                else -> true
            }
        }
    }

    val totalInformes = listaFiltrada.size
    val tiempoPromedio = if (totalInformes > 0) {
        listaFiltrada.sumOf { it.tiempo_total_minutos } / totalInformes
    } else {
        0L
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de Informes", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Botones de Filtrado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Diario", "Semanal", "Mensual").forEach { filtro ->
                val seleccionado = filtroTiempo == filtro
                Button(
                    onClick = { filtroTiempo = filtro },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (seleccionado) MaterialTheme.colorScheme.primary else Color.LightGray,
                        contentColor = if (seleccionado) Color.White else Color.Black
                    )
                ) {
                    Text(filtro)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Analítica General", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Informes", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalInformes", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tiempo Promedio", style = MaterialTheme.typography.bodyMedium)
                        Text("$tiempoPromedio min", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(listaFiltrada) { informe ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val fechaFormateada = remember(informe.fecha_cierre) {
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
                            sdf.format(java.util.Date(informe.fecha_cierre))
                        }
                        Text("📅 Cierre: $fechaFormateada", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Conductor ID: ${informe.id_conductor}", style = MaterialTheme.typography.bodyMedium)
                        Text("Duración del viaje: ${informe.tiempo_total_minutos} min", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Alertas emitidas: ${informe.total_alertas_emitidas}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (informe.total_alertas_emitidas > 0) Color.Red else Color.DarkGray
                        )
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