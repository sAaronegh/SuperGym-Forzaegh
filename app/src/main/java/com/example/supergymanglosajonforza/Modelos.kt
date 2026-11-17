package com.example.supergymanglosajonforza

data class Usuario(val id: Int, val nombre: String, val email: String)

data class Clase(
    val id: Int,
    val nombre: String,
    val instructor: String,
    val horario: String,
    val cupo: Int,
    val inscritos: Int
)

data class Reserva(val id: Int, val clase_id: Int, val clase: String, val horario: String)

data class LoginReq(val email: String, val password: String)
data class RegistroReq(val nombre: String, val email: String, val password: String)
data class ReservaReq(val usuario_id: Int, val clase_id: Int)

data class Estatus(
    val id: Int,
    val nombre: String,
    val codigo: String,
    val estatus: String,
    val ultimo_pago: String?,
    val monto: Double?,
    val vence: String?
)

data class Mensaje(val mensaje: String)
data class Detalle(val detail: String?)
