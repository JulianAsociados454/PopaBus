package com.ranger.popabus


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

import com.google.firebase.database.ValueEventListener
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ranger.popabus.ui.theme.PopaBusTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person




import androidx.compose.foundation.BorderStroke

import com.google.firebase.auth.GoogleAuthProvider


import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential


import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas

import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush


import androidx.credentials.CustomCredential
import com.google.firebase.database.ServerValue
import androidx.compose.ui.text.withStyle


import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString






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
// Evolución: El estado inicial ahora es el Splash[cite: 4]
                var pantallaActual by remember { mutableStateOf("SPLASH") }

                when (pantallaActual) {
                    "SPLASH" -> PantallaSplash(onSplashTerminado = { pantallaActual = "LOGIN" })
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
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold, fontSize = 28.sp)) {
                        append("Popa")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF0D9488), fontWeight = FontWeight.Bold, fontSize = 28.sp)) {
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
    var cargando by remember { mutableStateOf(false) }

    // Colores del Mockup
    val primaryBlue = Color(0xFF2563EB)
    val darkText = Color(0xFF1E293B)
    val grayText = Color(0xFF64748B)
    val tealBg = Color(0xFF14B8A6)
    val lightGrayBg = Color(0xFFF8FAFC)
    val cardBg = Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightGrayBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Cabecera con botón de retroceso y Título
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .clickable { onVolverLogin() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = darkText
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Crear Cuenta", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = darkText))
            Spacer(modifier = Modifier.weight(1.3f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Icono Teal de Usuario
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(tealBg, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Person,
                contentDescription = "Usuario",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Textos
        Text("Empieza a moverte mejor", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = darkText))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Crea tu cuenta para guardar rutas y recibir alertas personalizadas.",
            textAlign = TextAlign.Center,
            color = grayText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Tarjeta del Formulario
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Campo: Nombre
                Text("Nombre completo", fontWeight = FontWeight.Bold, color = darkText, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text("Tu nombre y apellido", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = lightGrayBg, focusedContainerColor = lightGrayBg,
                        unfocusedBorderColor = Color.Transparent, focusedBorderColor = primaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo: Correo
                Text("Correo", fontWeight = FontWeight.Bold, color = darkText, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text("tu@correo.com", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = lightGrayBg, focusedContainerColor = lightGrayBg,
                        unfocusedBorderColor = Color.Transparent, focusedBorderColor = primaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo: Contraseña
                Text("Contraseña", fontWeight = FontWeight.Bold, color = darkText, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    placeholder = { Text("Mínimo 8 caracteres", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = lightGrayBg, focusedContainerColor = lightGrayBg,
                        unfocusedBorderColor = Color.Transparent, focusedBorderColor = primaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Registrarse (Lógica Original Intacta)
                Button(
                    onClick = {
                        if (correo.isNotEmpty() && contrasena.isNotEmpty() && nombre.isNotEmpty()) {
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
                            Toast.makeText(context, "Por favor, llena todos los campos.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    enabled = !cargando
                ) {
                    Text(if (cargando) "Registrando..." else "Registrarse", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Enlace al Login
        Row(horizontalArrangement = Arrangement.Center) {
            Text("¿Ya tienes una cuenta? ", color = grayText, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Volver al Login",
                color = primaryBlue,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.clickable { onVolverLogin() }
            )
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
                            if (!permisoConcedido) {
                                Toast.makeText(context, "Se requieren permisos de ubicación para iniciar.", Toast.LENGTH_LONG).show()
                                return@Button
                            }

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

                                        try {
                                            val intent = Intent(context, ServicioGpsConductor::class.java)
                                            intent.putExtra("ID_RECORRIDO", idGenerado)
                                            intent.putExtra("ID_BUS", busIdReal)
                                            ContextCompat.startForegroundService(context, intent)

                                            Toast.makeText(context, "Viaje Oficial Iniciado", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Log.e("DepuradorBuses", "Crash evitado al iniciar Foreground Service", e)
                                            Toast.makeText(context, "No se pudo iniciar el servicio GPS.", Toast.LENGTH_LONG).show()
                                        }
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
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
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
    var vistaAyudaActual by rememberSaveable { mutableStateOf<String?>(null) }

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

    // Memoria para saber qué ruta tocó el usuario
    var idRutaSeleccionada by remember { mutableStateOf<String?>(null) }

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
    var textoBusqueda by remember { mutableStateOf("") }
    var menuSugerenciasExpandido by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    var paraderoSeleccionadoBusqueda by remember { mutableStateOf<ParaderoOficial?>(null) }

    val rutasProcesadas = remember(rutasOficiales) {
        rutasOficiales.filter { 
            it.estado == "activa" && 
            it.geometria.isNotEmpty() &&
            (it.id.equals("RUTA_RECORRIDO_1", ignoreCase = true) || it.id.equals("RUTA_RECORRIDO_2", ignoreCase = true))
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
        gesturesEnabled = drawerState.isOpen,
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
                
                if (auth.currentUser != null) {
                    NavigationDrawerItem(
                        label = { Text("⚠️ Reportar") },
                        selected = false,
                        onClick = {
                            mostrarDialogoIncidencia = true
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
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
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. El Mapa va al fondo
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties
                ) {
// --- NUEVO SPRINT: RENDERIZADO INTELIGENTE DE RUTAS ---
                    if (mostrarRutas) {
                        rutasProcesadas.forEachIndexed { index, ruta ->
                            val colorBase = paletaColores[index % paletaColores.size]
                            val estaSeleccionada = (idRutaSeleccionada == ruta.id)
                            val mostrandoTodas = (idRutaSeleccionada == null)

                            // Solo dibujamos si estamos viendo todas, o si es la que el usuario seleccionó
                            if (mostrandoTodas || estaSeleccionada) {
                                val puntosLinea = ruta.geometria.map { LatLng(it.latitud, it.longitud) }

                                com.google.maps.android.compose.Polyline(
                                    points = puntosLinea,
                                    // Si está enfocada brilla al 100%, si no, se vuelve semitransparente (0.4f)
                                    color = if (estaSeleccionada) colorBase else colorBase.copy(alpha = 0.4f),
                                    // Si está enfocada es muy gruesa, si no, más delgada
                                    width = if (estaSeleccionada) 22f else 12f,
                                    // La seleccionada siempre se dibuja por encima de las demás
                                    zIndex = if (estaSeleccionada) 2f else 1f,
                                    clickable = true,
                                    onClick = {
                                        // Al tocar la línea, se enfoca. Al tocarla de nuevo, vuelve a mostrar todas.
                                        idRutaSeleccionada = if (estaSeleccionada) null else ruta.id
                                    }
                                )
                            }
                        }
                    }

                    val paraderosRegulares = if (idRutaSeleccionada != null && idRutaSeleccionada != "NINGUNA") {
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
                    }.toMutableList()

                    if (paraderoSeleccionadoBusqueda != null && !paraderosRegulares.any { it.id == paraderoSeleccionadoBusqueda!!.id }) {
                        paraderosRegulares.add(paraderoSeleccionadoBusqueda!!)
                    }

                    paraderosRegulares.forEach { paradero ->
                        Marker(
                            state = MarkerState(position = LatLng(paradero.latitud, paradero.longitud)),
                            title = paradero.nombre,
                            snippet = paradero.direccion,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
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
                        .fillMaxWidth(0.92f)
                        .zIndex(2f), // Asegura que no sea bloqueado y esté por encima del mapa
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { 
                            textoBusqueda = it 
                            menuSugerenciasExpandido = true
                        },
                        label = { Text("¿A dónde vas?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp)),
                        singleLine = true,
                        trailingIcon = {
                            if (textoBusqueda.isNotEmpty()) {
                                IconButton(onClick = { 
                                    textoBusqueda = "" 
                                    menuSugerenciasExpandido = false
                                }) {
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

                    val sugerenciasParaderos = if (textoBusqueda.isNotBlank()) {
                        paraderosProcesados.filter {
                            it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                            it.direccion.contains(textoBusqueda, ignoreCase = true)
                        }.take(5)
                    } else emptyList()

                    val sugerenciasRutas = if (textoBusqueda.isNotBlank()) {
                        rutasProcesadas.filter {
                            it.nombre_ruta.contains(textoBusqueda, ignoreCase = true) ||
                            it.origen.contains(textoBusqueda, ignoreCase = true) ||
                            it.destino.contains(textoBusqueda, ignoreCase = true)
                        }.take(5)
                    } else emptyList()

                    if (menuSugerenciasExpandido && (sugerenciasParaderos.isNotEmpty() || sugerenciasRutas.isNotEmpty())) {
                        androidx.compose.material3.Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.heightIn(max = 220.dp)
                            ) {
                                items(sugerenciasParaderos) { paradero ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("📍 Paradero: ${paradero.nombre}") },
                                        onClick = {
                                            textoBusqueda = paradero.nombre
                                            menuSugerenciasExpandido = false
                                            paraderoSeleccionadoBusqueda = paradero
                                            idRutaSeleccionada = paradero.id_ruta // Seleccionar la ruta asociada al paradero
                                            focusManager.clearFocus()
                                            coroutineScope.launch {
                                                cameraPositionState.animate(
                                                    CameraUpdateFactory.newLatLngZoom(
                                                        LatLng(paradero.latitud, paradero.longitud),
                                                        16f
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                                items(sugerenciasRutas) { ruta ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("🚌 Ruta: ${ruta.nombre_ruta}") },
                                        onClick = {
                                            textoBusqueda = ruta.nombre_ruta
                                            menuSugerenciasExpandido = false
                                            idRutaSeleccionada = ruta.id
                                            focusManager.clearFocus()
                                            if (ruta.geometria.isNotEmpty()) {
                                                val firstPoint = ruta.geometria.first()
                                                coroutineScope.launch {
                                                    cameraPositionState.animate(
                                                        CameraUpdateFactory.newLatLngZoom(
                                                            LatLng(firstPoint.latitud, firstPoint.longitud),
                                                            14f
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

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
                                    onCheckedChange = { 
                                        mostrarRutas = it 
                                        if (it) idRutaSeleccionada = null
                                    }
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
                FloatingActionButton(
                    onClick = { mostrarDialogoPanico = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 96.dp, end = 16.dp),
                    containerColor = Color.Red
                ) {
                    Text("SOS", color = Color.White)
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
                    var listaActividades by remember { mutableStateOf<List<ActividadUsuario>>(emptyList()) }
                    
                    DisposableEffect(Unit) {
                        val uid = auth.currentUser?.uid ?: ""
                        val refHistorial = FirebaseDatabase.getInstance().getReference("actividad_usuario").child(uid)
                        
                        val listener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val actividades = mutableListOf<ActividadUsuario>()
                                for (actividadSnapshot in snapshot.children) {
                                    val actividad = actividadSnapshot.getValue(ActividadUsuario::class.java)
                                    if (actividad != null) {
                                        actividades.add(actividad)
                                    }
                                }
                                // Ordenamos de la más reciente a la más antigua
                                listaActividades = actividades.sortedByDescending { it.fecha_abordaje }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Historial", "Error al leer historial", error.toException())
                            }
                        }
                        refHistorial.addValueEventListener(listener)
                        
                        onDispose { refHistorial.removeEventListener(listener) }
                    }

                    VistaActividad(
                        listaActividades = listaActividades,
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
    var subPantalla by rememberSaveable { mutableStateOf("MENU") }

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
    val refRutas = database.getReference("rutas")

    var paraderoSeleccionado by remember { mutableStateOf<ParaderoOficial?>(null) }

    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var idRuta by remember { mutableStateOf("") }
    var expandedRuta by remember { mutableStateOf(false) }

    // Estado para la lista en tiempo real
    var listaParaderos by remember { mutableStateOf<List<ParaderoOficial>>(emptyList()) }
    var listaRutas by remember { mutableStateOf<List<RutaOficial>>(emptyList()) }

    // Descargamos los paraderos y rutas de Firebase en tiempo real
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
        refParaderos.addValueEventListener(listener)
        refRutas.addValueEventListener(listenerRutas)
        onDispose { 
            refParaderos.removeEventListener(listener) 
            refRutas.removeEventListener(listenerRutas)
        }
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
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = idRuta, onValueChange = { idRuta = it }, label = { Text("ID de la Ruta (Ej: RUTA_RECORRIDO_1)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

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
                        estado = "activo",
                        id_ruta = idRuta.trim()
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
            var editIdRuta by remember(paradero) { mutableStateOf(paradero.id_ruta) }

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
                        OutlinedTextField(
                            value = editIdRuta,
                            onValueChange = { editIdRuta = it },
                            label = { Text("ID de Ruta") },
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
                label = { Text("Asignar Ruta") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRuta) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
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
                label = { Text("Asignar Conductor") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedConductor) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
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
                val nombreRuta = listaRutas.find { it.id == bus.id_ruta }?.nombre_ruta ?: "Ninguna"
                val nombreConductor = listaConductores.find { it.id == bus.id_conductor_actual }?.nombre_completo ?: "Ninguno"

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
                        Text("Ruta Asignada: $nombreRuta", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("Conductor: $nombreConductor", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                label = { Text("Asignar Ruta") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEditRuta) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
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
                label = { Text("Asignar Conductor") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEditConductor) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión Humana", style = MaterialTheme.typography.headlineSmall)
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

        Text("Usuarios en esta categoría: ${usuariosFiltrados.size}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Lista visual con funcionalidad CRUD
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(usuariosFiltrados) { usuario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { usuarioSeleccionado = usuario }, // Se asigna el usuario al tocar
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("👤 ${usuario.nombre_completo}", style = MaterialTheme.typography.titleMedium)
                        Text("📧 Correo: ${usuario.correo.ifEmpty { "Sin sincronizar (requiere iniciar sesión)" }}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
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
                        Text("📧 Correo: ${usuario.correo.ifEmpty { "Sin sincronizar (requiere iniciar sesión)" }}", style = MaterialTheme.typography.bodyMedium)
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
                                    if (usuario.rol.trim().lowercase() == "administrador") {
                                        Toast.makeText(context, "Seguridad: Administradores no editables", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val nuevoEstado = !usuario.activo
                                        refUsuarios.child(usuario.id).updateChildren(mapOf("activo" to nuevoEstado))
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                                                usuarioSeleccionado = usuarioSeleccionado?.copy(activo = nuevoEstado)
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
                                        Toast.makeText(context, "Seguridad: No editable", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val nuevoRol = if (usuario.rol.trim().lowercase() == "pasajero") "conductor" else "pasajero"
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
                            if (usuario.rol.trim().lowercase() == "administrador") {
                                Toast.makeText(context, "Seguridad: Administradores no se pueden eliminar", Toast.LENGTH_SHORT).show()
                            } else {
                                refUsuarios.child(usuario.id).removeValue().addOnSuccessListener {
                                    Toast.makeText(context, "Usuario eliminado definitivamente", Toast.LENGTH_SHORT).show()
                                    usuarioSeleccionado = null
                                }.addOnFailureListener {
                                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
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
            drawLine(color = Color(0xFFF1F5F9), start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.2f), end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.45f), strokeWidth = 4f)
            drawLine(color = Color(0xFFF1F5F9), start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.85f), end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.6f), strokeWidth = 4f)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Center).offset(x = offsetX.value.dp).alpha(alpha.value)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.End) {
                val gradient = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0x000D9488), Color(0xFF0D9488)))
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
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp).alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF2563EB), modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Filled.DirectionsBus, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Popa", fontSize = 32.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFF1E293B))
                Text("Bus", fontSize = 32.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFF0D9488))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Tu bus, en tiempo real", fontSize = 14.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(24.dp))

            // NUEVO: Contenedor gris estático y barra turquesa dinámica superpuesta
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(50)), // Fondo gris clarito
                contentAlignment = Alignment.CenterStart
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(progresoBarra.value) // El ancho depende del estado animado (0 a 1f)
                        .height(3.dp)
                        .background(Color(0xFF0D9488), RoundedCornerShape(50)) // Barra verde turquesa
                )
            }
        }
    }
}