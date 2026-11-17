package com.example.supergymanglosajonforza

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GymApp()
            }
        }
    }
}

@Composable
fun GymApp(vm: GymViewModel = viewModel()) {
    val context = LocalContext.current

    // Muestra el Toast cuando el ViewModel deja un mensaje
    LaunchedEffect(vm.mensaje) {
        vm.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.mensaje = null
        }
    }

    if (vm.usuario == null) PantallaLogin(vm) else PantallaPrincipal(vm)
}

@Composable
fun PantallaLogin(vm: GymViewModel) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var modoRegistro by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(64.dp))
        Text("Mi Gym", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        if (modoRegistro) {
            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(email, { email = it }, label = { Text("Correo") }, singleLine = true,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (modoRegistro) vm.registro(nombre, email, password) else vm.login(email, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (modoRegistro) "Crear cuenta" else "Entrar") }

        TextButton(onClick = { modoRegistro = !modoRegistro }) {
            Text(if (modoRegistro) "Ya tengo cuenta" else "Crear cuenta nueva")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(vm: GymViewModel) {
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hola, ${vm.usuario?.nombre}") },
                actions = { TextButton(onClick = { vm.salir() }) { Text("Salir") } }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.FitnessCenter, null) }, label = { Text("Clases") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Mis reservas") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.QrCode, null) }, label = { Text("Mi QR") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> ListaClases(vm)
                1 -> ListaReservas(vm)
                else -> PantallaQr(vm)
            }
        }
    }
}

@Composable
fun ListaClases(vm: GymViewModel) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(vm.clases) { c ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(c.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("Instructor: ${c.instructor}")
                        Text(c.horario)
                        Text("Lugares: ${c.inscritos}/${c.cupo}")
                    }
                    Button(onClick = { vm.reservar(c.id) }) { Text("Reservar") }
                }
            }
        }
    }
}

@Composable
fun ListaReservas(vm: GymViewModel) {
    if (vm.reservas.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aún no tienes reservas")
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(vm.reservas) { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(r.clase, style = MaterialTheme.typography.titleMedium)
                        Text(r.horario)
                    }
                    OutlinedButton(onClick = { vm.cancelar(r.id) }) { Text("Cancelar") }
                }
            }
        }
    }
}
