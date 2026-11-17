package com.example.supergymanglosajonforza

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

// Convierte un texto (por ejemplo "GYM-1") en la imagen de un código QR
fun generarQr(texto: String, tam: Int = 600): Bitmap {
    val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tam, tam)
    val bmp = Bitmap.createBitmap(tam, tam, Bitmap.Config.RGB_565)
    for (x in 0 until tam) {
        for (y in 0 until tam) {
            bmp.setPixel(x, y, if (matriz[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bmp
}

@Composable
fun PantallaQr(vm: GymViewModel) {
    val e = vm.estatus
    val color = when (e?.estatus) {
        "Al corriente" -> Color(0xFF2B8A3E)
        "Vencido" -> Color(0xFFC92A2A)
        else -> Color(0xFF868E96)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (e == null) {
            Text("Cargando...")
        } else {
            Text("Muestra este código en recepción", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Image(
                bitmap = remember(e.codigo) { generarQr(e.codigo) }.asImageBitmap(),
                contentDescription = "Código QR",
                modifier = Modifier.size(240.dp)
            )
            Text(e.codigo)
            Spacer(Modifier.height(16.dp))
            Text(e.estatus, color = color, style = MaterialTheme.typography.headlineSmall)
            Text("Último pago: ${e.ultimo_pago ?: "—"}")
            Text("Vence: ${e.vence ?: "—"}")
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { vm.cargarTodo() }) { Text("Actualizar") }
    }
}
