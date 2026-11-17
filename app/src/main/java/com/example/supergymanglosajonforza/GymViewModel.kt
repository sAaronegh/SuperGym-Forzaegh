package com.example.supergymanglosajonforza

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response

class GymViewModel : ViewModel() {
    private val api = RetrofitClient.api

    var usuario by mutableStateOf<Usuario?>(null)
    var clases by mutableStateOf<List<Clase>>(emptyList())
    var reservas by mutableStateOf<List<Reserva>>(emptyList())
    var estatus by mutableStateOf<Estatus?>(null)

    // Cada vez que cambia, la pantalla muestra un Toast y lo limpia
    var mensaje by mutableStateOf<String?>(null)

    // Hace la llamada y, si falla, deja el error en "mensaje" para el Toast
    private suspend fun <T> llamar(call: suspend () -> Response<T>): T? {
        try {
            val r = call()
            if (r.isSuccessful) return r.body()
            val texto = r.errorBody()?.string()
            val detalle = runCatching { Gson().fromJson(texto, Detalle::class.java)?.detail }.getOrNull()
            mensaje = detalle ?: "Error ${r.code()}"
        } catch (e: Exception) {
            mensaje = "No se pudo conectar con el servidor"
        }
        return null
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        llamar { api.login(LoginReq(email, password)) }?.let {
            usuario = it
            mensaje = "Bienvenido, ${it.nombre}"
            cargarTodo()
        }
    }

    fun registro(nombre: String, email: String, password: String) = viewModelScope.launch {
        llamar { api.registro(RegistroReq(nombre, email, password)) }?.let {
            usuario = it
            mensaje = "Cuenta creada"
            cargarTodo()
        }
    }

    fun cargarTodo() = viewModelScope.launch {
        llamar { api.clases() }?.let { clases = it }
        usuario?.let { u ->
            llamar { api.misReservas(u.id) }?.let { reservas = it }
            llamar { api.estatus(u.id) }?.let { estatus = it }
        }
    }

    fun reservar(claseId: Int) = viewModelScope.launch {
        val u = usuario ?: return@launch
        llamar { api.reservar(ReservaReq(u.id, claseId)) }?.let {
            mensaje = it.mensaje
            cargarTodo()
        }
    }

    fun cancelar(reservaId: Int) = viewModelScope.launch {
        llamar { api.cancelar(reservaId) }?.let {
            mensaje = it.mensaje
            cargarTodo()
        }
    }

    fun salir() {
        usuario = null
        clases = emptyList()
        reservas = emptyList()
        estatus = null
    }
}
