package com.ranger.popabus

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun testRolUsuarioConductorNormalization() {
        assertEquals(RolUsuario.CONDUCTOR, RolUsuario.desdeString("conductor"))
        assertEquals(RolUsuario.CONDUCTOR, RolUsuario.desdeString("CONDUCTOR"))
        assertEquals(RolUsuario.CONDUCTOR, RolUsuario.desdeString(" Conductor "))
        assertEquals(RolUsuario.CONDUCTOR, RolUsuario.desdeString("driver"))
        assertEquals(RolUsuario.CONDUCTOR, RolUsuario.desdeString("chofer"))
    }

    @Test
    fun testRolUsuarioAdministradorNormalization() {
        assertEquals(RolUsuario.ADMINISTRADOR, RolUsuario.desdeString("administrador"))
        assertEquals(RolUsuario.ADMINISTRADOR, RolUsuario.desdeString("ADMINISTRADOR"))
        assertEquals(RolUsuario.ADMINISTRADOR, RolUsuario.desdeString("admin"))
    }

    @Test
    fun testRolUsuarioPasajeroNormalization() {
        assertEquals(RolUsuario.PASAJERO, RolUsuario.desdeString("pasajero"))
        assertEquals(RolUsuario.PASAJERO, RolUsuario.desdeString("PASAJERO"))
        assertEquals(RolUsuario.PASAJERO, RolUsuario.desdeString(null))
        assertEquals(RolUsuario.PASAJERO, RolUsuario.desdeString(""))
        assertEquals(RolUsuario.PASAJERO, RolUsuario.desdeString("desconocido"))
    }
}
