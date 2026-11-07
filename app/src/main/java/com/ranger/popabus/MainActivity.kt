package com.ranger.popabus


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.VibratorManager
import android.os.VibrationAttributes
import android.media.AudioAttributes
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ranger.popabus.ui.theme.PopaBusTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.layout.wrapContentWidth

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle



import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ChatBubbleOutline



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
    val estado: String = "activo",
    val id_ruta: String = "" // NUEVO CAMPO AÑADIDO (Reemplaza a idRuta para mantener coherencia en DB)
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
    val correo: String = "",
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
    val metodo_registro: String = "manual", // Puede ser "manual" o "automatico"
    val paradero_origen: String = "",
    val duracionMinutos: Long = 0L
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
    // La ubicación interpolada actual que se dibuja en pantalla
    var ubicacionActual by remember { mutableStateOf(destinoLatLng) }
    
    // Los "puntos clave" o esquinas consolidadas desde Firebase
    var puntosClave by remember { mutableStateOf(listOf(destinoLatLng)) }

    LaunchedEffect(destinoLatLng) {
        if (ubicacionActual != destinoLatLng) {
            // Guardamos la posición desde la que arranca el bus en este nuevo tramo
            val puntoInicial = ubicacionActual
            if (puntosClave.lastOrNull() != puntoInicial) {
                puntosClave = puntosClave + puntoInicial
            }

            animarMarcador(
                posicionActual = puntoInicial,
                posicionDestino = destinoLatLng,
                onProgress = { ubicacionActual = it }
            )
        }
    }

    // El rastro es la historia consolidada + la posición exacta interpolada en este nanosegundo
    val rastroCompleto = puntosClave + ubicacionActual

    if (rastroCompleto.size > 1) {
        com.google.maps.android.compose.Polyline(
            points = puntosClave + ubicacionActual,
            color = Color.Blue, // Azul fuerte según el diseño
            width = 16f,
            zIndex = 1.5f
        )
    }

    Marker(
        state = MarkerState(position = ubicacionActual),
        title = "Bus: $idBus",
        snippet = "ETA: $textoEta", // <-- NUEVO SPRINT 9: Mostramos el tiempo de llegada al tocar el bus
        icon = icono,
        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f) // Centrar el ícono sobre la coordenada exacta
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
    // Velocidad urbana promedio en Popayán: ~15 km/h (250 metros por minuto)
    val velocidadMetrosPorMinuto = 250f
    val minutosTotales = (distanciaMetros / velocidadMetrosPorMinuto).toInt()

    return when {
        distanciaMetros < 50f -> "¡Llegando!"
        minutosTotales < 1 -> "< 1 min"
        minutosTotales < 60 -> "~$minutosTotales min"
        else -> {
            val horas = minutosTotales / 60
            val minutosRestantes = minutosTotales % 60
            if (minutosRestantes == 0) "~${horas} h" else "~${horas} h ${minutosRestantes} min"
        }
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
                var pantallaActual by rememberSaveable { mutableStateOf("SPLASH") }

                when (pantallaActual) {
                    "SPLASH" -> PantallaSplash(
                        onSplashTerminado = {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                FirebaseDatabase.getInstance().getReference("usuarios")
                                    .child(currentUser.uid)
                                    .child("rol")
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        val rolLeido = snapshot.getValue(String::class.java)
                                        pantallaActual = when (RolUsuario.desdeString(rolLeido)) {
                                            RolUsuario.CONDUCTOR -> "CONDUCTOR"
                                            RolUsuario.ADMINISTRADOR -> "ADMINISTRADOR"
                                            RolUsuario.PASAJERO -> "PASAJERO"
                                        }
                                    }
                                    .addOnFailureListener {
                                        pantallaActual = "LOGIN" // Fallback seguro
                                    }
                            } else {
                                pantallaActual = "LOGIN"
                            }
                        }
                    )
                    "LOGIN" -> PantallaLogin(
                        onIrARegistro = { pantallaActual = "REGISTRO" },
                        onLoginExitoso = { rolRecibido ->
                            // ... tu lógica de enrutamiento intacta[cite: 4, 10]
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
                                "correo" to (FirebaseAuth.getInstance().currentUser?.email ?: ""),
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
                        },
                        onIrALogin = { pantallaActual = "LOGIN" },
                        onIrARegistro = { pantallaActual = "REGISTRO" }
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
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    val WEB_CLIENT_ID = "519575230065-fe8et4hv5issfchig8pdarg8nuitspcu.apps.googleusercontent.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Logo y Título PopaBus (Restaurado)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2563EB),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.DirectionsBus,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold, fontSize = 28.sp)) {
                        append("Popa")
                    }
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 28.sp)) { // <--- Cambiado a Azul
                        append("Bus")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Bienvenido", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(4.dp))
        Text("Tu ciudad se mueve contigo", fontSize = 14.sp, color = Color(0xFF64748B))

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Campo Correo (Restaurado)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Correo", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = { Text("tu@correo.com", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Campo Contraseña (Restaurado)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Contraseña", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                placeholder = { Text("Ingresa tu contraseña", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(
                                id = if (passwordVisible) android.R.drawable.ic_menu_view else android.R.drawable.ic_secure
                            ),
                            contentDescription = "Alternar contraseña",
                            tint = Color.Gray
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¿Problemas para ingresar?",
            color = Color(0xFF2563EB),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.End).clickable { /* Lógica recuperar */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Botón Ingresar (Restaurado)
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
                                            val activo = snapshot.child("activo").getValue(Boolean::class.java) ?: true
                                            if (!activo) {
                                                auth.signOut()
                                                Toast.makeText(context, "ACCESO DENEGADO", Toast.LENGTH_LONG).show()
                                            } else {
                                                val rol = snapshot.child("rol").getValue(String::class.java) ?: "pasajero"
                                                Toast.makeText(context, "Bienvenido", Toast.LENGTH_SHORT).show()
                                                onLoginExitoso(rol)
                                            }
                                        }
                                        .addOnFailureListener { error ->
                                            cargando = false
                                            Toast.makeText(context, "Error al leer perfil", Toast.LENGTH_LONG).show()
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            enabled = !cargando
        ) {
            Text(if (cargando) "Validando..." else "Ingresar", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            Text(" o ", color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Botón Invitado (Restaurado)
        OutlinedButton(
            onClick = { onLoginExitoso("invitado") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Text("Continuar sin registrarse", color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Botón Google
        OutlinedButton(
            onClick = {
                cargando = true
                coroutineScope.launch {
                    try {
                        val credentialManager = CredentialManager.create(context)
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(WEB_CLIENT_ID)
                            .setAutoSelectEnabled(true)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(context, request)
                        val credential = result.credential

                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                            auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user?.uid != null) {
                                        val ref = database.getReference("usuarios").child(user.uid)
                                        ref.get().addOnSuccessListener { snap ->
                                            if (snap.exists()) {
                                                val activo = snap.child("activo").getValue(Boolean::class.java) ?: true
                                                if (!activo) { auth.signOut(); cargando = false; Toast.makeText(context, "CUENTA SUSPENDIDA", Toast.LENGTH_LONG).show() }
                                                else { cargando = false; onLoginExitoso(snap.child("rol").getValue(String::class.java) ?: "pasajero") }
                                            } else {
                                                ref.setValue(mapOf("nombre_completo" to (user.displayName ?: ""), "correo" to (user.email ?: ""), "rol" to "pasajero", "activo" to true, "fecha_registro" to ServerValue.TIMESTAMP)).addOnSuccessListener {
                                                    cargando = false; onLoginExitoso("pasajero")
                                                }
                                            }
                                        }
                                    }
                                } else { cargando = false; Toast.makeText(context, "Error Auth", Toast.LENGTH_SHORT).show() }
                            }
                        } else { cargando = false; Toast.makeText(context, "Credencial no válida", Toast.LENGTH_SHORT).show() }
                    } catch (e: Exception) { cargando = false; Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            contentPadding = PaddingValues(horizontal = 16.dp),
            enabled = !cargando
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_google_logo), contentDescription = "Google", tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(if (cargando) "Accediendo..." else "Continuar con Google", color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 7. Enlace de Registro (Restaurado)
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            Text("¿No tienes cuenta? ", color = Color(0xFF64748B))
            Text("Regístrate aquí", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onIrARegistro() })
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
    var confirmContrasena by remember { mutableStateOf("") }

    // Estados para el ojito
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    // Lógica de Validación en Tiempo Real
    val tieneMinimo8 = contrasena.length >= 8
    val tieneMayuscula = contrasena.any { it.isUpperCase() }
    val tieneNumero = contrasena.any { it.isDigit() }
    val tieneEspecial = contrasena.any { !it.isLetterOrDigit() }

    // El botón solo se habilita si se cumplen las reglas y las claves coinciden
    val contrasenaSegura = tieneMinimo8 && tieneMayuscula && tieneNumero && tieneEspecial
    val contraseñasCoinciden = contrasena.isNotEmpty() && contrasena == confirmContrasena
    val formularioValido = nombre.isNotEmpty() && correo.isNotEmpty() && contrasenaSegura && contraseñasCoinciden

    // Colores base para la UI
    val primaryBlue = Color(0xFF2563EB)
    val darkText = Color(0xFF1E293B)
    val grayText = Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()) // <--- ESTA ES LA LÍNEA MÁGICA
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

// 1. Logo y Título PopaBus (Ajuste de Color)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2563EB),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.DirectionsBus,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold, fontSize = 28.sp)) {
                        append("Popa")
                    }
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 28.sp)) { // <--- Cambiado a Azul
                        append("Bus")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Crear Cuenta", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = darkText)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Completa tus datos para empezar a moverte mejor.", fontSize = 12.sp, color = grayText)

        Spacer(modifier = Modifier.height(25.dp))




        // Campo: Nombre
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Nombre completo", fontWeight = FontWeight.Bold, color = darkText, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Tu nombre y apellido", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Correo
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Correo electrónico", fontWeight = FontWeight.Bold, color = darkText, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = { Text("nombre@correo.com", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Contraseña
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Contraseña", fontWeight = FontWeight.Bold, color = darkText, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                placeholder = { Text("Crea una contraseña segura", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_view),
                            contentDescription = "Ver contraseña",
                            tint = if (passwordVisible) primaryBlue else Color.Gray
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Checklist de Validación
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                FiltroCheck(texto = "Mínimo 8 caracteres", cumplido = tieneMinimo8, modifier = Modifier.weight(1f))
                FiltroCheck(texto = "Al menos una mayúscula", cumplido = tieneMayuscula, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                FiltroCheck(texto = "Al menos un número", cumplido = tieneNumero, modifier = Modifier.weight(1f))
                FiltroCheck(texto = "Un carácter especial (@,#,etc)", cumplido = tieneEspecial, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Confirmar Contraseña
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Confirmar Contraseña", fontWeight = FontWeight.Bold, color = darkText, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmContrasena,
                onValueChange = { confirmContrasena = it },
                placeholder = { Text("Repite tu contraseña", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (confirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmContrasena.isNotEmpty() && contrasena != confirmContrasena,
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_view),
                            contentDescription = "Ver contraseña",
                            tint = if (confirmPasswordVisible) primaryBlue else Color.Gray
                        )
                    }
                }
            )
            if (confirmContrasena.isNotEmpty() && contrasena != confirmContrasena) {
                Text("Las contraseñas no coinciden", color = Color.Red, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Registrarse (Ajuste a Color Azul Sólido)
        Button(
            onClick = {
                if (formularioValido) {
                    cargando = true
                    auth.createUserWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                Toast.makeText(context, "¡Usuario creado exitosamente!", Toast.LENGTH_SHORT).show()
                                onRegistroExitoso(user?.uid ?: "", nombre)
                            } else {
                                cargando = false
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Por favor, completa el formulario correctamente.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            // Se mantiene SIEMPRE azul, sin importar si el formulario está válido o no.
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            enabled = !cargando // Removemos el bloqueo por "formularioValido" para que el botón no se vea gris
        ) {
            Text(if (cargando) "Registrando..." else "Registrarse", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.weight(1f)) // Empuja el enlace hacia el fondo
        Spacer(modifier = Modifier.height(24.dp))

        // Enlace al Login (Restaurado y ajustado al diseño)
        Row(modifier = Modifier.padding(bottom = 24.dp)) {
            Text("¿Ya tienes cuenta? ", color = grayText, fontSize = 14.sp)
            Text(
                "Inicia sesión aquí",
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onVolverLogin() }
            )
        }
    }
}

// Sub-componente para el checklist
@Composable
fun FiltroCheck(texto: String, cumplido: Boolean, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = if (cumplido) androidx.compose.material.icons.Icons.Filled.CheckCircle else androidx.compose.material.icons.Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (cumplido) Color(0xFF10B981) else Color.LightGray,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(texto, color = if (cumplido) Color(0xFF1E293B) else Color.Gray, fontSize = 10.sp)
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
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
    val iconoBus = remember { obtenerIconoBus(context, R.drawable.ic_bus_nuevo) }

    // Estado para la geometría de la ruta ideal
    var rutaIdeal by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    // Estado para el trazo real recorrido
    var rutaRecorrida by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    // Estado de la ubicación actual del propio conductor
    var miUbicacionActual by remember { mutableStateOf<LatLng?>(null) }
    // ESTADO ELEVADO COMPARTIDO (State Hoisting) a nivel del mapa
    var ubicacionFluida by remember { mutableStateOf<LatLng?>(null) }

    // LÓGICA DE ANIMACIÓN ELEVADA
    LaunchedEffect(miUbicacionActual) {
        miUbicacionActual?.let { destino ->
            val inicio = ubicacionFluida ?: destino
            if (inicio != destino) {
                animarMarcador(
                    posicionActual = inicio,
                    posicionDestino = destino,
                    onProgress = { ubicacionFluida = it }
                )
            } else {
                ubicacionFluida = destino
            }
        }
    }

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
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // --- LECTURA DE LA RUTA IDEAL (Línea punteada) ---
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        val refBuses = database.getReference("buses")
        refBuses.orderByChild("id_conductor_actual").equalTo(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val busSnapshot = snapshot.children.first()
                    val rutaIdReal = busSnapshot.child("id_ruta").getValue(String::class.java)
                        ?: return@addOnSuccessListener

                    // Buscar la geometría de esta ruta en la colección "rutas"
                    database.getReference("rutas").child(rutaIdReal).child("geometria").get()
                        .addOnSuccessListener { geoSnapshot ->
                            if (geoSnapshot.exists()) {
                                val puntos = geoSnapshot.children.mapNotNull { puntoSnap ->
                                    val latObj = puntoSnap.child("latitud").value
                                    val lngObj = puntoSnap.child("longitud").value
                                    val lat = (latObj as? Number)?.toDouble() ?: latObj?.toString()
                                        ?.toDoubleOrNull()
                                    val lng = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()
                                        ?.toDoubleOrNull()
                                    if (lat != null && lng != null) LatLng(lat, lng) else null
                                }
                                rutaIdeal = puntos
                            }
                        }
                }
            }
    }


                // --- ESCUCHA DEL GPS REAL DEL CONDUCTOR (Para el trazo azul sólido) ---
    DisposableEffect(enRecorrido) {
        var locationCallback: LocationCallback? = null
        if (enRecorrido && permisoConcedido) {
            val locationRequest =
                com.google.android.gms.location.LocationRequest.Builder(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    3000L
                ).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val nuevaPos = LatLng(location.latitude, location.longitude)

                        // Guardar en el historial solo si cambia significativamente para no saturar
                        if (rutaRecorrida.isEmpty() || rutaRecorrida.last() != nuevaPos) {
                            rutaRecorrida = rutaRecorrida + nuevaPos
                        }

                        // Actualizar la posición exacta del conductor al instante
                        miUbicacionActual = nuevaPos
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        onDispose {
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        }
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
                    val lat = (latObj as? Number)?.toDouble() ?: latObj?.toString()
                        ?.toDoubleOrNull()
                    val lng = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()
                        ?.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        busesRecibidos[idBus] = LatLng(lat, lng)
                    }
                }
                busesEnZona = busesRecibidos
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        query.addValueEventListener(listener)
        onDispose { query.removeEventListener(listener) }
    }

    // --- NUEVO SPRINT 10: MOTOR DE ALERTAS EN LA VÍA ---
    DisposableEffect(Unit) {
        val refIncidencias = FirebaseDatabase.getInstance().getReference("incidencias")
        val listenerAlertas = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertas = mutableListOf<IncidenciaOficial>()
                for (hijo in snapshot.children) {
                    val inc = hijo.getValue(IncidenciaOficial::class.java)
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

    // --- NUEVA UI TÁCTICA DEL CONDUCTOR (SPRINT 10 REDISEÑO) ---
    val conductorScaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = conductorScaffoldState,
        sheetPeekHeight = 150.dp,
        sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        sheetContainerColor = Color.White,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LÍNEA 4 • BUS PB-024", // Dinamizar con datos reales
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (!enRecorrido) "Listo para conducir" else "Recorrido en curso",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    // Reloj pequeño simulado
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        var horaActual by remember { mutableStateOf("") }
                        LaunchedEffect(Unit) {
                            while (true) {
                                val sdf = java.text.SimpleDateFormat(
                                    "HH:mm",
                                    java.util.Locale.getDefault()
                                )
                                horaActual = sdf.format(java.util.Date())
                                kotlinx.coroutines.delay(1000)
                            }
                        }
                        Text(
                            text = if (horaActual.isEmpty()) "00:00" else horaActual,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // BOTÓN DE ACCIÓN PRINCIPAL
                if (!enRecorrido) {
                    Button(
                        onClick = {
                            if (!permisoConcedido) {
                                Toast.makeText(
                                    context,
                                    "Se requieren permisos de ubicación para iniciar.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            val uid = auth.currentUser?.uid ?: "CONDUCTOR_DESCONOCIDO"
                            val refBuses = database.getReference("buses")

                            refBuses.orderByChild("id_conductor_actual").equalTo(uid)
                                .get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    val busSnapshot = snapshot.children.first()
                                    val busIdReal = busSnapshot.key ?: "BUS_DESCONOCIDO"
                                    val rutaIdReal = busSnapshot.child("id_ruta")
                                        .getValue(String::class.java)
                                        ?: "RUTA_DESCONOCIDA"

                                    val refRecorridos =
                                        database.getReference("recorridos")
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

                                    nuevoRecorridoRef.setValue(nuevoRecorrido)
                                        .addOnSuccessListener {
                                            idRecorridoActual = idGenerado
                                            enRecorrido = true
                                            transmitiendo = true
                                            rutaRecorrida =
                                                emptyList() // Reiniciar trazo
                                            horaInicioViaje = System.currentTimeMillis()
                                            contadorAlertas = 0

                                            val intent = Intent(
                                                context,
                                                ServicioGpsConductor::class.java
                                            )
                                            intent.putExtra("ID_RECORRIDO", idGenerado)
                                            intent.putExtra("ID_BUS", busIdReal)
                                            ContextCompat.startForegroundService(
                                                context,
                                                intent
                                            )
                                        }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFF2563EB
                            )
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(
                            "▶  Iniciar Recorrido Oficial",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (idRecorridoActual != null) {
                                val uidConductor =
                                    auth.currentUser?.uid ?: "desconocido"
                                val tiempoActual = System.currentTimeMillis()

                                val refRecorrido = database.getReference("recorridos")
                                    .child(idRecorridoActual!!)
                                refRecorrido.updateChildren(
                                    mapOf(
                                        "hora_fin" to tiempoActual,
                                        "estado" to "finalizado"
                                    )
                                )

                                val minutosTotales =
                                    if (horaInicioViaje > 0L) (tiempoActual - horaInicioViaje) / 60000 else 0L

                                val nuevoInforme = InformeDiario(
                                    id_recorrido = idRecorridoActual!!,
                                    id_conductor = uidConductor,
                                    tiempo_total_minutos = minutosTotales,
                                    total_alertas_emitidas = contadorAlertas,
                                    fecha_cierre = tiempoActual
                                )

                                database.getReference("informes_diarios").push()
                                    .setValue(nuevoInforme)
                                val intent =
                                    Intent(context, ServicioGpsConductor::class.java)
                                context.stopService(intent)
                            }
                            enRecorrido = false
                            transmitiendo = false
                            idRecorridoActual = null
                            horaInicioViaje = 0L
                            contadorAlertas = 0
                            rutaRecorrida = emptyList() // Limpiar trazo
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFF0D9488
                            )
                        ), // Verde para detenido
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(
                            "▶  Recorrido Oficial Iniciado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ) // Texto simulando tu imagen 2
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Fila 1 de Reportes
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            contadorAlertas++
                            val incidencia = IncidenciaOficial(
                                id_usuario = auth.currentUser?.uid ?: "",
                                descripcion = "Trancón",
                                estado = "pendiente",
                                fecha_reporte = System.currentTimeMillis()
                            )
                            database.getReference("incidencias").push()
                                .setValue(incidencia)
                            Toast.makeText(
                                context,
                                "Trancón reportado",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFFFFF7ED
                            ), contentColor = Color(0xFFC2410C)
                        )
                    ) {
                        Text("🚦  Trancón")
                    }
                    Button(
                        onClick = {
                            contadorAlertas++
                            val incidencia = IncidenciaOficial(
                                id_usuario = auth.currentUser?.uid ?: "",
                                descripcion = "Desvío",
                                estado = "pendiente",
                                fecha_reporte = System.currentTimeMillis()
                            )
                            database.getReference("incidencias").push()
                                .setValue(incidencia)
                            Toast.makeText(
                                context,
                                "Desvío reportado",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f).padding(start = 6.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFFFEFCE8
                            ), contentColor = Color(0xFFA16207)
                        )
                    ) {
                        Text("🔄  Desvío")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fila 2 de Reportes
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            contadorAlertas++
                            val incidencia = IncidenciaOficial(
                                id_usuario = auth.currentUser?.uid ?: "",
                                descripcion = "Fallo Mecánico",
                                estado = "pendiente",
                                fecha_reporte = System.currentTimeMillis()
                            )
                            database.getReference("incidencias").push()
                                .setValue(incidencia)
                            Toast.makeText(
                                context,
                                "Fallo reportado",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFFF1F5F9
                            ), contentColor = Color(0xFF475569)
                        )
                    ) {
                        Text("🔧  Fallo")
                    }
                    Button(
                        onClick = {
                            contadorAlertas++
                            val incidencia = IncidenciaOficial(
                                id_usuario = auth.currentUser?.uid ?: "",
                                descripcion = "Pánico / Emergencia",
                                estado = "urgente",
                                fecha_reporte = System.currentTimeMillis()
                            )
                            database.getReference("incidencias").push()
                                .setValue(incidencia)
                            Toast.makeText(
                                context,
                                "Alerta de pánico enviada",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.weight(1f).padding(start = 6.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFFFEF2F2
                            ), contentColor = Color(0xFFB91C1C)
                        )
                    ) {
                        Text("🚨  Pánico")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Cerrar Sesión minimalista
                OutlinedButton(
                    onClick = {
                        transmitiendo = false
                        val intent = Intent(context, ServicioGpsConductor::class.java)
                        context.stopService(intent)
                        onCerrarSesion()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(
                            0xFFF8FAFC
                        ), contentColor = Color(0xFF334155)
                    ),
                    border = null
                ) {
                    Text("→] Cerrar Sesión", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // 1. EL MAPA DE FONDO
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties.copy(isMyLocationEnabled = false) // Ocultar el punto azul nativo de Google para no duplicar marcadores
            ) {
                // Dibujar la ruta ideal cortada (Dashed)
                if (rutaIdeal.isNotEmpty()) {
                    com.google.maps.android.compose.Polyline(
                        points = rutaIdeal,
                        color = Color(0xFF6396E3), // Color azul claro/grisáceo
                        width = 10f,
                        pattern = listOf(Dash(20f), Gap(20f)) // Efecto línea punteada
                    )
                }

                // Dibujar el trazo recorrido (Línea continua)
                if (rutaRecorrida.isNotEmpty()) {
                    val rastroCompleto = if (ubicacionFluida != null) {
                        rutaRecorrida + ubicacionFluida!!
                    } else {
                        rutaRecorrida
                    }

                    if (rastroCompleto.size > 1) {
                        com.google.maps.android.compose.Polyline(
                            points = rastroCompleto,
                            color = Color(0xFF2563EB), // Azul oscuro sólido
                            width = 16f,
                            zIndex = 1.5f
                        )
                    }
                }

                // MARCADOR DEL PROPIO CONDUCTOR (Usando la MISMA variable ubicacionFluida que dibuja el rastro)
                if (ubicacionFluida != null) {
                    Marker(
                        state = MarkerState(position = ubicacionFluida!!),
                        title = "Mi Vehículo",
                        icon = iconoBus,
                        anchor = androidx.compose.ui.geometry.Offset(
                            0.5f,
                            0.5f
                        ), // Centrar el ícono sobre la coordenada exacta
                        zIndex = 2.0f
                    )
                }

                busesEnZona.forEach { (idBus, destino) ->
                    key(idBus) {
                        MarcadorBusAnimado(
                            idBus = idBus,
                            destinoLatLng = destino,
                            icono = iconoBus,
                            textoEta = "Compañero"
                        )
                    }
                }

                incidenciasActivas.forEach { incidencia ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                incidencia.latitud,
                                incidencia.longitud
                            )
                        ),
                        title = "⚠️ Alerta Reportada",
                        snippet = incidencia.descripcion,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_ORANGE
                        )
                    )
                }
            }

            // 2. INDICADOR DE ESTADO SUPERIOR (Píldora)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ), // Ajuste estético
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Aquí iría tu logo de PopaBus
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2563EB),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.DirectionsBus,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Popa",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = 20.sp
                    )
                    Text(
                        "Bus",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D9488),
                        fontSize = 20.sp
                    )
                }

                // Píldora de estado
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (enRecorrido) Color(0xFF2563EB) else Color(
                                        0xFF0D9488
                                    ), CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (enRecorrido) "En recorrido" else "Disponible",
                            color = if (enRecorrido) Color(0xFF2563EB) else Color(
                                0xFF0D9488
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapaPopabus(
    onCerrarSesion: () -> Unit,
    onIrALogin: () -> Unit = {},
    onIrARegistro: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }
    var permisoConcedido by remember { mutableStateOf(false) }

    var pestañaActual by remember { mutableStateOf("INICIO") }
    // Memoria para saber qué ruta tocó el usuario
    var idRutaSeleccionada by remember { mutableStateOf<String?>(null) }
    
    // [INICIO BLOQUE - ESTADO ALERTA PROXIMIDAD]
    var alertaEmitida by remember { mutableStateOf(false) }
    
    LaunchedEffect(idRutaSeleccionada) {
        if (idRutaSeleccionada == null) {
            alertaEmitida = false
        }
    }
    // [FIN BLOQUE - ESTADO ALERTA PROXIMIDAD]
    
    // ESTADO PARA LA VISTA ACTUAL DE AYUDA (Sugerencias, Acerca De, Preguntas)
    var vistaAyudaActual by rememberSaveable { mutableStateOf<String?>(null) }

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
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    var busesEnZona by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }
    val iconoBus = remember { obtenerIconoBus(context, R.drawable.ic_bus_nuevo) }

    var rutasOficiales by remember { mutableStateOf<List<RutaOficial>>(emptyList()) }
    var paraderosOficiales by remember { mutableStateOf<List<ParaderoOficial>>(emptyList()) }
    
    // ESTADOS DEL PLANIFICADOR
    var mostrandoPlanificador by remember { mutableStateOf(false) }
    var inventarioBuses by remember { mutableStateOf<List<BusOficial>>(emptyList()) }

    // Paleta de colores distintivos para cada ruta
    val paletaColores = remember {
        listOf(
            Color(0xFF1E88E5), // Azul
            Color(0xFF2E7D32), // Verde
            Color(0xFF8E24AA), // Morado
            Color(0xFFFB8C00), // Naranja
            Color(0xFF00897B)  // Verde Azulado
        )
    }

    var mostrarDialogoIncidencia by remember { mutableStateOf(false) }
    val auth = remember { FirebaseAuth.getInstance() }
    val focusManager = LocalFocusManager.current

    val rutasProcesadas = remember(rutasOficiales) {
        rutasOficiales.filter {
            it.estado == "activa" &&
                    it.geometria.isNotEmpty() &&
                    (it.id.equals(
                        "RUTA_RECORRIDO_1",
                        ignoreCase = true
                    ) || it.id.equals("RUTA_RECORRIDO_2", ignoreCase = true))
        }
    }

    val paraderosProcesados = remember(paraderosOficiales, rutasProcesadas) {
        val lista = paraderosOficiales.toMutableList()
        val idPrimeraRuta = rutasProcesadas.firstOrNull()?.id ?: ""
        if (lista.none { it.nombre.equals("Campanario", ignoreCase = true) }) {
            lista.add(
                ParaderoOficial(
                    id = "paradero_campanario_test",
                    nombre = "Campanario",
                    direccion = "Norte",
                    latitud = 2.4593,
                    longitud = -76.5992,
                    estado = "activo",
                    id_ruta = idPrimeraRuta
                )
            )
        }
        lista
    }

    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val prefijoPasajero = calcularGeohash(popayan.latitude, popayan.longitude, 4)

        Log.d(
            "FiltroEspacial",
            "1. Pasajero buscando buses con Geohash que empiece por: $prefijoPasajero"
        )

        val query = database.getReference("BusesActivos")
            .orderByChild("geohash")
            .startAt(prefijoPasajero)
            .endAt(prefijoPasajero + "\uf8ff")

        query.keepSynced(true)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val busesRecibidos = mutableMapOf<String, LatLng>()
                Log.d(
                    "FiltroEspacial",
                    "2. Firebase respondió con ${snapshot.childrenCount} buses"
                )

                for (busSnapshot in snapshot.children) {
                    val idBus = busSnapshot.key ?: continue

                    val latObj = busSnapshot.child("latitud").value
                    val lngObj = busSnapshot.child("longitud").value
                    val geohashBus = busSnapshot.child("geohash").value

                    val lat =
                        (latObj as? Number)?.toDouble() ?: latObj?.toString()?.toDoubleOrNull()
                    val lng =
                        (lngObj as? Number)?.toDouble() ?: lngObj?.toString()?.toDoubleOrNull()

                    Log.d(
                        "FiltroEspacial",
                        "-> Procesando $idBus | Lat: $lat, Lng: $lng, Hash: $geohashBus"
                    )

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
        val refBuses = database.getReference("buses")

        val listenerBuses = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val buses = mutableListOf<BusOficial>()
                for (hijo in snapshot.children) {
                    hijo.getValue(BusOficial::class.java)?.let { buses.add(it.copy(id = hijo.key ?: "")) }
                }
                inventarioBuses = buses
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        refBuses.addValueEventListener(listenerBuses)

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

                    val estadoRaw =
                        hijo.child("estado").getValue(String::class.java)?.trim()?.lowercase()
                            ?: "activa"

                    val geometriaNode =
                        if (hijo.child("geometria").exists()) hijo.child("geometria")
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

                        val lat = (latObj as? Number)?.toDouble() ?: latObj?.toString()
                            ?.toDoubleOrNull() ?: 0.0
                        val lng = (lngObj as? Number)?.toDouble() ?: lngObj?.toString()
                            ?.toDoubleOrNull() ?: 0.0

                        if (lat != 0.0 && lng != 0.0) {
                            geometriaList.add(CoordenadaRuta(lat, lng))
                        }
                    }

                    Log.d(
                        "Infraestructura",
                        "-> Ruta: $nombre | Estado: $estadoRaw | Puntos válidos: ${geometriaList.size}"
                    )

                    listaRutas.add(
                        RutaOficial(
                            id = idRuta,
                            nombre_ruta = nombre,
                            origen = origen,
                            destino = destino,
                            estado = estadoRaw,
                            geometria = geometriaList
                        )
                    )
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
            refBuses.removeEventListener(listenerBuses)
            refParaderos.removeEventListener(listenerParaderos)
            refRutas.removeEventListener(listenerRutas)
        }
    }

    val mapProperties = MapProperties(isMyLocationEnabled = permisoConcedido)

    Scaffold(
        bottomBar = {
            if (idRutaSeleccionada == null) {
                NavigationBar(

                    containerColor = Color.White,
                    tonalElevation = 4.dp // o el valor que tengas actualmente
                ) {
                    NavigationBarItem(
                        icon = {
                            Box(contentAlignment = Alignment.TopCenter) {
                                // La línea azul condicional
                                if (pestañaActual == "INICIO") {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-16).dp) // Ajusta este número (ej. -12.dp o -18.dp) hasta que toque el borde
                                            .width(32.dp)
                                            .height(3.dp)
                                            .background(Color(0xFF2563EB), RoundedCornerShape(50))
                                    )
                                }
                                // El ícono estático debajo
                                Icon(Icons.Outlined.Home, contentDescription = "Inicio")
                            }
                        },
                        label = { Text("Inicio", fontSize = 10.sp) },
                        selected = pestañaActual == "INICIO",
                        onClick = { pestañaActual = "INICIO" },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent, // Esto elimina el fondo gris/azul oscuro
                            selectedIconColor = Color(0xFF2563EB),
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFF2563EB),
                            unselectedTextColor = Color.Gray
                        )
                    )
                    NavigationBarItem(
                        icon = {
                            Box(contentAlignment = Alignment.TopCenter) {
                                // La línea azul condicional
                                if (pestañaActual == "ACTIVIDAD") {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-16).dp) // Ajusta este número (ej. -12.dp o -18.dp) hasta que toque el borde
                                            .width(32.dp)
                                            .height(3.dp)
                                            .background(Color(0xFF2563EB), RoundedCornerShape(50))
                                    )
                                }
                                // El ícono estático debajo
                                Icon(Icons.Default.Schedule, contentDescription = "Actividad")
                            }
                        },
                        selected = pestañaActual == "ACTIVIDAD",
                        onClick = { pestañaActual = "ACTIVIDAD" },
                        label = { Text("Actividad") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2563EB),
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFF2563EB),
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        icon = {
                            Box(contentAlignment = Alignment.TopCenter) {
                                // La línea azul condicional para CUENTA
                                if (pestañaActual == "CUENTA") {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-16).dp) // Usa el mismo valor que pusiste en Inicio y Actividad
                                            .width(32.dp)
                                            .height(3.dp)
                                            .background(Color(0xFF2563EB), RoundedCornerShape(50))
                                    )
                                }
                                // Ícono de líneas puras
                                Icon(Icons.Outlined.Person, contentDescription = "Cuenta")
                            }
                        },
                        label = { Text("Cuenta", fontSize = 10.sp) },
                        selected = pestañaActual == "CUENTA",
                        onClick = { pestañaActual = "CUENTA" },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent, // Elimina el óvalo de fondo
                            selectedIconColor = Color(0xFF2563EB), // Pinta solo las líneas de azul
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFF2563EB),
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        when (pestañaActual) {
            "INICIO" -> {
                if (mostrandoPlanificador) {
                    PantallaPlanificaViaje(
                        ubicacionPasajero = ubicacionPasajero,
                        rutasProcesadas = rutasOficiales,
                        paraderosProcesados = paraderosOficiales,
                        busesEnZona = busesEnZona,
                        onVolver = { mostrandoPlanificador = false },
                        onRutaSeleccionada = { idRuta ->
                            idRutaSeleccionada = idRuta            // 1. Dispara el modo aislamiento en el mapa
                            mostrandoPlanificador = false          // 2. Apaga el planificador para volver a la vista del mapa
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        LaunchedEffect(permisoConcedido) {
                        if (permisoConcedido) {
                            val intent = Intent(context, ServicioRastreoPasajero::class.java)
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                ContextCompat.startForegroundService(context, intent)
                            } else {
                                context.startService(intent)
                            }
                        }
                    }

                // 1. El Mapa va al fondo
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    // AGREGA ESTA LÍNEA EXACTA AQUÍ:
                    uiSettings = com.google.maps.android.compose.MapUiSettings(
                        myLocationButtonEnabled = false,
                        zoomControlsEnabled = false
                    )
                ) {
// --- NUEVO SPRINT: RENDERIZADO INTELIGENTE DE RUTAS ---
                    if (idRutaSeleccionada != null) {
                        rutasProcesadas.forEachIndexed { index, ruta ->
                            val colorBase = paletaColores[index % paletaColores.size]
                            val estaSeleccionada = (idRutaSeleccionada == ruta.id)

                            if (estaSeleccionada) {
                                val puntosLinea =
                                    ruta.geometria.map { LatLng(it.latitud, it.longitud) }

                                com.google.maps.android.compose.Polyline(
                                    points = puntosLinea,
                                    color = colorBase,
                                    width = 22f,
                                    zIndex = 2f,
                                    clickable = true,
                                    onClick = {
                                        // Al tocar la línea de nuevo, vuelve a mostrar todas o deselecciona.
                                        idRutaSeleccionada = null
                                    }
                                )
                            }
                        }
                    }

                    val paraderosRegulares =
                        if (idRutaSeleccionada != null && idRutaSeleccionada != "NINGUNA") {
                            val paraderosRuta = paraderosOficiales.filter {
                                it.estado == "activo" && it.id_ruta == idRutaSeleccionada
                            }
                            if (paraderosRuta.size <= 15) {
                                paraderosRuta
                            } else {
                                (0 until 15).map { i ->
                                    val indice = (i * (paraderosRuta.size - 1) / 14.0).toInt()
                                    paraderosRuta[indice]
                                }
                            }
                        } else {
                            emptyList()
                        }

                    paraderosRegulares.forEach { paradero ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    paradero.latitud,
                                    paradero.longitud
                                )
                            ),
                            title = paradero.nombre,
                            snippet = paradero.direccion,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                    }

                    busesEnZona.forEach { (idBus, destino) ->
                        val busOficial = inventarioBuses.find { it.id == idBus }
                        val rutaDelBus = busOficial?.id_ruta

                        val mostrarEsteBus = idRutaSeleccionada == null || idRutaSeleccionada == rutaDelBus

                        if (mostrarEsteBus) {
                            key(idBus) {
                                MarcadorBusAnimado(
                                    idBus = idBus,
                                    destinoLatLng = destino,
                                    icono = iconoBus,
                                    textoEta = "En camino"
                                )
                            }
                        }
                    }
                }

// 2. ELEMENTOS DE UI CONDICIONALES SOBRE EL MAPA
                if (idRutaSeleccionada == null) {
                    // MODO GENERAL (NO AISLADO)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .zIndex(2f) // Asegura que esté por encima del mapa
                    ) {
                        // 1. Efecto Degradado Blanco Superior
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp) // Cubre la zona superior del mapa
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color.White.copy(alpha = 0.8f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // 2. Contenedor del Buscador y Radar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Saludo Dinámico desde Realtime Database
                            var nombreUsuario by remember { mutableStateOf("Cargando...") }

                            LaunchedEffect(Unit) {
                                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                if (uid != null) {
                                    com.google.firebase.database.FirebaseDatabase.getInstance()
                                        .getReference("usuarios").child(uid).child("nombre_completo").get()
                                        .addOnSuccessListener { snapshot ->
                                            val nombreCompleto = snapshot.getValue(String::class.java) ?: "Usuario"
                                            nombreUsuario = nombreCompleto.split(" ")[0] // Corta hasta el primer espacio
                                        }
                                } else {
                                    nombreUsuario = "Usuario"
                                }
                            }

                            Text(
                                text = "Buenos días, $nombreUsuario",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp, start = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )

                            // Buscador Altamente Redondeado
                            Surface(
                                shape = RoundedCornerShape(50), // Máximo redondeo
                                color = Color.White,
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable { mostrandoPlanificador = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = Color(0xFF2563EB), // Lupa azul intensa
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "¿A dónde vas?",
                                        color = Color.Black, // Texto negro
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tarjeta de buses en radar (Píldora con sombra)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White,
                                shadowElevation = 6.dp,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Punto Verde con Efecto de Sombra/Resplandor Verde
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .shadow(
                                                elevation = 8.dp,
                                                shape = CircleShape,
                                                spotColor = Color(0xFF00BFA5), // Tonalidad verde para la sombra
                                                ambientColor = Color(0xFF00BFA5)
                                            )
                                            .background(Color(0xFF00BFA5), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Buses en radar: ${busesEnZona.size}", // Contador Dinámico restaurado
                                        color = Color(0xFF00BFA5), // Color del texto ahora es verde
                                        fontSize = 14.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // MODO AISLAMIENTO DE RUTA (Línea Azul)
                    val rutaSeleccionadaObj = rutasProcesadas.find { it.id == idRutaSeleccionada }
                    if (rutaSeleccionadaObj != null) {
                        // Top Start Row
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                                .zIndex(2f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { idRutaSeleccionada = null }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color(0xFF1E293B),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White,
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        start = 4.dp,
                                        end = 16.dp,
                                        top = 4.dp,
                                        bottom = 4.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val numRuta = rutaSeleccionadaObj.nombre_ruta.filter { it.isDigit() }
                                        .ifEmpty { rutaSeleccionadaObj.nombre_ruta.take(1) }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFF2563EB), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            numRuta,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Hacia ${rutaSeleccionadaObj.destino}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Esta Card reemplaza a la tarjeta flotante anterior (PASO 2 y 3 consolidados)
// --- INICIO DE LA NUEVA TARJETA ESTÁTICA ---

                        // 1. Cálculos en tiempo real desde Firebase
                        val cantidadParadas = paraderosOficiales.count { paradero ->
                            paradero.id_ruta == idRutaSeleccionada && paradero.estado == "activo"
                        }

                        // Filtramos de TODOS los buses activos (busesEnZona) SOLO los que pertenecen a la ruta aislada
                        val busesDeRutaAislada = inventarioBuses.filter { it.id_ruta == idRutaSeleccionada }.map { it.id }
                        // Extraemos las coordenadas precisas de esos buses, descartando cualquier bus que no esté en la lista anterior
                        val ubicacionesBusesAislados = busesEnZona.filterKeys { it in busesDeRutaAislada }.values

                        val etaAislada = when {
                            ubicacionPasajero == null -> "Ubicando..."
                            ubicacionesBusesAislados.isEmpty() -> "Sin buses"
                            else -> {
                                val distanciaMinima = ubicacionesBusesAislados.minOf { ubicacionBus ->
                                    calcularDistanciaMetros(ubicacionPasajero!!, ubicacionBus)
                                }
                                obtenerTextoETA(distanciaMinima)
                            }
                        }

                        // [INICIO BLOQUE INYECTADO - GATILLO ALERTA PROXIMIDAD]
                        val sharedPrefs = context.getSharedPreferences("PreferenciasPopabus", Context.MODE_PRIVATE)
                        val alertasActivas = sharedPrefs.getBoolean("alertasProximidad", true)

                        LaunchedEffect(ubicacionesBusesAislados, ubicacionPasajero, alertasActivas) {
                            if (alertasActivas && ubicacionPasajero != null && ubicacionesBusesAislados.isNotEmpty()) {
                                // Calculamos en base a la fórmula requerida de forma local (sin afectar tu lógica de interfaz)
                                val distanciaMin = ubicacionesBusesAislados.minOf { 
                                    calcularDistanciaMetros(ubicacionPasajero!!, it) 
                                }
                                val etaMinutos = (distanciaMin / 250f).toInt()
                                
                                if (etaMinutos <= 3 && !alertaEmitida) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                        val vibrator = vibratorManager.defaultVibrator
                                        val effect = VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val attributes = VibrationAttributes.Builder()
                                                .setUsage(VibrationAttributes.USAGE_ALARM)
                                                .build()
                                            vibrator.vibrate(effect, attributes)
                                        } else {
                                            val audioAttributes = AudioAttributes.Builder()
                                                .setUsage(AudioAttributes.USAGE_ALARM)
                                                .build()
                                            vibrator.vibrate(effect, audioAttributes)
                                        }
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                        val effect = VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE)
                                        val audioAttributes = AudioAttributes.Builder()
                                            .setUsage(AudioAttributes.USAGE_ALARM)
                                            .build()
                                        vibrator.vibrate(effect, audioAttributes)
                                    } else {
                                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                        val audioAttributes = AudioAttributes.Builder()
                                            .setUsage(AudioAttributes.USAGE_ALARM)
                                            .build()
                                        @Suppress("DEPRECATION")
                                        vibrator.vibrate(5000, audioAttributes)
                                    }
                                    alertaEmitida = true // Candado one-shot
                                }
                            }
                        }
                        // [FIN BLOQUE INYECTADO]

                        // 2. Contenedor anclado al fondo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            TarjetaRutaAisladaFija(
                                nombreRuta = rutaSeleccionadaObj.nombre_ruta,
                                origenDestino = "${rutaSeleccionadaObj.origen} → ${rutaSeleccionadaObj.destino}",
                                hayBusesActivos = ubicacionesBusesAislados.isNotEmpty(),
                                cantidadParadas = cantidadParadas,
                                etaActual = etaAislada,
                                onVerRutaCompleta = { idRutaSeleccionada = null }
                            )
                        }
                        // --- FIN DE LA NUEVA TARJETA ESTÁTICA ---
                    }
                }

                // DIÁLOGOS (SOS e Incidencia)
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

                            refIncidencias.push().setValue(nuevaIncidencia)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Incidencia reportada. ¡Gracias!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    mostrarDialogoIncidencia = false
                                }.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Error al enviar reporte",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
                // BOTÓN SOS ÚNICO Y ESTILIZADO (Oculto en modo aislamiento)
                if (idRutaSeleccionada == null) {
                    FloatingActionButton(
                        onClick = { mostrarDialogoPanico = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp, end = 16.dp)
                            .border(3.dp, Color.White, CircleShape), // Borde blanco de 3.dp
                        shape = CircleShape,
                        containerColor = Color(0xFFE53935),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 8.dp, // Sombra inferior
                            pressedElevation = 12.dp
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.GppGood, contentDescription = "SOS", tint = Color.White, modifier = Modifier.size(24.dp))
                            Text("SOS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

// --- TAREA 15: BOTÓN DE PÁNICO DEL PASAJERO (REDISEÑO PREMIUM) ---
                        if (mostrarDialogoPanico) {
                            // Estado para la cuenta regresiva
                            var tiempoRestante by remember { mutableIntStateOf(3) }
                            val context = LocalContext.current

                            // Lógica intacta de Firebase aislada en una función para llamarla manual o automáticamente
                            val enviarAlerta = {
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
                            }

                            // Temporizador: Descuenta cada segundo y envía al llegar a 0
                            LaunchedEffect(Unit) {
                                while (tiempoRestante > 0) {
                                    kotlinx.coroutines.delay(1000)
                                    tiempoRestante--
                                }
                                if (tiempoRestante == 0) {
                                    enviarAlerta()
                                }
                            }

                            androidx.compose.ui.window.Dialog(
                                onDismissRequest = { mostrarDialogoPanico = false },
                                properties = androidx.compose.ui.window.DialogProperties(
                                    dismissOnBackPress = false,
                                    dismissOnClickOutside = false
                                )
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // 1. Ícono de Escudo Rojo
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color(0xFFDC2626), // Rojo sólido
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Security, // Usa tu icono de escudo
                                                contentDescription = "Alerta",
                                                tint = Color.White,
                                                modifier = Modifier.padding(14.dp).fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // 2. Títulos
                                        Text(
                                            text = "ALERTA SOS",
                                            color = Color(0xFFDC2626),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.5.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Enviando alerta en",
                                            color = Color(0xFF1E293B),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "$tiempoRestante segundos...",
                                            color = Color(0xFFDC2626),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // 3. Descripción
                                        Text(
                                            text = "Compartiremos tu ubicación actual con las autoridades.",
                                            color = Color(0xFF64748B),
                                            fontSize = 13.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(32.dp))

                                        // 4. Botones Apilados
                                        OutlinedButton(
                                            onClick = { mostrarDialogoPanico = false },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2563EB))
                                        ) {
                                            Text("Cancelar", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = { enviarAlerta() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                        ) {
                                            Text("Enviar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        // ----------------------------------------------

                // INYECCIÓN DE LAS VISTAS DE AYUDA (Se superponen al mapa)
                if (vistaAyudaActual == "ACERCA") {
                    VistaAcercaDe(onDismiss = { vistaAyudaActual = null })
                } else if (vistaAyudaActual == "PREGUNTAS") {
                    VistaPreguntasFrecuentes(onDismiss = { vistaAyudaActual = null })
                } else if (vistaAyudaActual == "SUGERENCIAS") {
                    VistaSugerencias(onDismiss = { vistaAyudaActual = null })
                }
            } // Fin de Box original de mapa
            } // Fin de else (mostrandoPlanificador)
            } // Fin de Box de INICIO
            "ACTIVIDAD" -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    var listaActividades by remember {
                        mutableStateOf<List<ActividadUsuario>>(
                            emptyList()
                        )
                    }

                    DisposableEffect(Unit) {
                        val uid = auth.currentUser?.uid ?: ""
                        val refHistorial =
                            FirebaseDatabase.getInstance().getReference("actividad_usuario")
                                .child(uid)

                        val listener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val actividades = mutableListOf<ActividadUsuario>()
                                for (actividadSnapshot in snapshot.children) {
                                    val actividad =
                                        actividadSnapshot.getValue(ActividadUsuario::class.java)
                                    if (actividad != null) {
                                        actividades.add(actividad)
                                    }
                                }
                                // Ordenamos de la más reciente a la más antigua
                                listaActividades =
                                    actividades.sortedByDescending { it.fecha_abordaje }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(
                                    "Historial",
                                    "Error al leer historial",
                                    error.toException()
                                )
                            }
                        }
                        refHistorial.addValueEventListener(listener)

                        onDispose { refHistorial.removeEventListener(listener) }
                    }

                    VistaActividad(
                        listaActividades = listaActividades,
                        hayInternet = true,
                        onDismiss = { pestañaActual = "INICIO" }
                    )
                }
            }
            "CUENTA" -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    PantallaCuentaPasajero(
                        onCerrarSesion = onCerrarSesion,
                        onVolverAlInicio = { pestañaActual = "INICIO" },
                        onOpcionSeleccionada = { opcion ->
                            when (opcion) {
                                "Acerca de PopaBus" -> vistaAyudaActual = "ACERCA"
                                "Preguntas Frecuentes" -> vistaAyudaActual = "PREGUNTAS"
                                "Sugerencias" -> vistaAyudaActual = "SUGERENCIAS"
                                "Reportar" -> mostrarDialogoIncidencia = true
                            }
                        },
                        onIrALogin = onIrALogin,
                        onIrARegistro = onIrARegistro
                    )
                }
            }
        }
    }
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
        title = {
            Text(
                "Preguntas Frecuentes",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text("¿Cómo busco una ruta?", color = MaterialTheme.colorScheme.primary)
                Text("Usa la barra de búsqueda en la parte superior para ingresar el nombre de la ruta, origen o destino.")
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "¿Cómo reporto un problema?",
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Toca el botón '⚠️ Reportar' en la esquina inferior derecha para avisarnos de accidentes, desvíos o trancones.")
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "¿Cómo veo los buses en tiempo real?",
                    color = MaterialTheme.colorScheme.primary
                )
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
        title = {
            Text(
                "Buzón de Sugerencias",
                style = MaterialTheme.typography.titleLarge
            )
        },
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
                        Toast.makeText(
                            context,
                            "¡Gracias por tu sugerencia!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error al enviar", Toast.LENGTH_SHORT)
                            .show()
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
fun VistaActividad(
    listaActividades: List<ActividadUsuario>,
    hayInternet: Boolean,
    onDismiss: () -> Unit = {}
) {
    // 1. Configuramos el ColorMatrix para la escala de grises
    val colorMatrix = remember(hayInternet) {
        val matrix = ColorMatrix()
        if (!hayInternet) {
            matrix.setToSaturation(0f) // 0f elimina toda la saturación (blanco y negro)
        }
        matrix
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Si no hay internet, inyectamos el filtro a TODA la vista hija
                if (!hayInternet) {
                    colorFilter = ColorFilter.colorMatrix(colorMatrix)
                }
            }
    ) {
        // Cabecera Blanca Estática
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Actividad",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
        }

        // Contenedor principal para el resto de la pantalla con fondo gris
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            Text(
                text = "TUS VIAJES RECIENTES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            )

            if (listaActividades.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Aún no tienes actividad reciente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(listaActividades) { actividad ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            val fechaFormateada = remember(actividad.fecha_abordaje) {
                                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                                sdf.format(Date(actividad.fecha_abordaje))
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Bloque Izquierdo (50%)
                                Row(
                                    modifier = Modifier.weight(0.70f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Ícono
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timeline,
                                            contentDescription = "Viaje",
                                            tint = Color(0xFF2563EB)
                                        )
                                    }

                                    // Textos (Destino, Trayecto, Fecha)
                                    Column(
                                        modifier = Modifier.padding(start = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = actividad.paradero_origen.trim(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Trayecto de ${actividad.duracionMinutos} min",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = fechaFormateada,
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                // 2. Espaciador (25%)
                                Spacer(modifier = Modifier.weight(0.25f))

                                // 3. Bloque Derecho (25%) - Píldora de Ruta
                                Surface(
                                    modifier = Modifier
                                        .weight(0.30f) // CAMBIO 2: Toma el 30% restante
                                        .align(Alignment.Top) // CAMBIO 3: Lo empuja a la parte superior de la tarjeta
                                        .wrapContentWidth(Alignment.End), // CAMBIO 4: Pega la píldora al borde derecho
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFFE6FFFA)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${actividad.id_ruta}",
                                            color = Color(0xFF0D9488),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PantallaAdministrador(onCerrarSesion: () -> Unit) {
    var subPantalla by remember { mutableStateOf("MENU") }

    // --- NUEVO: ESTADO PARA EL NOMBRE DEL ADMINISTRADOR ---
    var nombreAdmin by remember { mutableStateOf("Cargando...") }
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }

    // --- NUEVO: EFECTO PARA DESCARGAR EL NOMBRE REAL ---
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.getReference("usuarios").child(uid).child("nombre_completo").get()
                .addOnSuccessListener { snapshot ->
                    val nombreReal = snapshot.getValue(String::class.java)
                    if (nombreReal != null) {
                        nombreAdmin = nombreReal
                    } else {
                        nombreAdmin = "Administrador" // Fallback seguro
                    }
                }
                .addOnFailureListener {
                    nombreAdmin = "Administrador" // Fallback si falla la conexión
                }
        } else {
            nombreAdmin = "Administrador Invitado"
        }
    }

// --- NUEVO: ESTRUCTURA SCAFFOLD PARA LA BARRA INFERIOR ---
    Scaffold(
        bottomBar = {
            // SOLO mostramos la barra si NO estamos en el Menú principal
            if (subPantalla != "MENU") {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        icon = {
                            Box(contentAlignment = Alignment.TopCenter) {
                                // La línea azul condicional
                                if (subPantalla == "MENU") {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-16).dp) // Ajusta este número (ej. -12.dp o -18.dp) hasta que toque el borde
                                            .width(32.dp)
                                            .height(3.dp)
                                            .background(Color(0xFF2563EB), RoundedCornerShape(50))
                                    )
                                }
                                // El ícono estático debajo
                                Icon(Icons.Outlined.Home, contentDescription = "Inicio")
                            }
                        },
                        label = { Text("Inicio", fontSize = 10.sp) },
                        selected = subPantalla == "MENU",
                        onClick = { subPantalla = "MENU" },

                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent, // Esto elimina el fondo gris/azul oscuro
                            selectedIconColor = Color(0xFF2563EB),
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFF2563EB),
                            unselectedTextColor = Color.Gray
                        )
                    )


                    // Atajo 2: Reportes
                    NavigationBarItem(
                        icon = {
                            Box(contentAlignment = Alignment.TopCenter) {
                                // La línea azul condicional
                                if (subPantalla == "REPORTES") {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-16).dp) // Ajusta este número (ej. -12.dp o -18.dp) hasta que toque el borde
                                            .width(32.dp)
                                            .height(3.dp)
                                            .background(Color(0xFF2563EB), RoundedCornerShape(50))
                                    )
                                }
                                // El ícono estático debajo
                                Icon(Icons.Default.Inbox, contentDescription = "Reportes")
                            }
                        },
                        label = { Text("Reportes", fontSize = 10.sp) },
                        selected = subPantalla == "REPORTES",
                        onClick = { subPantalla = "REPORTES" },

                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent, // Esto elimina el fondo gris/azul oscuro
                            selectedIconColor = Color(0xFF2563EB),
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFF2563EB),
                            unselectedTextColor = Color.Gray
                        )
                    )

                    // Atajo 3: Mapa (Torre de Control)
                    NavigationBarItem(

                        icon = {
                            Box(contentAlignment = Alignment.TopCenter) {
                                // La línea azul condicional
                                if (subPantalla == "TORRE_CONTROL") {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-16).dp) // Ajusta este número (ej. -12.dp o -18.dp) hasta que toque el borde
                                            .width(32.dp)
                                            .height(3.dp)
                                            .background(Color(0xFF2563EB), RoundedCornerShape(50))
                                    )
                                }
                                // El ícono estático debajo
                                Icon(Icons.Default.LocationOn, contentDescription = "Mapa")
                            }
                        },
                        label = { Text("Mapa", fontSize = 10.sp) },
                        selected = subPantalla == "TORRE_CONTROL",
                        onClick = { subPantalla = "TORRE_CONTROL" },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent, // Esto elimina el fondo gris/azul oscuro
                            selectedIconColor = Color(0xFF2563EB),
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFF2563EB),
                            unselectedTextColor = Color.Gray
                        )
                    )
                    // Atajo 4: Informes
                    NavigationBarItem(
                        icon = {
                            Box(contentAlignment = Alignment.TopCenter) {
                                // La línea azul condicional
                                if (subPantalla == "INFORMES") {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-16).dp) // Ajusta este número (ej. -12.dp o -18.dp) hasta que toque el borde
                                            .width(32.dp)
                                            .height(3.dp)
                                            .background(Color(0xFF2563EB), RoundedCornerShape(50))
                                    )
                                }
                                // El ícono estático debajo
                                Icon(Icons.Default.BarChart, contentDescription = "Informes")
                            }
                        },
                        label = { Text("Informes", fontSize = 10.sp) },
                        selected = subPantalla == "INFORMES",
                        onClick = { subPantalla = "INFORMES" },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent, // Esto elimina el fondo gris/azul oscuro
                            selectedIconColor = Color(0xFF2563EB),
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFF2563EB),
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
// El contenido actual de tus pantallas se renderiza aquí
        Box(modifier = Modifier.padding(paddingValues)) {
            when (subPantalla) {
                "MENU" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8FAFC)) // Color de fondo gris muy claro del mockup
                            // --- CORRECCIÓN: Ajustamos el padding para evitar que el botón quede oculto ---
                            .padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 24.dp,
                                // Reducimos el padding inferior drásticamente porque el Scaffold ya apartó espacio
                                bottom = 0.dp
                            )
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Cabecera
                        // Cabecera
                        Text(
                            text = "POPABUS",
                            color = Color(0xFF0D9488),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Panel de\nAdministración",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                lineHeight = 30.sp
                            )

                            // Avatar simulado
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE0E7FF),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        // Mostramos la inicial del nombre real
                                        Text(
                                            text = nombreAdmin.firstOrNull()?.uppercase() ?: "A",
                                            color = Color(0xFF3B82F6),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    // --- AQUÍ MOSTRAMOS EL NOMBRE DINÁMICO ---
                                    Text(text = nombreAdmin, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                                    Text(text = "Administrador", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Subtítulo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "VISTA GENERAL",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "¿Qué deseas gestionar?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("En línea", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Grilla de 6 botones
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Botón 1: Gestión Humana
                            Card(
                                onClick = { subPantalla = "USUARIOS" },
                                modifier = Modifier.weight(1f).aspectRatio(1.2f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                    Text(
                                        "Gestión Humana",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }

                            // Botón 2: Paraderos
                            Card(
                                onClick = { subPantalla = "PARADEROS" },
                                modifier = Modifier.weight(1f).aspectRatio(1.2f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("📍", fontSize = 16.sp)
                                        }
                                    }
                                    Text(
                                        "Gestionar Paraderos",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Botón 3: Rutas
                            Card(
                                onClick = { subPantalla = "RUTAS" },
                                modifier = Modifier.weight(1f).aspectRatio(1.2f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("🗺️", fontSize = 16.sp)
                                        }
                                    }
                                    Text(
                                        "Gestionar Rutas",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }

                            // Botón 4: Buses
                            Card(
                                onClick = { subPantalla = "BUSES" },
                                modifier = Modifier.weight(1f).aspectRatio(1.2f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBus,
                                            contentDescription = null,
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                    Text(
                                        "Gestionar Buses",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Botón 5: Reportes
                            Card(
                                onClick = { subPantalla = "REPORTES" },
                                modifier = Modifier.weight(1f).aspectRatio(1.2f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("📥", fontSize = 16.sp)
                                        }
                                    }
                                    Text(
                                        "Buzón de Reportes",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }

                            // Botón 6: Informes
                            Card(
                                onClick = { subPantalla = "INFORMES" },
                                modifier = Modifier.weight(1f).aspectRatio(1.2f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("📊", fontSize = 16.sp)
                                        }
                                    }
                                    Text(
                                        "Gestión de Informes",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tarjeta Torre de Control
                        Card(
                            onClick = { subPantalla = "TORRE_CONTROL" },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // Verde muy claro
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE0E7FF),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("📡", fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("MONITOREO EN TIEMPO REAL", color = Color(0xFF0D9488), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("Torre de Control - Mapa", color = Color(0xFF1E293B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Botón Cerrar Sesión Rojo
                        Button(
                            onClick = onCerrarSesion,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)) // Rojo intenso
                        ) {
                            Text("→] Cerrar Sesión", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                "PARADEROS" -> PantallaGestionParaderos(onVolver = { subPantalla = "MENU" })
                "RUTAS" -> PantallaGestionRutas(onVolver = { subPantalla = "MENU" })
                "BUSES" -> PantallaGestionBuses(onVolver = { subPantalla = "MENU" })
                "USUARIOS" -> PantallaGestionUsuarios(onVolver = { subPantalla = "MENU" })
                "REPORTES" -> PantallaGestionReportes(onVolver = { subPantalla = "MENU" })
                "INFORMES" -> PantallaGestionInformes(onVolver = { subPantalla = "MENU" })
                "TORRE_CONTROL" -> MapaGeneralAdmin(onVolver = { subPantalla = "MENU" })
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionParaderos(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refParaderos = database.getReference("paraderos")
    val refRutas = database.getReference("rutas")

    var paraderoSeleccionado by remember { mutableStateOf<ParaderoOficial?>(null) }

    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }

    // Estado del Dropdown de Rutas
    var idRuta by remember { mutableStateOf("") }
    var expandedRuta by remember { mutableStateOf(false) }

    // Estados para las listas en tiempo real
    var listaParaderos by remember { mutableStateOf<List<ParaderoOficial>>(emptyList()) }
    var listaRutas by remember { mutableStateOf<List<RutaOficial>>(emptyList()) }

    // Descargamos Paraderos y Rutas de Firebase en tiempo real
    DisposableEffect(Unit) {
        val listenerParaderos = object : ValueEventListener {
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
        val listenerRutas = object : ValueEventListener {
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
            override fun onCancelled(error: DatabaseError) {}
        }

        refParaderos.addValueEventListener(listenerParaderos)
        refRutas.addValueEventListener(listenerRutas)

        onDispose {
            refParaderos.removeEventListener(listenerParaderos)
            refRutas.removeEventListener(listenerRutas)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        // Cabecera idéntica al mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = CircleShape)
                    .clickable { onVolver() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF1E293B)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POPABUS · SOTRACAUCA",
                    color = Color(0xFF0D9488),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Paraderos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PP", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            item {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                // Textos de subtítulo
                Text(
                    text = "RED DE PARADAS",
                    color = Color(0xFF0D9488),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Crea y consulta los puntos autorizados.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // FORMULARIO DE CREACIÓN
                Text(
                    text = "Nombre",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text("Ej: Terminal Norte", color = Color.LightGray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dirección",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    placeholder = { Text("Ej: Calle 4 # 12-45", color = Color.LightGray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Latitud",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = latitud,
                            onValueChange = { latitud = it },
                            placeholder = { Text("Ej: 2.4419", color = Color.LightGray, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Longitud",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = longitud,
                            onValueChange = { longitud = it },
                            placeholder = { Text("Ej: -76.6063", color = Color.LightGray, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // DROPDOWN NUEVO PARA ASIGNAR RUTA
                Text(
                    text = "Asignar a Ruta Oficial",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val nombreRutaSeleccionada = listaRutas.find { it.id == idRuta }?.nombre_ruta ?: ""

                ExposedDropdownMenuBox(
                    expanded = expandedRuta,
                    onExpandedChange = { expandedRuta = !expandedRuta }
                ) {
                    OutlinedTextField(
                        value = nombreRutaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Ej: Ruta 3", color = Color.LightGray, fontSize = 14.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRuta) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRuta,
                        onDismissRequest = { expandedRuta = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin ruta asignada") },
                            onClick = {
                                idRuta = ""
                                expandedRuta = false
                            }
                        )
                        listaRutas.forEach { ruta ->
                            DropdownMenuItem(
                                text = { Text(ruta.nombre_ruta) },
                                onClick = {
                                    idRuta = ruta.id // Guardamos el ID real en la variable
                                    expandedRuta = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Crear Paradero (Azul vibrante)
                Button(
                    onClick = {
                        val lat = latitud.toDoubleOrNull()
                        val lng = longitud.toDoubleOrNull()

                        if (nombre.isNotEmpty() && lat != null && lng != null) {
                            val nuevoParaderoRef = refParaderos.push()
                            val geohash = calcularGeohash(lat, lng)

                            val nuevoParadero = ParaderoOficial(
                                nombre = nombre,
                                direccion = direccion,
                                latitud = lat,
                                longitud = lng,
                                geohash = geohash,
                                estado = "activo",
                                id_ruta = idRuta.trim() // El ID seleccionado se guarda aquí
                            )

                            nuevoParaderoRef.setValue(nuevoParadero).addOnSuccessListener {
                                Toast.makeText(context, "Paradero Guardado", Toast.LENGTH_SHORT).show()
                                nombre = ""
                                direccion = ""
                                latitud = ""
                                longitud = ""
                                idRuta = ""
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Llene nombre y coordenadas válidas", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("➕ Crear Paradero", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Encabezado de la lista con Badge
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paraderos registrados", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = listaParaderos.size.toString(), color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // LISTA DE PARADEROS
        items(listaParaderos) { paradero ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { paraderoSeleccionado = paradero },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("📍", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = paradero.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                        Text(text = paradero.direccion, fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(text = "${paradero.latitud}, ${paradero.longitud}", fontSize = 10.sp, color = Color(0xFF94A3B8))

                        val rutaVinculada = listaRutas.find { it.id == paradero.id_ruta }?.nombre_ruta
                        if (rutaVinculada != null) {
                            Text(text = "Ruta: $rutaVinculada", fontSize = 10.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                // Botón Volver
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Text("<  Volver al Menú", color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

    // --- DIÁLOGO DE EDICIÓN CON DROPDOWN ---
    if (paraderoSeleccionado != null) {
        val paradero = paraderoSeleccionado!!
        var editNombre by remember(paradero) { mutableStateOf(paradero.nombre) }
        var editDireccion by remember(paradero) { mutableStateOf(paradero.direccion) }
        var editIdRuta by remember(paradero) { mutableStateOf(paradero.id_ruta) }

        var expandedEditRuta by remember { mutableStateOf(false) }

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
                    Spacer(modifier = Modifier.height(8.dp))

                    // DROPDOWN PARA EDICIÓN
                    val nombreEditRuta = listaRutas.find { it.id == editIdRuta }?.nombre_ruta ?: "Sin ruta asignada"

                    ExposedDropdownMenuBox(
                        expanded = expandedEditRuta,
                        onExpandedChange = { expandedEditRuta = !expandedEditRuta }
                    ) {
                        OutlinedTextField(
                            value = nombreEditRuta,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cambiar Ruta") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEditRuta) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEditRuta,
                            onDismissRequest = { expandedEditRuta = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sin ruta asignada") },
                                onClick = {
                                    editIdRuta = ""
                                    expandedEditRuta = false
                                }
                            )
                            listaRutas.forEach { ruta ->
                                DropdownMenuItem(
                                    text = { Text(ruta.nombre_ruta) },
                                    onClick = {
                                        editIdRuta = ruta.id
                                        expandedEditRuta = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Geo: ${paradero.latitud}, ${paradero.longitud}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editNombre.isNotBlank() && editDireccion.isNotBlank()) {
                        val actualizaciones = mapOf(
                            "nombre" to editNombre,
                            "direccion" to editDireccion,
                            "id_ruta" to editIdRuta.trim()
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
                            Toast.makeText(context, "Paradero eliminado", Toast.LENGTH_SHORT).show()
                            paraderoSeleccionado = null
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("🗑️ Borrar")
                }
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        // Cabecera idéntica al mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = CircleShape)
                    .clickable { onVolver() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF1E293B)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POPABUS · SOTRACAUCA",
                    color = Color(0xFF0D9488),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Gestión de Rutas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PP", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            item {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

        // Textos de subtítulo
        Text(
            text = "OPERACIÓN",
            color = Color(0xFF0D9488),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Configura recorridos base para la flota.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // FORMULARIO DE CREACIÓN
        Text(
            text = "Nombre",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = nombreRuta,
            onValueChange = { nombreRuta = it },
            placeholder = { Text("Ej: Ruta 12", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Origen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = origen,
                    onValueChange = { origen = it },
                    placeholder = { Text("Ej: Punto inicial", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Destino",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = destino,
                    onValueChange = { destino = it },
                    placeholder = { Text("Ej: Punto final", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Crear Ruta Base
        Button(
            onClick = {
                if (nombreRuta.isNotEmpty() && origen.isNotEmpty() && destino.isNotEmpty()) {
                    val nuevaRutaRef = refRutas.push()

                    val nuevaRuta = RutaOficial(
                        nombre_ruta = nombreRuta,
                        origen = origen,
                        destino = destino,
                        estado = "activa"
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("➕ Crear Ruta Base", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item {
            Column {
                // Encabezado de la lista con Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Rutas activas", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = listaRutas.size.toString(), color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // LISTA DE RUTAS
        items(listaRutas) { ruta ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { rutaSeleccionada = ruta },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("🗺️", fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = ruta.nombre_ruta, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1f))

                            // Badge Estado
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (ruta.estado == "activa") Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, if (ruta.estado == "activa") Color(0xFF10B981) else Color(0xFFEF4444))
                            ) {
                                Text(
                                    text = if (ruta.estado == "activa") "Activa" else "Inactiva",
                                    color = if (ruta.estado == "activa") Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fila de Origen y Destino con fondo gris
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = ruta.origen, fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(text = "→", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                Text(text = ruta.destino, fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }

        item {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                // Botón Volver
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Text("<  Volver al Menú", color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

    // --- DIÁLOGO DE EDICIÓN CON LÓGICA INTACTA ---
    if (rutaSeleccionada != null) {
        val ruta = rutaSeleccionada!!
        var editNombre by remember(ruta) { mutableStateOf(ruta.nombre_ruta) }
        var editOrigen by remember(ruta) { mutableStateOf(ruta.origen) }
        var editDestino by remember(ruta) { mutableStateOf(ruta.destino) }
        var editEstado by remember(ruta) { mutableStateOf(ruta.estado) } // Agregado para poder activar/desactivar

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
                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle para activar o desactivar la ruta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estado de la Ruta:")
                        Switch(
                            checked = editEstado == "activa",
                            onCheckedChange = { isChecked -> editEstado = if (isChecked) "activa" else "inactiva" }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editNombre.isNotBlank() && editOrigen.isNotBlank() && editDestino.isNotBlank()) {
                        val actualizaciones = mapOf(
                            "nombre_ruta" to editNombre,
                            "origen" to editOrigen,
                            "destino" to editDestino,
                            "estado" to editEstado
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
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var listaRutas by remember { mutableStateOf<List<RutaOficial>>(emptyList()) }
    var listaConductores by remember { mutableStateOf<List<UsuarioOficial>>(emptyList()) }

    // Descargamos los buses, rutas y conductores de Firebase en tiempo real
    DisposableEffect(Unit) {
        val listenerBuses = object : ValueEventListener {
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
        refBuses.addValueEventListener(listenerBuses)

        val refRutas = database.getReference("rutas")
        val listenerRutas = object : ValueEventListener {
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
            override fun onCancelled(error: DatabaseError) {}
        }
        refRutas.addValueEventListener(listenerRutas)

        val refUsuarios = database.getReference("usuarios")
        val listenerUsuarios = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuarios = mutableListOf<UsuarioOficial>()
                for (hijo in snapshot.children) {
                    val u = hijo.getValue(UsuarioOficial::class.java)
                    if (u != null && u.rol.trim().lowercase() == "conductor") {
                        usuarios.add(u.copy(id = hijo.key ?: ""))
                    }
                }
                listaConductores = usuarios
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        refUsuarios.addValueEventListener(listenerUsuarios)

        onDispose {
            refBuses.removeEventListener(listenerBuses)
            refRutas.removeEventListener(listenerRutas)
            refUsuarios.removeEventListener(listenerUsuarios)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        // Cabecera idéntica al mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = CircleShape)
                    .clickable { onVolver() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF1E293B)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POPABUS · SOTRACAUCA",
                    color = Color(0xFF0D9488),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Vehículos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PP", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
        
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            item {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

        // Textos de subtítulo
        Text(
            text = "FLOTA SOTRACAUCA",
            color = Color(0xFF0D9488),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Registra buses y asigna su operación.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // FORMULARIO DE CREACIÓN
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Placa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = placa,
                    onValueChange = { placa = it },
                    placeholder = { Text("Ej: ABC-123", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Modelo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    placeholder = { Text("Marca/línea", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Asignar Ruta",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        // --- DROPDOWN RUTAS ---
        var expandedRuta by remember { mutableStateOf(false) }
        val nombreRutaSeleccionada = listaRutas.find { it.id == idRuta }?.nombre_ruta ?: "Sin ruta asignada"

        ExposedDropdownMenuBox(
            expanded = expandedRuta,
            onExpandedChange = { expandedRuta = !expandedRuta }
        ) {
            OutlinedTextField(
                value = nombreRutaSeleccionada,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRuta) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(
                expanded = expandedRuta,
                onDismissRequest = { expandedRuta = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin ruta asignada") },
                    onClick = {
                        idRuta = ""
                        expandedRuta = false
                    }
                )
                listaRutas.forEach { ruta ->
                    DropdownMenuItem(
                        text = { Text(ruta.nombre_ruta) },
                        onClick = {
                            idRuta = ruta.id
                            expandedRuta = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Asignar Conductor",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        // --- DROPDOWN CONDUCTORES ---
        var expandedConductor by remember { mutableStateOf(false) }
        val nombreConductorSeleccionado = listaConductores.find { it.id == idConductor }?.nombre_completo ?: "Sin conductor asignado"

        ExposedDropdownMenuBox(
            expanded = expandedConductor,
            onExpandedChange = { expandedConductor = !expandedConductor }
        ) {
            OutlinedTextField(
                value = nombreConductorSeleccionado,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedConductor) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(
                expanded = expandedConductor,
                onDismissRequest = { expandedConductor = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin conductor asignado") },
                    onClick = {
                        idConductor = ""
                        expandedConductor = false
                    }
                )
                listaConductores.forEach { conductor ->
                    DropdownMenuItem(
                        text = { Text(conductor.nombre_completo) },
                        onClick = {
                            idConductor = conductor.id
                            expandedConductor = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Registrar Bus
        Button(
            onClick = {
                if (placa.isNotEmpty()) {
                    val nuevoBusRef = refBuses.push()
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("➕ Registrar Bus", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Encabezado de la lista con Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vehículos registrados", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = listaBuses.size.toString(), color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // LISTA DE BUSES
        items(listaBuses) { bus ->
                val nombreRutaList = listaRutas.find { it.id == bus.id_ruta }?.nombre_ruta ?: "Ninguna"
                val nombreConductorList = listaConductores.find { it.id == bus.id_conductor_actual }?.nombre_completo ?: "Ninguno"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { busSeleccionado = bus },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = bus.placa, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Text(text = bus.modelo.ifEmpty { "Sin modelo" }, fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(text = "$nombreRutaList - $nombreConductorList", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }

                        // Badge de estado verde/rojo
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (bus.estado == "activo") Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                        )
                    }
                }
            }

        item {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                // Botón Volver
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Text("<  Volver al Menú", color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

    // --- DIÁLOGO DE EDICIÓN CON DROPDOWNS ---
    if (busSeleccionado != null) {
        val bus = busSeleccionado!!
        var editPlaca by remember(bus) { mutableStateOf(bus.placa) }
        var editModelo by remember(bus) { mutableStateOf(bus.modelo) }
        var editRuta by remember(bus) { mutableStateOf(bus.id_ruta) }
        var editConductor by remember(bus) { mutableStateOf(bus.id_conductor_actual) }
        var editEstado by remember(bus) { mutableStateOf(bus.estado) }

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

                    // --- DROPDOWN EDITAR RUTA ---
                    var expandedEditRuta by remember { mutableStateOf(false) }
                    val nombreEditRuta = listaRutas.find { it.id == editRuta }?.nombre_ruta ?: "Sin ruta asignada"

                    ExposedDropdownMenuBox(
                        expanded = expandedEditRuta,
                        onExpandedChange = { expandedEditRuta = !expandedEditRuta }
                    ) {
                        OutlinedTextField(
                            value = nombreEditRuta,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Reasignar Ruta") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEditRuta) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEditRuta,
                            onDismissRequest = { expandedEditRuta = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sin ruta asignada") },
                                onClick = {
                                    editRuta = ""
                                    expandedEditRuta = false
                                }
                            )
                            listaRutas.forEach { ruta ->
                                DropdownMenuItem(
                                    text = { Text(ruta.nombre_ruta) },
                                    onClick = {
                                        editRuta = ruta.id
                                        expandedEditRuta = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- DROPDOWN EDITAR CONDUCTOR ---
                    var expandedEditConductor by remember { mutableStateOf(false) }
                    val nombreEditConductor = listaConductores.find { it.id == editConductor }?.nombre_completo ?: "Sin conductor asignado"

                    ExposedDropdownMenuBox(
                        expanded = expandedEditConductor,
                        onExpandedChange = { expandedEditConductor = !expandedEditConductor }
                    ) {
                        OutlinedTextField(
                            value = nombreEditConductor,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Reasignar Conductor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEditConductor) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEditConductor,
                            onDismissRequest = { expandedEditConductor = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sin conductor asignado") },
                                onClick = {
                                    editConductor = ""
                                    expandedEditConductor = false
                                }
                            )
                            listaConductores.forEach { conductor ->
                                DropdownMenuItem(
                                    text = { Text(conductor.nombre_completo) },
                                    onClick = {
                                        editConductor = conductor.id
                                        expandedEditConductor = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle de estado (Activo/Inactivo)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estado del Bus:")
                        Switch(
                            checked = editEstado == "activo",
                            onCheckedChange = { isChecked -> editEstado = if (isChecked) "activo" else "inactivo" }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editPlaca.isNotBlank()) {
                        val actualizaciones = mapOf(
                            "placa" to editPlaca,
                            "modelo" to editModelo,
                            "id_ruta" to editRuta,
                            "id_conductor_actual" to editConductor,
                            "estado" to editEstado
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
}


@Composable
fun DialogoReportarIncidencia(onDismiss: () -> Unit, onEnviar: (String) -> Unit) {
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚠️ Reportar Incidencia") },
        text = {
            Column {
                Text(
                    "Describe el problema (accidente, trancón, desvío, etc.):",
                    style = MaterialTheme.typography.bodyMedium
                )
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
// --- PANTALLA GESTIÓN DE USUARIOS (SPRINT 11 - REDISEÑO) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionUsuarios(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refUsuarios = database.getReference("usuarios")

    // Estado para la lista de usuarios en tiempo real
    var listaUsuarios by remember { mutableStateOf<List<UsuarioOficial>>(emptyList()) }
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

    // Estado de las pestañas
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Pasajeros", "Conductores")

    val usuariosFiltrados = listaUsuarios.filter { u ->
        val rolNorm = u.rol.trim().lowercase()
        if (rolNorm == "administrador") return@filter false
        when (tabIndex) {
            0 -> rolNorm == "pasajero"
            1 -> rolNorm == "conductor"
            else -> false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // Cabecera idéntica al mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = CircleShape)
                    .clickable { onVolver() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF1E293B)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POPABUS · SOTRACAUCA",
                    color = Color(0xFF0D9488),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Gestión Humana",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PP", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Textos de subtítulo
        Text(
            text = "DIRECTORIO",
            color = Color(0xFF0D9488),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Administra usuarios y permisos del sistema.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Pestañas personalizadas (Pill-shaped)
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFF1F5F9), // Gris claro para el fondo de pestañas
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = tabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .background(
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { tabIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF64748B),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de usuarios (Tarjetas de Figma)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(usuariosFiltrados) { usuario ->
                val primeraLetra = usuario.nombre_completo.firstOrNull()?.uppercase() ?: "?"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { usuarioSeleccionado = usuario }, // Mantiene tu lógica de abrir el diálogo
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar circular
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = primeraLetra, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Información
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = usuario.nombre_completo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Text(text = if (usuario.rol == "pasajero") "Pasajero" else "Conductor", fontSize = 12.sp, color = Color(0xFF64748B))
                            // Correo según tu requerimiento
                            Text(text = usuario.correo.ifEmpty { "Sin sincronizar" }, fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(text = "UID · ${usuario.id.take(8)}...", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }

                        // Badge (Estado)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (usuario.activo) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, if (usuario.activo) Color(0xFF10B981) else Color(0xFFEF4444))
                        ) {
                            Text(
                                text = if (usuario.activo) "Activo" else "Suspendido",
                                color = if (usuario.activo) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Volver (Estilo delineado como en mockup de Paraderos)
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Text("<  Volver al Menú", color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
        }
    }

    // --- DIÁLOGO DE CRUD COMPLETO (Se mantiene tu lógica exacta) ---
    if (usuarioSeleccionado != null) {
        val usuario = usuarioSeleccionado!!

        AlertDialog(
            onDismissRequest = { usuarioSeleccionado = null },
            title = {
                Text(
                    "Perfil de Usuario",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "👤 Nombre: ${usuario.nombre_completo}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "📧 Correo: ${usuario.correo.ifEmpty { "Sin sincronizar" }}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "🔑 UID: ${usuario.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val fechaFormateada = remember(usuario.fecha_registro) {
                        val sdf = java.text.SimpleDateFormat(
                            "dd/MM/yyyy hh:mm a",
                            java.util.Locale.getDefault()
                        )
                        sdf.format(java.util.Date(usuario.fecha_registro))
                    }
                    Text(
                        "📅 Registro: $fechaFormateada",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Rol actual: ${usuario.rol.uppercase()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Estado: ${if (usuario.activo) "🟢 Activo" else "🔴 Suspendido"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones de Actualización (Update)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Cambiar Estado
                        OutlinedButton(
                            onClick = {
                                if (usuario.rol.trim().lowercase() == "administrador") {
                                    Toast.makeText(
                                        context,
                                        "Seguridad: Administradores no editables",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val nuevoEstado = !usuario.activo
                                    refUsuarios.child(usuario.id)
                                        .updateChildren(mapOf("activo" to nuevoEstado))
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Estado actualizado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            usuarioSeleccionado =
                                                usuarioSeleccionado?.copy(activo = nuevoEstado)
                                        }
                                }
                            }
                        ) {
                            Text(if (usuario.activo) "🔴 Suspender" else "🟢 Activar")
                        }

                        // Cambiar Rol
                        OutlinedButton(
                            onClick = {
                                if (usuario.rol.trim().lowercase() == "administrador") {
                                    Toast.makeText(
                                        context,
                                        "Seguridad: No editable",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val nuevoRol = if (usuario.rol.trim()
                                            .lowercase() == "pasajero"
                                    ) "conductor" else "pasajero"
                                    refUsuarios.child(usuario.id)
                                        .updateChildren(mapOf("rol" to nuevoRol))
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Rol actualizado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            usuarioSeleccionado =
                                                usuarioSeleccionado?.copy(rol = nuevoRol)
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
                        if (usuario.rol.trim().lowercase() == "administrador") {
                            Toast.makeText(
                                context,
                                "Seguridad: Administradores no se pueden eliminar",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            refUsuarios.child(usuario.id).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Usuario eliminado definitivamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    usuarioSeleccionado = null
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Error al eliminar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("🗑️ Borrar Definitivamente")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionReportes(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refIncidencias = database.getReference("incidencias")

    // Estado para la lista en tiempo real
    var listaIncidencias by remember { mutableStateOf<List<IncidenciaOficial>>(emptyList()) }

    // Estado de las pestañas
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
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

    // --- FILTRO MATEMÁTICO DE MATRIZ OPERATIVA (Intacto) ---
    val matrizOperativaYPanicos = listOf("trancón", "desvío", "fallo", "pánico")

    val incidenciasFiltradas = listaIncidencias.filter { incidencia ->
        val descripcionNormalizada = incidencia.descripcion.lowercase()
        val esOperativaOPanico = matrizOperativaYPanicos.any { keyword ->
            descripcionNormalizada.contains(keyword)
        }

        if (tabIndex == 0) {
            // Pestaña Pasajeros: NO es operativa, ni pánico
            !esOperativaOPanico
        } else {
            // Pestaña Conductores / Operativa: SÍ es operativa o pánico
            esOperativaOPanico
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // Cabecera idéntica al mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = CircleShape)
                    .clickable { onVolver() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF1E293B)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POPABUS · SOTRACAUCA",
                    color = Color(0xFF0D9488),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Reportes e Incidencias",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PP", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Textos de subtítulo
        Text(
            text = "CENTRO DE ATENCIÓN",
            color = Color(0xFF0D9488),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Prioriza novedades de la operación.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Pestañas personalizadas (Pill-shaped)
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFF1F5F9), // Gris claro
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = tabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .background(
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { tabIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF64748B),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
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
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        // Fondo rojizo si es urgente, blanco si es normal
                        colors = CardDefaults.cardColors(
                            containerColor = if (esUrgente) Color(0xFFFEF2F2) else Color.White
                        ),
                        // Borde rojo suave si es urgente, sino sin borde
                        border = if (esUrgente) BorderStroke(1.dp, Color(0xFFFECACA)) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // CONTENIDO SUPERIOR
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f)) {
                                    // Ícono Circular
                                    Surface(
                                        shape = CircleShape,
                                        color = if (esUrgente) Color(0xFFFEE2E2) else Color(0xFFEFF6FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            // Usamos emojis temporalmente como en tu mockup para simplificar iconos
                                            Text(if (esUrgente) "⚠️" else "💬", fontSize = 16.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Título y Descripción
                                    Column {
                                        val titulo = if (esUrgente) "Alerta Crítica: Pánico" else "Reporte Ciudadano"
                                        Text(
                                            text = titulo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = incidencia.descripcion,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )

                                        // Fecha
                                        val fechaFormateada = remember(incidencia.fecha_reporte) {
                                            val sdf = java.text.SimpleDateFormat(
                                                "dd/MM/yyyy HH:mm",
                                                java.util.Locale.getDefault()
                                            )
                                            sdf.format(java.util.Date(incidencia.fecha_reporte))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "Hace instantes · $fechaFormateada", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }

                                // Badge de Estado
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (esUrgente) Color(0xFFDC2626) else Color(0xFF3B82F6)
                                ) {
                                    Text(
                                        text = if (esUrgente) "URGENTE" else "NUEVO",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = if (esUrgente) Color(0xFFFECACA) else Color(0xFFE2E8F0), thickness = 1.dp)

                            // BOTÓN DE ACCIÓN INFERIOR
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* Aquí podrías añadir lógica para marcar como resuelto en el futuro */ }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (esUrgente) "Atender alerta" else "Revisar reporte",
                                    color = if (esUrgente) Color(0xFFDC2626) else Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "→",
                                    color = if (esUrgente) Color(0xFFDC2626) else Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Volver
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Text("<  Volver al Menú", color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MapaGeneralAdmin(onVolver: () -> Unit) {
    val context = LocalContext.current
    var flotaActiva by remember { mutableStateOf<Map<String, PosicionGpsOficial>>(emptyMap()) }
    var inventarioBuses by remember { mutableStateOf<Map<String, BusOficial>>(emptyMap()) }

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

                    val lat =
                        (latObj as? Number)?.toDouble() ?: latObj?.toString()?.toDoubleOrNull()
                        ?: 0.0
                    val lng =
                        (lngObj as? Number)?.toDouble() ?: lngObj?.toString()?.toDoubleOrNull()
                        ?: 0.0
                    val ts =
                        (tsObj as? Number)?.toLong() ?: tsObj?.toString()?.toLongOrNull() ?: 0L

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
                Toast.makeText(
                    context,
                    "Error conectando con la flota: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        refBuses.addValueEventListener(listener)
        onDispose { refBuses.removeEventListener(listener) }
    }

    // Descargamos el inventario de buses
    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val refInventario = database.getReference("buses")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevoInventario = mutableMapOf<String, BusOficial>()
                for (busSnapshot in snapshot.children) {
                    val busData = busSnapshot.getValue(BusOficial::class.java)
                    if (busSnapshot.key != null && busData != null) {
                        nuevoInventario[busSnapshot.key!!] = busData
                    }
                }
                inventarioBuses = nuevoInventario
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error cargando inventario: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        refInventario.addValueEventListener(listener)
        onDispose { refInventario.removeEventListener(listener) }
    }

    val popayan = LatLng(2.4419, -76.6063)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(popayan, 13f)
    }

    val iconoBusNormal = remember { obtenerIconoBus(context, R.drawable.ic_bus_nuevo) }
    val iconoBusGris = remember { obtenerIconoBusGris(context, R.drawable.ic_bus_nuevo) }

// ... (Mantén tus variables de estado aquí arriba intactas) ...

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. EL MAPA BASE (Mantenemos tu lógica actual por ahora)
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false) // <--- Cambia esto a false
        ) {
            flotaActiva.forEach { (idBus, busActivo) ->
                val busEstatico = inventarioBuses[idBus]
                
                // Asignación de Placa y Ruta (con fallback si aún no descarga el dato)
                val titulo = if (busEstatico != null) {
                    "${busEstatico.placa} - Ruta ${busEstatico.id_ruta}"
                } else {
                    "Bus $idBus"
                }

                // Evaluación de color por conexión (2 minutos = 120000 ms)
                val sinSenal = (System.currentTimeMillis() - busActivo.hora_lectura) > 120000

                MarkerComposable(
                    state = MarkerState(position = LatLng(busActivo.latitud, busActivo.longitud)),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (sinSenal) Color(0xFFE53935) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = if (sinSenal) "Señal Perdida" else titulo,
                                color = if (sinSenal) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (sinSenal) Color(0xFFE53935) else Color(0xFF3B82F6),
                                    shape = CircleShape
                                )
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (sinSenal) Icons.Default.Warning else Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. BOTÓN DE RETROCESO (Arriba a la izquierda)
        FloatingActionButton(
            onClick = onVolver,
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .size(48.dp)
                .align(Alignment.TopStart),
            containerColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Volver",
                tint = Color(0xFF0F172A)
            )
        }

        // 3. CABECERA CENTRAL (Torre de Control)
        Card(
            modifier = Modifier
                .padding(top = 48.dp, start = 80.dp, end = 16.dp) // Deja espacio para el botón de volver
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TORRE DE CONTROL",
                    color = Color(0xFF00BFA5), // Verde corporativo
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Flota en tiempo real",
                    color = Color(0xFF0F172A),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF00BFA5), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${flotaActiva.size} buses conectados", // Conectado dinámicamente a tu estado
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 4. LEYENDA DE ESTADOS (Abajo a la derecha)
        Card(
            modifier = Modifier
                .padding(bottom = 32.dp, end = 16.dp)
                .align(Alignment.BottomEnd),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF00BFA5), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("En ruta", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.width(16.dp))

                Box(modifier = Modifier.size(8.dp).background(Color(0xFFE53935), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sin señal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionInformes(onVolver: () -> Unit) {
    val context = LocalContext.current
    val database = remember { FirebaseDatabase.getInstance() }
    val refInformes = database.getReference("informes_diarios")

    var listaInformes by remember { mutableStateOf<List<InformeDiario>>(emptyList()) }
    var filtroTiempo by remember { mutableStateOf("Diario") }

    // Descargamos los informes en tiempo real (Lógica Intacta)
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

    // Filtros Matemáticos (Lógica Intacta)
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        // Cabecera idéntica al mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = CircleShape)
                    .clickable { onVolver() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF1E293B)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POPABUS · SOTRACAUCA",
                    color = Color(0xFF0D9488),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Gestión de Informes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PP", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Textos de subtítulo
        Text(
            text = "ANALÍTICA",
            color = Color(0xFF0D9488),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Consulta el rendimiento operativo.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Pestañas personalizadas (Pill-shaped)
        val tabs = listOf("Diario", "Semanal", "Mensual")
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFF1F5F9), // Gris claro
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                tabs.forEach { title ->
                    val isSelected = filtroTiempo == title
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .background(
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { filtroTiempo = title },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de Resumen (Analítica General)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                // Título y logo de la tarjeta
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📊", fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("RESUMEN ACTUAL", color = Color(0xFF0D9488), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Analítica General", color = Color(0xFF1E293B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Números grandes
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$totalInformes",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2563EB)
                        )
                        Text("Total Informes", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$tiempoPromedio min",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2563EB)
                        )
                        Text("Tiempo Promedio", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Encabezado de la lista con Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cierres de viaje", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = listaFiltrada.size.toString(), color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // PARTE SCROLLABLE
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(listaFiltrada) { informe ->
                // Generamos un ID corto visual a partir del ID de Firebase para simular el TRP-XXXX
                val idCortoVisual = if (informe.id_recorrido.length >= 6) {
                    "TRP-${informe.id_recorrido.takeLast(4).uppercase()}"
                } else {
                    "TRP-0000"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Check azul en círculo outline
                            Surface(
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text("✓", color = Color(0xFF2563EB), fontSize = 16.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text("CIERRE DE VIAJE", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = idCortoVisual, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                            }
                        }

                        // Columna derecha con tiempos y alertas
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏱", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${informe.tiempo_total_minutos} min",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Text(
                                text = "${informe.total_alertas_emitidas} alerta(s)",
                                fontSize = 10.sp,
                                color = if (informe.total_alertas_emitidas > 0) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                fontWeight = if (informe.total_alertas_emitidas > 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            
            item {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón Volver
                    OutlinedButton(
                        onClick = onVolver,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Text("<  Volver al Menú", color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun PantallaSplash(onSplashTerminado: () -> Unit) {
    val offsetX = remember { androidx.compose.animation.core.Animatable(-300f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val resplandor = remember { androidx.compose.animation.core.Animatable(2f) }

    // NUEVO: Estado para animar la barra inferior de 0 a 1 (0% a 100%)
    val progresoBarra = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Movimiento del bus: Entra rápido, frena suave (Dura 1.5s)
        launch {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 1500,
                    easing = androidx.compose.animation.core.LinearOutSlowInEasing
                )
            )
        }

        // 2. Opacidad: Sincronizada con el movimiento (Dura 1.5s)
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 1500,
                    easing = androidx.compose.animation.core.LinearEasing
                )
            )
        }

        // 3. Brillo del contenedor: Se activa al detenerse (Dura 400ms)
        launch {
            kotlinx.coroutines.delay(1300)
            resplandor.animateTo(
                targetValue = 24f,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)
            )
        }

        // 4. Barra de progreso: Se llena suavemente (Dura 1.8s)
        launch {
            progresoBarra.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 1800,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
        }

        // 5. SALTO EXACTO Y ESTRICTO A LOS 2.8 SEGUNDOS DESDE EL INICIO
        launch {
            kotlinx.coroutines.delay(2800) // 2.8 segundos absolutos de contemplación
            onSplashTerminado()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = Color(0xFFF1F5F9),
                start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.2f),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.45f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color(0xFFF1F5F9),
                start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.85f),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.6f),
                strokeWidth = 4f
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Center).offset(x = offsetX.value.dp)
                .alpha(alpha.value)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End
            ) {
                val gradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(
                        Color(0x000D9488), Color(0xFF0D9488)
                    )
                )
                Spacer(modifier = Modifier.width(20.dp).height(2.dp).background(gradient))
                Spacer(modifier = Modifier.width(45.dp).height(2.dp).background(gradient))
                Spacer(modifier = Modifier.width(35.dp).height(2.dp).background(gradient))
                Spacer(modifier = Modifier.width(15.dp).height(2.dp).background(gradient))
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF2563EB),
                shadowElevation = resplandor.value.dp,
                modifier = Modifier.size(90.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.DirectionsBus,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.padding(20.dp).fillMaxSize()
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2563EB),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.DirectionsBus,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Popa",
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    "Bus",
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }


            Spacer(modifier = Modifier.height(6.dp))
            Text("Tu bus, en tiempo real", fontSize = 14.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(24.dp))

            // NUEVO: Contenedor gris estático y barra turquesa dinámica superpuesta
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .background(
                        Color(0xFFE2E8F0),
                        RoundedCornerShape(50)
                    ), // Fondo gris clarito
                contentAlignment = Alignment.CenterStart
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(progresoBarra.value) // El ancho depende del estado animado (0 a 1f)
                        .height(3.dp)
                        .background(
                            Color(0xFF0D9488),
                            RoundedCornerShape(50)
                        ) // Barra verde turquesa
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPlanificaViaje(
    ubicacionPasajero: LatLng?,
    rutasProcesadas: List<RutaOficial>,
    paraderosProcesados: List<ParaderoOficial>,
    busesEnZona: Map<String, LatLng>, // Para el ETA
    onVolver: () -> Unit,
    onRutaSeleccionada: (String) -> Unit
) {
    var textoDestino by remember { mutableStateOf("") }
    // Relaciona ID del Bus -> ID de su Ruta leyendo la colección "buses"
    val rutasPorBus = remember { mutableStateOf(emptyMap<String, String>()) }

    DisposableEffect(Unit) {
        val refBuses = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("buses")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val mapaTemp = mutableMapOf<String, String>()
                for (busSnap in snapshot.children) {
                    val idBus = busSnap.key ?: continue
                    val idRuta = busSnap.child("id_ruta").value?.toString()
                        ?: busSnap.child("ruta").value?.toString()
                        ?: ""
                    if (idRuta.isNotEmpty()) {
                        mapaTemp[idBus] = idRuta
                    }
                }
                rutasPorBus.value = mapaTemp
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        refBuses.addValueEventListener(listener)
        onDispose { refBuses.removeEventListener(listener) }
    }

    // Buscador Inteligente: Filtramos paraderos y luego sacamos sus rutas
    val rutasSugeridas = remember(textoDestino, rutasProcesadas, paraderosProcesados) {
        if (textoDestino.isBlank()) {
            rutasProcesadas.filter { it.estado == "activa" } // Muestra todas si no hay texto
        } else {
            val paraderosCoincidentes = paraderosProcesados.filter {
                it.nombre.contains(textoDestino, ignoreCase = true) ||
                        it.direccion.contains(textoDestino, ignoreCase = true)
            }
            val idsRutas = paraderosCoincidentes.map { it.id_ruta }.toSet()
            rutasProcesadas.filter { it.id in idsRutas && it.estado == "activa" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // CABECERA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onVolver,
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                shadowElevation = 2.dp,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier.padding(12.dp),
                    tint = Color(0xFF0F172A)
                )
            }

            Text(
                text = "Planifica tu viaje",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        // TARJETA DE ORIGEN/DESTINO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Guía Visual Izquierda
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).border(2.dp, Color(0xFF2563EB), CircleShape))
                    Box(modifier = Modifier.width(2.dp).height(45.dp).background(Color(0xFFE2E8F0)))
                    Box(modifier = Modifier.size(10.dp).border(2.dp, Color(0xFF10B981), RoundedCornerShape(2.dp)))
                }

                // Campos
                Column(modifier = Modifier.weight(1f)) {
                    Text("Origen", fontSize = 11.sp, color = Color.Gray)
                    Text("Mi ubicación actual", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                    Text("Destino", fontSize = 11.sp, color = Color.Gray)
                    BasicTextField(
                        value = textoDestino,
                        onValueChange = { textoDestino = it },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (textoDestino.isEmpty()) {
                                Text("Busca un lugar o dirección", color = Color.LightGray, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    )
                }

                Surface(
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.padding(start = 8.dp).size(36.dp)
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Invertir", tint = Color(0xFF2563EB), modifier = Modifier.padding(6.dp))
                }
            }
        }

        Text(
            text = "RUTAS SUGERIDAS",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )

// LISTA DINÁMICA DE RUTAS
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rutasSugeridas) { ruta ->
                val numeroVisual = ruta.nombre_ruta.filter { it.isDigit() }.takeIf { it.isNotEmpty() } ?: "R"

// 2. Filtro estricto cruzado con rutasPorBus local
                val idRutaActual = ruta.id
                val idsBusesDeEstaRuta = rutasPorBus.value.filterValues { it == idRutaActual }.keys
                val ubicacionesBusesDeEstaRuta = busesEnZona.filterKeys { it in idsBusesDeEstaRuta }.values

                val etaReal = when {
                    ubicacionPasajero == null -> "Ubicando..."
                    ubicacionesBusesDeEstaRuta.isEmpty() -> "Sin buses"
                    else -> {
                        val distanciaMinima = ubicacionesBusesDeEstaRuta.minOf { ubicacionBus ->
                            calcularDistanciaMetros(ubicacionPasajero!!, ubicacionBus)
                        }
                        obtenerTextoETA(distanciaMinima)
                    }
                }

                TarjetaRutaSugerida(
                    numeroRuta = numeroVisual,
                    nombreRuta = ruta.nombre_ruta,
                    paraderos = "${ruta.origen} - ${ruta.destino}",
                    tipoViaje = "Ruta directa",
                    eta = etaReal,
                    onClick = { onRutaSeleccionada(ruta.id) }
                )
            }
        }
    }
}

@Composable
fun PantallaCuentaPasajero(
    onCerrarSesion: () -> Unit,
    onVolverAlInicio: () -> Unit,
    onOpcionSeleccionada: (String) -> Unit,
    onIrALogin: () -> Unit,
    onIrARegistro: () -> Unit
) {
    // NUEVO: Estado para saber qué mostrar (MENU o NOTIFICACIONES)
    var vistaInterna by remember { mutableStateOf("MENU") }


    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val usuarioLogueado = currentUser != null

// Estado para guardar el nombre descargado de Realtime Database
    var nombreUsuario by remember { mutableStateOf("Cargando...") }
    val correoUsuario = currentUser?.email ?: "Sin correo vinculado"

// LaunchedEffect para consultar el nombre_completo en Firebase
    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            val database = com.google.firebase.database.FirebaseDatabase.getInstance()
            database.getReference("usuarios").child(currentUser.uid).child("nombre_completo").get()
                .addOnSuccessListener { snapshot ->
                    // Actualiza la variable con el nombre real, o un fallback
                    nombreUsuario = snapshot.getValue(String::class.java) ?: "Usuario"
                }
                .addOnFailureListener {
                    nombreUsuario = "Usuario"
                }
        }
    }




    if (vistaInterna == "NOTIFICACIONES") {
        PantallaNotificaciones(onVolver = { vistaInterna = "MENU" })
    } else if (vistaInterna == "SUGERENCIAS") { // <- NUEVA VISTA
        PantallaSugerencias(onVolver = { vistaInterna = "MENU" })
    } else if (vistaInterna == "ACERCA") {
        PantallaAcercaDe(onVolver = { vistaInterna = "MENU" })
    } else if (vistaInterna == "AYUDA") {
        PantallaAyudaYFrecuentes(titulo = "Ayuda", onVolver = { vistaInterna = "MENU" })
    } else if (vistaInterna == "FRECUENTES") {
        PantallaAyudaYFrecuentes(titulo = "Preguntas Frecuentes", onVolver = { vistaInterna = "MENU" })
    } else {

        // Tu código original intacto queda aquí adentro...
        // Tu código original intacto queda aquí adentro
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // TopBar simple
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = onVolverAlInicio,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Mi Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (usuarioLogueado) {
                // Tarjeta Azul superior
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f) // Evita que un correo largo empuje el botón de Editar
                        ) {
                            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.padding(12.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))

                            // REEMPLAZO EXACTO: Columna apilando nombre y correo dinámicos
                            Column {
                                Text(text = nombreUsuario, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = correoUsuario, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            }
                        }

                        TextButton(onClick = { /* Editar */ }) {
                            Text("Editar", color = Color.White)
                        }
                    }
                }
            }else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFFEFF6FF), modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No has iniciado sesión", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                        Text("Accede para guardar tus rutas y preferencias.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 20.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onIrALogin,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text("Iniciar Sesión")
                            }
                            OutlinedButton(
                                onClick = onIrARegistro,
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Color(0xFF2563EB))
                            ) {
                                Text("Crear Cuenta", color = Color(0xFF2563EB))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lista de opciones
            val opciones = listOf(
                "Notificaciones" to Icons.Outlined.Notifications,
                "Ayuda" to Icons.Outlined.HelpOutline,
                "Preguntas Frecuentes" to Icons.Outlined.Info,
                "Sugerencias" to Icons.Outlined.ChatBubbleOutline,
                "Acerca de PopaBus" to Icons.Outlined.Info
            )

            opciones.forEach { (texto, icono) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            if (texto == "Notificaciones") {
                                vistaInterna = "NOTIFICACIONES"
                            } else if (texto == "Sugerencias") {
                                vistaInterna = "SUGERENCIAS"
                            } else if (texto == "Acerca de PopaBus") { // <- NUEVO
                                vistaInterna = "ACERCA"
                            } else if (texto == "Ayuda") { // <- NUEVO
                                vistaInterna = "AYUDA"
                            } else if (texto == "Preguntas Frecuentes") { // <- NUEVO
                                vistaInterna = "FRECUENTES"
                            } else {
                                onOpcionSeleccionada(texto)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Cuadro azul claro envolviendo el ícono
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF), // Fondo azul muy claro
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = icono,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB), // Color azul fuerte del ícono
                                    modifier = Modifier.padding(6.dp) // Espaciado interno para ajustar el tamaño del dibujo
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = texto,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = Color(0xFF1E293B), // Texto oscuro intenso
                                fontSize = 16.sp
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (usuarioLogueado) {
                // Botón Cerrar Sesión
                Button(
                    onClick = onCerrarSesion,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFDC2626)),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNotificaciones(onVolver: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("PreferenciasPopabus", Context.MODE_PRIVATE) }

    // Estados independientes para cada interruptor
    var alertasProximidad by remember { mutableStateOf(sharedPrefs.getBoolean("alertasProximidad", true)) }
    var promociones by remember { mutableStateOf(false) }
    var avisos by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
    ) {
        // Cabecera Blanca
        CenterAlignedTopAppBar(
            title = { Text("Notificaciones", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            navigationIcon = {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF1E293B))
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Elige qué novedades quieres recibir.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Contenedor principal de los interruptores
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    FilaNotificacionToggle(
                        titulo = "Alertas de proximidad",
                        subtitulo = "Te avisamos cuando tu bus esté cerca.",
                        checked = alertasProximidad,
                        onCheckedChange = { 
                            alertasProximidad = it 
                            sharedPrefs.edit().putBoolean("alertasProximidad", it).apply()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F5F9))

                    FilaNotificacionToggle(
                        titulo = "Promociones",
                        subtitulo = "Beneficios y ofertas para tus viajes.",
                        checked = promociones,
                        onCheckedChange = { promociones = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F5F9))

                    FilaNotificacionToggle(
                        titulo = "Avisos",
                        subtitulo = "Novedades importantes en tu zona.",
                        checked = avisos,
                        onCheckedChange = { avisos = it }
                    )
                }
            }

            Text(
                text = "Puedes cambiar estas preferencias cuando quieras.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Componente reutilizable para evitar repetir código en cada fila
@Composable
fun FilaNotificacionToggle(titulo: String, subtitulo: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = titulo, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF0F172A))
            Text(text = subtitulo, fontSize = 13.sp, color = Color(0xFF64748B))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0D9488), // Verde turquesa de tu app
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            )
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSugerencias(onVolver: () -> Unit) {
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var textoSugerencia by remember { mutableStateOf("") }
    var expandido by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val opcionesCategoria = listOf("Rutas y horarios", "Experiencia en la app", "Seguridad", "Otro")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
    ) {
        // TopBar Blanco
        CenterAlignedTopAppBar(
            title = { Text("Sugerencias", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            navigationIcon = {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF1E293B))
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono superior
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.Message,
                        contentDescription = null,
                        tint = Color(0xFF2563EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu opinión nos mueve",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Text(
                text = "Cuéntanos cómo podemos mejorar tu experiencia.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Menú Desplegable (Categoría)
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text("Categoría", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = { expandido = !expandido }
                ) {
                    OutlinedTextField(
                        value = categoriaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Selecciona una categoría", color = Color(0xFF94A3B8)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFF2563EB), // Borde azul al seleccionar
                            focusedBorderColor = Color(0xFF2563EB)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false }
                    ) {
                        opcionesCategoria.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    categoriaSeleccionada = opcion
                                    expandido = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de Texto de la Sugerencia
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text("Tu sugerencia", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 8.dp))

                OutlinedTextField(
                    value = textoSugerencia,
                    onValueChange = { if (it.length <= 500) textoSugerencia = it },
                    placeholder = { Text("Escribe aquí tu idea o comentario...", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFF2563EB)
                    )
                )

                Text(
                    text = "Máximo 500 caracteres",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }

            Spacer(modifier = Modifier.weight(1f))

// Botón Enviar
            Button(
                onClick = {
                    if (categoriaSeleccionada.isNotEmpty() && textoSugerencia.isNotBlank()) {
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                        val uid = auth.currentUser?.uid ?: ""

                        // Formateamos el texto para que el admin sepa la categoría
                        val descripcionFinal = "Sugerencia [$categoriaSeleccionada]: $textoSugerencia"

                        // Usamos tu modelo de datos existente
                        val nuevaIncidencia = IncidenciaOficial(
                            id_usuario = uid,
                            descripcion = descripcionFinal,
                            estado = "pendiente",
                            fecha_reporte = System.currentTimeMillis()
                        )

                        database.getReference("incidencias").push().setValue(nuevaIncidencia)
                            .addOnSuccessListener {
                                android.widget.Toast.makeText(context, "Sugerencia enviada. ¡Gracias!", android.widget.Toast.LENGTH_SHORT).show()
                                onVolver() // Regresamos al menú después de enviar
                            }
                            .addOnFailureListener {
                                android.widget.Toast.makeText(context, "Error al enviar sugerencia", android.widget.Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        android.widget.Toast.makeText(context, "Por favor selecciona una categoría y escribe tu sugerencia", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Enviar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAcercaDe(onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Acerca de PopaBus", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            navigationIcon = {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF1E293B))
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta del Logo principal
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2563EB),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus, // O tu icono del bus
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold, fontSize = 32.sp)) {
                                    append("Popa")
                                }
                                withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color(0xFF0D9488), fontWeight = FontWeight.Bold, fontSize = 32.sp)) {
                                    append("Bus")
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Etiqueta de versión
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "v2.0",
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Text("Movilidad que conecta", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(
                text = "Plataforma inteligente para el transporte urbano de Popayán. Desarrollado por estudiantes de Unicomfacauca.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Tarjeta de Beneficios
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    FilaAcercaDe(icono = Icons.Default.Timeline, titulo = "Rutas claras", subtitulo = "Información para moverte mejor") // Reemplaza Timeline si prefieres otro
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F5F9))
                    FilaAcercaDe(icono = Icons.Default.Security, titulo = "Viajes seguros", subtitulo = "Acompañamiento cuando lo necesitas")
                }
            }
        }
    }
}

@Composable
fun FilaAcercaDe(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, subtitulo: String) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF0FDF4), modifier = Modifier.size(40.dp)) {
            Icon(icono, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Text(subtitulo, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAyudaYFrecuentes(titulo: String, onVolver: () -> Unit) {
    var faq1Expandido by remember { mutableStateOf(false) }
    var faq2Expandido by remember { mutableStateOf(false) }
    var faq3Expandido by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Fondo gris claro
    ) {
        CenterAlignedTopAppBar(
            title = { Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
            navigationIcon = {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF1E293B))
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )

        Column(modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
        ) {
            // Cabecera superior
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2563EB), modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¿Cómo podemos ayudarte?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("Encuentra respuestas rápidas sobre PopaBus.", fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
                }
            }

            Text("PREGUNTAS FRECUENTES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 12.dp))

            // Lista tipo acordeón
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Pregunta 1 (Expandible)
                    Column(modifier = Modifier.clickable { faq1Expandido = !faq1Expandido }.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("¿Cómo funciona el ETA?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                            Icon(if (faq1Expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                        }
                        if (faq1Expandido) {
                            Text(
                                "El tiempo estimado se calcula con la ubicación del bus y las condiciones actuales de tránsito.",
                                fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F5F9))

                    // Pregunta 2
                    Column(modifier = Modifier.clickable { faq2Expandido = !faq2Expandido }.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("¿Puedo usar PopaBus sin una cuenta?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                            Icon(if (faq2Expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                        }
                        if (faq2Expandido) {
                            Text(
                                "Si. Puedes consultar rutas y buses cercanos sin registrarte.",
                                fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F5F9))
                        // Pregunta 3
                    Column(modifier = Modifier.clickable { faq3Expandido = !faq3Expandido }.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("¿Cómo reporto un objeto perdido?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                            Icon(if (faq3Expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                        }
                        if (faq3Expandido) {
                            Text(
                                "Contacta a soporte e indica la ruta, hora y descripción del objeto.",
                                fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Contactar a Soporte
            OutlinedButton(
                onClick = { /* Lógica futura de contacto */ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contactar a Soporte", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2563EB))
            }
        }
    }
}

// Lógica para asignar un color dinámico según el número o nombre de la ruta
fun obtenerColorRuta(numeroRuta: String): androidx.compose.ui.graphics.Color {
    return when {
        numeroRuta.contains("7") -> androidx.compose.ui.graphics.Color(0xFF2563EB) // Azul
        numeroRuta.contains("2") -> androidx.compose.ui.graphics.Color(0xFF10B981) // Verde
        numeroRuta.contains("4") -> androidx.compose.ui.graphics.Color(0xFF8B5CF6) // Morado
        numeroRuta.contains("1") -> androidx.compose.ui.graphics.Color(0xFFF59E0B) // Naranja
        else -> androidx.compose.ui.graphics.Color(0xFF64748B) // Gris por defecto
    }
}

// Componente visual para cada tarjeta de ruta sugerida
@Composable
fun TarjetaRutaSugerida(
    numeroRuta: String,
    nombreRuta: String,
    paraderos: String,
    tipoViaje: String,
    eta: String,
    onClick: () -> Unit
) {
    val colorFondo = obtenerColorRuta(numeroRuta)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cuadro de color grande con el número
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(colorFondo, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numeroRuta,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos centrales
            Column(modifier = Modifier.weight(1f)) {
                Text(text = nombreRuta, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text(text = paraderos, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = tipoViaje, fontSize = 12.sp, color = Color.Gray)
            }

            // ETA (Tiempo estimado) a la derecha
            Column(horizontalAlignment = Alignment.End) {
                Text(text = eta, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                Text(text = "Próximo bus", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TarjetaRutaAisladaFija(
    nombreRuta: String,
    origenDestino: String,
    hayBusesActivos: Boolean,
    cantidadParadas: Int,
    tiempoRecorridoTotal: String = "22 min", // Puedes hacerlo dinámico después
    etaActual: String = "5 min", // Aquí va tu cálculo matemático real
    onVerRutaCompleta: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Indicador estético superior (la pequeña línea gris)
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFE2E8F0), CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // FILA 1: Título y Píldora de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título de la ruta (limitado al 50% del ancho)
                Text(
                    text = "Ruta $nombreRuta",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color(0xFF0F172A), // Casi negro
                    modifier = Modifier.weight(1f) // Ocupa el espacio disponible
                )

                // Píldora Dinámica (En camino / No disponible) - Asegurada con el texto
                Surface(
                    shape = CircleShape,
                    color = if (hayBusesActivos) Color(0xFFE8F5E9) else Color(0xFFF1F5F9),
                    modifier = Modifier.padding(start = 8.dp) // Pequeño margen a la izquierda
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (hayBusesActivos) Color(0xFF10B981) else Color(0xFF94A3B8), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hayBusesActivos) "En camino" else "No disponible",
                            color = if (hayBusesActivos) Color(0xFF059669) else Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtítulo de Origen y Destino (limitado al 50% del ancho)
            Text(
                text = origenDestino,
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(0.5f) // Limita el ancho al 50%
            )

            Spacer(modifier = Modifier.height(24.dp))

            // FILA 2: ETA en una sola línea
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = "Próximo bus en ", fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                Text(text = etaActual, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FILA 3: Íconos y estadísticas (Reloj y Paraderos)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reloj
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = tiempoRecorridoTotal, fontSize = 14.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.width(24.dp))

                // Paraderos
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "$cantidadParadas paradas", fontSize = 14.sp, color = Color(0xFF64748B))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FILA 4: Botón Azul de ancho completo
            Button(
                onClick = onVerRutaCompleta,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Ver ruta completa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}