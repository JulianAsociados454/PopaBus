package com.ranger.popabus
import android.Manifest
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    val scope = rememberCoroutineScope()
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
                    scope.launch {
                        try {
                            val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.login(
                                com.ranger.popabus.network.LoginRequest(
                                    correo = correo,
                                    contrasena = contrasena
                                )
                            )
                            cargando = false

                            if (respuesta.isSuccessful && respuesta.body()?.status == "ok") {
                                val datos = respuesta.body()!!
                                Toast.makeText(
                                    context,
                                    "Bienvenido ${datos.nombre_completo}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onLoginExitoso(datos.rol)
                            } else {
                                // Leemos el mensaje de error real del backend
                                val errorJson = respuesta.errorBody()?.string()
                                val msj = if (!errorJson.isNullOrEmpty()) {
                                    try {
                                        val jsonObject = org.json.JSONObject(errorJson)
                                        jsonObject.getString("mensaje")
                                    } catch (e: Exception) {
                                        "Error ${respuesta.code()}"
                                    }
                                } else {
                                    "Error ${respuesta.code()}"
                                }
                                Toast.makeText(context, msj, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            cargando = false
                            Toast.makeText(context, "❌ Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("LoginTest", "Error en login", e)
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
    val scope = rememberCoroutineScope()
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
                if (correo.isNotEmpty() && contrasena.isNotEmpty() && nombre.isNotEmpty()) {
                    scope.launch {
                        try {
                            val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.registrar(
                                com.ranger.popabus.network.RegisterRequest(
                                    correo = correo,
                                    contrasena = contrasena,
                                    nombre_completo = nombre,
                                    rol = rolSeleccionado
                                )
                            )

                            if (respuesta.isSuccessful && respuesta.body()?.status == "ok") {
                                val datos = respuesta.body()!!
                                Toast.makeText(
                                    context,
                                    " Cuenta creada: ${datos.nombre_completo}",
                                    Toast.LENGTH_LONG
                                ).show()
                                // Notificamos a MainActivity para que navegue
                                onRegistroExitoso(datos.uid, datos.nombre_completo, datos.rol)
                            } else {
                                val msj = respuesta.body()?.mensaje ?: "Error ${respuesta.code()}"
                                Toast.makeText(context, " $msj", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, " Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("RegisterTest", "Error en registro", e)
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
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var transmitiendo by remember { mutableStateOf(false) }
    var permisoConcedido by remember { mutableStateOf(false) }
    var enRecorrido by remember { mutableStateOf(false) }

    // Instancias de Firebase (solo para el radar de buses por ahora)
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }

    var idRecorridoActual by remember { mutableStateOf<String?>(null) }

    val popayan = LatLng(2.4419, -76.6063)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(popayan, 15f)
    }
    val mapProperties = MapProperties(isMyLocationEnabled = permisoConcedido)

    var busesEnZona by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }
    val iconoBus = remember { obtenerIconoBus(context, R.drawable.ic_bus) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permisos ->
            permisoConcedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // RADAR DE COMPAÑEROS (Dejamos Firebase aquí por ahora para no romper el radar)
    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val prefijoCentro = calcularGeohash(popayan.latitude, popayan.longitude, 4)
        val query = database.getReference("BusesActivos")
            .orderByChild("geohash").startAt(prefijoCentro).endAt(prefijoCentro + "\uf8ff")
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
                    if (lat != null && lng != null) busesRecibidos[idBus] = LatLng(lat, lng)
                }
                busesEnZona = busesRecibidos
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        query.addValueEventListener(listener)
        onDispose { query.removeEventListener(listener) }
    }

    // --- MOTOR DE ALERTAS EN LA VÍA (AHORA LEE DE POSTGRESQL) ---
    var incidenciasActivas by remember { mutableStateOf<List<com.ranger.popabus.network.IncidenciaRespuesta>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.listarIncidencias()
                if (respuesta.isSuccessful) {
                    incidenciasActivas = respuesta.body()?.filter { it.latitud != 0.0 } ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ConductorIncidencias", "Error: ${e.message}")
            }
            delay(5000) // Consulta cada 5 segundos
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {
            busesEnZona.forEach { (idBus, destino) ->
                key(idBus) {
                    MarcadorBusAnimado(idBus, destino, iconoBus, "Compañero")
                }
            }
            incidenciasActivas.forEach { incidencia ->
                Marker(
                    state = MarkerState(position = LatLng(incidencia.latitud, incidencia.longitud)),
                    title = "⚠️ Alerta Reportada",
                    snippet = incidencia.descripcion,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (!enRecorrido) "Panel Táctico" else "🟢 Viaje en Curso",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (!enRecorrido) Color.Black else Color(0xFF006400)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (!enRecorrido) {
                    Button(
                        onClick = {
                            val uidConductor = "uid_conductor_001"
                            scope.launch {
                                try {
                                    val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.obtenerBusConductor(uidConductor)
                                    if (respuesta.isSuccessful && respuesta.body()?.status == "ok") {
                                        val bus = respuesta.body()!!.bus!!
                                        val idGenerado = "rec_${System.currentTimeMillis()}"
                                        idRecorridoActual = idGenerado
                                        enRecorrido = true
                                        transmitiendo = true

                                        val intent = Intent(context, ServicioGpsConductor::class.java)
                                        intent.putExtra("ID_RECORRIDO", idGenerado)
                                        intent.putExtra("ID_BUS", bus.id)
                                        ContextCompat.startForegroundService(context, intent)
                                        Toast.makeText(context, "Viaje Iniciado", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "No tienes bus asignado", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error de red", Toast.LENGTH_LONG).show()
                                }
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
                            enRecorrido = false
                            transmitiendo = false
                            idRecorridoActual = null
                            context.stopService(Intent(context, ServicioGpsConductor::class.java))
                            Toast.makeText(context, "Viaje Finalizado", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🛑 Finalizar Recorrido")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- NUEVOS BOTONES DE REPORTE (INTEGRADOS A POSTGRESQL) ---
                Text("Reportar Incidencia en la Vía", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                scope.launch {
                                    com.ranger.popabus.network.RetrofitClient.apiService.crearIncidencia(
                                        com.ranger.popabus.network.IncidenciaRespuesta(
                                            id_usuario = "uid_conductor_001", descripcion = "Trancón",
                                            estado = "pendiente", fecha_reporte = System.currentTimeMillis()
                                        )
                                    )
                                    Toast.makeText(context, "Trancón reportado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Trancón") }

                        Button(
                            onClick = {
                                scope.launch {
                                    com.ranger.popabus.network.RetrofitClient.apiService.crearIncidencia(
                                        com.ranger.popabus.network.IncidenciaRespuesta(
                                            id_usuario = "uid_conductor_001", descripcion = "Desvío",
                                            estado = "pendiente", fecha_reporte = System.currentTimeMillis()
                                        )
                                    )
                                    Toast.makeText(context, "Desvío reportado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D))
                        ) { Text("Desvío") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                scope.launch {
                                    com.ranger.popabus.network.RetrofitClient.apiService.crearIncidencia(
                                        com.ranger.popabus.network.IncidenciaRespuesta(
                                            id_usuario = "uid_conductor_001", descripcion = "Fallo Mecánico",
                                            estado = "pendiente", fecha_reporte = System.currentTimeMillis()
                                        )
                                    )
                                    Toast.makeText(context, "Fallo reportado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) { Text("Fallo") }

                        Button(
                            onClick = {
                                scope.launch {
                                    com.ranger.popabus.network.RetrofitClient.apiService.crearIncidencia(
                                        com.ranger.popabus.network.IncidenciaRespuesta(
                                            id_usuario = "uid_conductor_001", descripcion = "Pánico Conductor",
                                            estado = "urgente", fecha_reporte = System.currentTimeMillis()
                                        )
                                    )
                                    Toast.makeText(context, "Alerta enviada", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) { Text("Pánico") }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        transmitiendo = false
                        context.stopService(Intent(context, ServicioGpsConductor::class.java))
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

    var incidenciasActivas by remember { mutableStateOf<List<com.ranger.popabus.network.IncidenciaRespuesta>>(emptyList()) }
    var rutasOficiales by remember { mutableStateOf<List<com.ranger.popabus.network.RutaRespuesta>>(emptyList()) }
    var paraderosOficiales by remember { mutableStateOf<List<com.ranger.popabus.network.ParaderoRespuesta>>(emptyList()) }
    var mostrarRutas by remember { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var vistaAyudaActual by rememberSaveable { mutableStateOf<String?>(null) }
    var rastreandoAbordaje by remember { mutableStateOf(false) }
    var mostrarDialogoPanico by remember { mutableStateOf(false) }

    val popayan = LatLng(2.4419, -76.6063)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(popayan, 14f)
    }

    var ubicacionPasajero by remember { mutableStateOf<LatLng?>(null) }
    var busesEnZona by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }
    val iconoBus = remember { obtenerIconoBus(context, R.drawable.ic_bus) }
    var textoBusqueda by remember { mutableStateOf("") }

    // Permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permisos ->
            permisoConcedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (permisoConcedido) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val miUbicacion = LatLng(location.latitude, location.longitude)
                        ubicacionPasajero = miUbicacion
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(miUbicacion, 16f), 2000)
                        }
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    // --- MOTOR 1: Buses ---
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.obtenerBusesActivos()
                if (respuesta.isSuccessful && respuesta.body()?.status == "ok") {
                    val mapa = mutableMapOf<String, LatLng>()
                    for (bus in respuesta.body()!!.buses) {
                        mapa[bus.id_bus] = LatLng(bus.latitud, bus.longitud)
                    }
                    busesEnZona = mapa
                }
            } catch (e: Exception) {}
            delay(3000)
        }
    }

    // --- MOTOR 2: Alertas ---
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.listarIncidencias()
                if (respuesta.isSuccessful) {
                    incidenciasActivas = respuesta.body()?.filter { it.latitud != 0.0 } ?: emptyList()
                }
            } catch (e: Exception) {}
            delay(5000)
        }
    }

    // --- MOTOR 3: Rutas y Paraderos ---
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val respRutas = com.ranger.popabus.network.RetrofitClient.apiService.listarRutas()
                if (respRutas.isSuccessful) rutasOficiales = respRutas.body() ?: emptyList()

                val respParaderos = com.ranger.popabus.network.RetrofitClient.apiService.listarParaderos()
                if (respParaderos.isSuccessful) paraderosOficiales = respParaderos.body() ?: emptyList()
            } catch (e: Exception) {}
            delay(10000)
        }
    }

    // ⚠️ AQUÍ ESTÁ LA MAGIA: Convertimos el JSON a coordenadas ANTES de dibujar
    val puntosRutas = remember(rutasOficiales) {
        rutasOficiales.filter { it.estado == "activa" && it.geometria.isNotEmpty() }.mapNotNull { ruta ->
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<CoordenadaRuta>>() {}.type
                val puntos: List<CoordenadaRuta> = com.google.gson.Gson().fromJson(ruta.geometria, type)
                puntos.map { LatLng(it.latitud, it.longitud) }
            } catch (e: Exception) {
                null // Si una ruta está mal formada, la ignoramos
            }
        }
    }

    val mapProperties = MapProperties(isMyLocationEnabled = permisoConcedido)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Text("Ayuda", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(label = { Text("Sugerencias") }, selected = false, onClick = { vistaAyudaActual = "SUGERENCIAS"; coroutineScope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                NavigationDrawerItem(label = { Text("Acerca De") }, selected = false, onClick = { vistaAyudaActual = "ACERCA"; coroutineScope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                NavigationDrawerItem(label = { Text("Preguntas Frecuentes") }, selected = false, onClick = { vistaAyudaActual = "PREGUNTAS"; coroutineScope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties
                ) {
                    // 1. RUTAS (Usando la lista pre-procesada)
                    if (mostrarRutas) {
                        puntosRutas.forEach { puntosLatLng ->
                            if (puntosLatLng.isNotEmpty()) {
                                com.google.maps.android.compose.Polyline(
                                    points = puntosLatLng,
                                    color = Color.Red,
                                    width = 15f
                                )
                            }
                        }
                    }

                    // 2. PARADEROS
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

                    // 3. BUSES
                    busesEnZona.forEach { (idBus, destino) ->
                        key(idBus) {
                            val eta = if (ubicacionPasajero != null) {
                                obtenerTextoETA(calcularDistanciaMetros(ubicacionPasajero!!, destino))
                            } else "Calculando..."
                            MarcadorBusAnimado(idBus, destino, iconoBus, eta)
                        }
                    }

                    // 4. ALERTAS
                    incidenciasActivas.forEach { incidencia ->
                        Marker(
                            state = MarkerState(position = LatLng(incidencia.latitud, incidencia.longitud)),
                            title = "⚠️ Alerta",
                            snippet = incidencia.descripcion,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        )
                    }
                }

                // Barra de búsqueda y Switch
                Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).fillMaxWidth(0.92f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        label = { Text("¿A dónde vas?") },
                        modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp)),
                        singleLine = true,
                        trailingIcon = { TextButton(onClick = { textoBusqueda = "" }) { Text("🔍") } }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🗺️ Ver Rutas", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = mostrarRutas,
                            onCheckedChange = { mostrarRutas = it }
                        )
                    }
                }

                // --- BOTÓN SOS ---
                FloatingActionButton(
                    onClick = { mostrarDialogoPanico = true },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 96.dp, end = 16.dp),
                    containerColor = Color.Red
                ) {
                    Text("SOS", color = Color.White)
                }

                if (mostrarDialogoPanico) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoPanico = false },
                        title = { Text("⚠️ ALERTA DE PÁNICO") },
                        text = { Text("¿Estás seguro de enviar una alerta de pánico prioritaria?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.crearIncidencia(
                                                com.ranger.popabus.network.IncidenciaRespuesta(
                                                    id_usuario = "anonimo",
                                                    descripcion = "PÁNICO PASAJERO",
                                                    estado = "urgente",
                                                    fecha_reporte = System.currentTimeMillis(),
                                                    latitud = ubicacionPasajero?.latitude ?: popayan.latitude,
                                                    longitud = ubicacionPasajero?.longitude ?: popayan.longitude
                                                )
                                            )
                                            if (respuesta.isSuccessful) {
                                                Toast.makeText(context, "🚨 Alerta de pánico enviada", Toast.LENGTH_LONG).show()
                                                mostrarDialogoPanico = false
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) { Text("Enviar Alerta") }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoPanico = false }) { Text("Cancelar") }
                        }
                    )
                }

                // --- BOTÓN DE ABORDAJE ---
                Button(
                    onClick = {
                        rastreandoAbordaje = !rastreandoAbordaje
                        val intentRastreo = Intent(context, ServicioRastreoPasajero::class.java)
                        if (rastreandoAbordaje) ContextCompat.startForegroundService(context, intentRastreo)
                        else context.stopService(intentRastreo)
                    },
                    modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 32.dp, start = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (rastreandoAbordaje) MaterialTheme.colorScheme.primary else Color.DarkGray)
                ) {
                    Text(if (rastreandoAbordaje) "⏹️ Detener Búsqueda" else "✅ Registrar Abordaje")
                }

                // Botón de Cerrar Sesión
                Button(
                    onClick = onCerrarSesion,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cerrar Sesión")
                }

                // Vistas de ayuda
                if (vistaAyudaActual == "ACERCA") {
                    AlertDialog(onDismissRequest = { vistaAyudaActual = null }, title = { Text("Acerca de POPABUS") }, text = { Text("Plataforma de movilidad para Popayán.") }, confirmButton = { TextButton(onClick = { vistaAyudaActual = null }) { Text("Cerrar") } })
                } else if (vistaAyudaActual == "PREGUNTAS") {
                    AlertDialog(onDismissRequest = { vistaAyudaActual = null }, title = { Text("Preguntas Frecuentes") }, text = { Text("Usa la barra superior para buscar rutas o reportar problemas.") }, confirmButton = { TextButton(onClick = { vistaAyudaActual = null }) { Text("Cerrar") } })
                } else if (vistaAyudaActual == "SUGERENCIAS") {
                    var sugerencia by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { vistaAyudaActual = null },
                        title = { Text("Buzón de Sugerencias") },
                        text = { OutlinedTextField(value = sugerencia, onValueChange = { sugerencia = it }, label = { Text("Tu sugerencia...") }) },
                        confirmButton = { Button(onClick = { Toast.makeText(context, "Enviado (Simulado)", Toast.LENGTH_SHORT).show(); vistaAyudaActual = null }) { Text("Enviar") } }
                    )
                }
            }
        }
    }
}


@Composable
fun PantallaAdministrador(onCerrarSesion: () -> Unit) {
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

                Button(onClick = { subPantalla = "USUARIOS" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("👥 Gestión Humana")
                }
                Spacer(modifier = Modifier.height(16.dp))

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

                // --- NUEVOS BOTONES DE TU AMIGO ---
                Button(onClick = { subPantalla = "REPORTES" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("📊 Buzón de Reportes")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { subPantalla = "INFORMES" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("📈 Gestión de Informes")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // --- ESTE ES EL BOTÓN QUE FALTABA ---
                Button(onClick = { subPantalla = "TORRE_CONTROL" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                    Text("📡 Torre de Control (Mapa)")
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
        "INFORMES" -> PantallaGestionInformes(onVolver = { subPantalla = "MENU" })
        "USUARIOS" -> PantallaGestionUsuarios(onVolver = { subPantalla = "MENU" })

        // --- ESTA ES LA RUTA QUE FALTABA ---
        "TORRE_CONTROL" -> MapaGeneralAdmin(onVolver = { subPantalla = "MENU" })
    }
}
@Composable
fun PantallaGestionParaderos(onVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }

    var listaParaderos by remember { mutableStateOf<List<com.ranger.popabus.network.ParaderoRespuesta>>(emptyList()) }

    fun recargarParaderos() {
        scope.launch {
            try {
                val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.listarParaderos()
                if (respuesta.isSuccessful) {
                    listaParaderos = respuesta.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ParaderosAdmin", "Error listando paraderos: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        recargarParaderos()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de Paraderos", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

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
                    scope.launch {
                        try {
                            val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.crearParadero(
                                com.ranger.popabus.network.ParaderoRespuesta(
                                    nombre = nombre,
                                    direccion = direccion,
                                    latitud = lat,
                                    longitud = lng
                                )
                            )
                            if (respuesta.isSuccessful) {
                                Toast.makeText(context, "Paradero Guardado", Toast.LENGTH_SHORT).show()
                                nombre = ""
                                direccion = ""
                                latitud = ""
                                longitud = ""
                                recargarParaderos()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Llene nombre y coordenadas válidas", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Crear Paradero")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Paraderos Registrados: ${listaParaderos.size}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

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

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val resp = com.ranger.popabus.network.RetrofitClient.apiService.eliminarParadero(paradero.id)
                                        if (resp.isSuccessful) {
                                            Toast.makeText(context, "Paradero eliminado", Toast.LENGTH_SHORT).show()
                                            recargarParaderos()
                                        }
                                    } catch (e: Exception) {}
                                }
                            }
                        ) {
                            Text("🗑️ Eliminar")
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
    val scope = rememberCoroutineScope()

    // Estados del formulario
    var placa by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var idRuta by remember { mutableStateOf("") }
    var idConductor by remember { mutableStateOf("") }

    // Lista de buses
    var listaBuses by remember { mutableStateOf<List<com.ranger.popabus.network.BusRespuesta>>(emptyList()) }

    // Función para recargar la lista
    fun recargarBuses() {
        scope.launch {
            try {
                val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.listarBuses()
                if (respuesta.isSuccessful) {
                    listaBuses = respuesta.body() ?: emptyList()
                    Log.d("BusesAdmin", "Cargados ${listaBuses.size} buses")
                }
            } catch (e: Exception) {
                Log.e("BusesAdmin", "Error listando buses: ${e.message}")
            }
        }
    }

    // Carga inicial
    LaunchedEffect(Unit) {
        recargarBuses()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de Vehículos", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

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
                    scope.launch {
                        try {
                            val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.crearBus(
                                com.ranger.popabus.network.BusRespuesta(
                                    id = "",
                                    placa = placa,
                                    modelo = modelo,
                                    id_ruta = idRuta,
                                    id_conductor_actual = idConductor,
                                    estado = "activo"
                                )
                            )
                            if (respuesta.isSuccessful) {
                                Toast.makeText(context, "Bus Registrado", Toast.LENGTH_SHORT).show()
                                placa = ""
                                modelo = ""
                                idRuta = ""
                                idConductor = ""
                                recargarBuses()
                            } else {
                                Toast.makeText(context, "Error al guardar (${respuesta.code()})", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("BusesAdmin", "Error creando bus", e)
                        }
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

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.eliminarBus(bus.id)
                                        if (respuesta.isSuccessful) {
                                            Toast.makeText(context, "Bus eliminado", Toast.LENGTH_SHORT).show()
                                            recargarBuses()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text("🗑️ Eliminar")
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
    val scope = rememberCoroutineScope()

    var listaUsuarios by remember { mutableStateOf<List<com.ranger.popabus.network.UsuarioRespuesta>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    fun recargarUsuarios() {
        scope.launch {
            try {
                val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.listarUsuarios()
                if (respuesta.isSuccessful) {
                    listaUsuarios = respuesta.body() ?: emptyList()
                }
                cargando = false
            } catch (e: Exception) {
                Log.e("UsuariosAdmin", "Error listando usuarios: ${e.message}")
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        recargarUsuarios()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión Humana", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            CircularProgressIndicator()
        } else {
            Text("Usuarios Registrados: ${listaUsuarios.size}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(listaUsuarios) { usuario ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("👤 ${usuario.nombre_completo}", style = MaterialTheme.typography.titleMedium)
                            Text("📧 ${usuario.correo}", style = MaterialTheme.typography.bodySmall)
                            Text("Rol: ${usuario.rol.uppercase()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Text("Estado: ${if (usuario.activo) "🟢 Activo" else "🔴 Suspendido"}", style = MaterialTheme.typography.bodySmall)
                            Text("UID: ${usuario.uid}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 1. Botón para Suspender / Activar
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val nuevoEstado = !usuario.activo
                                                val resp = com.ranger.popabus.network.RetrofitClient.apiService.cambiarEstadoUsuario(
                                                    usuario.uid,
                                                    mapOf("activo" to nuevoEstado)
                                                )
                                                if (resp.isSuccessful) {
                                                    Toast.makeText(context, if (nuevoEstado) "Cuenta Activada" else "Cuenta Suspendida", Toast.LENGTH_SHORT).show()
                                                    recargarUsuarios()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Text(if (usuario.activo) "🔴 Suspender" else "🟢 Activar")
                                }

                                // 2. Botón para Cambiar Rol
                                OutlinedButton(
                                    onClick = {
                                        if (usuario.rol.lowercase() == "administrador") {
                                            Toast.makeText(context, "SEGURIDAD: Los administradores no se cambian aquí", Toast.LENGTH_LONG).show()
                                        } else {
                                            scope.launch {
                                                try {
                                                    val nuevoRol = if (usuario.rol.lowercase() == "pasajero") "conductor" else "pasajero"
                                                    val resp = com.ranger.popabus.network.RetrofitClient.apiService.cambiarRolUsuario(
                                                        usuario.uid,
                                                        mapOf("rol" to nuevoRol)
                                                    )
                                                    if (resp.isSuccessful) {
                                                        Toast.makeText(context, "Rol cambiado a $nuevoRol", Toast.LENGTH_SHORT).show()
                                                        recargarUsuarios()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                                                }
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
            Text("Volver al Menú")
        }
    }
}
@Composable
fun PantallaGestionReportes(onVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var listaIncidencias by remember { mutableStateOf<List<com.ranger.popabus.network.IncidenciaRespuesta>>(emptyList()) }

    // Estado de las pestañas
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Pasajeros", "Conductores")

    fun recargarIncidencias() {
        scope.launch {
            try {
                val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.listarIncidencias()
                if (respuesta.isSuccessful) {
                    listaIncidencias = respuesta.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ReportesAdmin", "Error: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        recargarIncidencias()
    }

    // --- FILTRO MATEMÁTICO DE MATRIZ OPERATIVA ---
    val palabrasConductor = listOf("trancón", "desvío", "fallo", "pánico conductor")

    val incidenciasFiltradas = listaIncidencias.filter { incidencia ->
        val descNormalizada = incidencia.descripcion.lowercase()
        val esDeConductor = palabrasConductor.any { keyword -> descNormalizada.contains(keyword) }

        if (tabIndex == 0) {
            !esDeConductor // Pestaña Pasajeros (SOS y sugerencias)
        } else {
            esDeConductor // Pestaña Conductores
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reportes e Incidencias", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

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

        if (incidenciasFiltradas.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No hay reportes en esta categoría", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(incidenciasFiltradas) { incidencia ->
                    val esUrgente = incidencia.estado.lowercase() == "urgente"

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (esUrgente) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (esUrgente) androidx.compose.foundation.BorderStroke(1.dp, Color.Red) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (esUrgente) "🚨 ALERTA: ${incidencia.estado.uppercase()}" else "📋 Reporte Normal",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (esUrgente) Color.Red else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = incidencia.descripcion, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            val fecha = remember(incidencia.fecha_reporte) {
                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(incidencia.fecha_reporte))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "📅 $fecha", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text(text = "Usuario: ${incidencia.id_usuario.take(8)}...", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
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
fun PantallaGestionInformes(onVolver: () -> Unit) {
    // Pantalla de ejemplo, tu amigo la tenía conectada a Firebase,
    // aquí podemos dejarla lista para conectar a PostgreSQL después.
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Gestión de Informes", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Próximamente: Análisis de tiempos y alertas de conductores.", color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onVolver, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
            Text("Volver al Menú")
        }
    }
}
@Composable
fun MapaGeneralAdmin(onVolver: () -> Unit) {
    val context = LocalContext.current

    // Ahora guardamos la lista completa de buses (con su timestamp) desde PostgreSQL
    var flotaActiva by remember { mutableStateOf<List<com.ranger.popabus.network.BusActivo>>(emptyList()) }

    // Timer para forzar la actualización de los colores (gris/verde) cada 5 segundos
    var tiempoActual by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            tiempoActual = System.currentTimeMillis()
        }
    }

    // --- MOTOR: Lee la flota desde PostgreSQL ---
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val respuesta = com.ranger.popabus.network.RetrofitClient.apiService.obtenerBusesActivos()
                if (respuesta.isSuccessful && respuesta.body()?.status == "ok") {
                    flotaActiva = respuesta.body()!!.buses
                }
            } catch (e: Exception) {
                Log.e("TorreControl", "Error conectando con la flota: ${e.message}")
            }
            kotlinx.coroutines.delay(3000)
        }
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
            flotaActiva.forEach { bus ->
                // Lógica de conectividad: Si el último timestamp es mayor a 2 minutos, está desconectado
                val isDesconectado = (tiempoActual - bus.timestamp) > 120_000L

                Marker(
                    state = MarkerState(position = LatLng(bus.latitud, bus.longitud)),
                    title = "Vehículo: ${bus.id_bus}",
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
fun obtenerIconoBusGris(context: Context, vectorResId: Int): BitmapDescriptor? {
    MapsInitializer.initialize(context)
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

    val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = android.graphics.Paint()
    val matrix = android.graphics.ColorMatrix()
    matrix.setSaturation(0f)
    paint.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)

    canvas.saveLayer(null, paint)
    vectorDrawable.draw(canvas)
    canvas.restore()

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}