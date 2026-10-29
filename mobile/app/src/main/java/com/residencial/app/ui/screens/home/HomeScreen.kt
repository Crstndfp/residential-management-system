package com.residencial.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.residencial.app.data.model.ResidentProfile
import com.residencial.app.ui.navigation.Screen
import com.residencial.app.ui.theme.*

@Composable
fun HomeScreen(
    profile: ResidentProfile,
    onNavigate: (Screen) -> Unit,
    onTriggerPanic: () -> Unit
) {
    var showPanicConfirmDialog by BooleanState(false)
    var panicSentMessage by BooleanState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Resident Welcome Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = profile.houseNumber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = White
                        )
                        Text(
                            text = profile.fullName,
                            fontSize = 13.sp,
                            color = Slate400
                        )
                    }
                    // Solvency Semaphore Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (profile.isSolvent) Emerald950 else Rose950,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (profile.isSolvent) Emerald500 else Rose500
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (profile.isSolvent) Emerald400 else Rose400)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (profile.isSolvent) "SOLVENTE" else "EN MORA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profile.isSolvent) Emerald400 else Rose400
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Slate800)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Cuota Mensual", fontSize = 11.sp, color = Slate400)
                        Text("Q. ${profile.monthlyMaintenanceFee}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate200)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Saldo Pendiente", fontSize = 11.sp, color = Slate400)
                        Text(
                            "Q. ${profile.pendingBalance}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (profile.isSolvent) Emerald400 else Rose400
                        )
                    }
                }
            }
        }

        // Emergency SOS Panic Button Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BOTÓN DE PÁNICO RESIDENCIAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Rose400
                )
                Text(
                    text = "Presione en caso de emergencia médica o de seguridad",
                    fontSize = 11.sp,
                    color = Slate400,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                Button(
                    onClick = { showPanicConfirmDialog = true },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                    modifier = Modifier.size(110.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "SOS",
                            tint = White,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "SOS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = White
                        )
                    }
                }

                if (panicSentMessage) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡Alerta enviada a Garita y Seguridad!",
                        color = Emerald400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Action Shortcuts Grid
        Text(
            text = "ACCESOS RÁPIDOS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pass Shortcut
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.PASSES) }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Emerald400, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Pase QR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = White)
                    Text("Invitar visita", fontSize = 10.sp, color = Slate400)
                }
            }

            // Amenities Shortcut
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.AMENITIES) }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Purple500, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Reservar", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = White)
                    Text("Amenidades", fontSize = 10.sp, color = Slate400)
                }
            }

            // Maintenance Shortcut
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.TICKETS) }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = Amber400, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Reportar", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = White)
                    Text("Incidencia", fontSize = 10.sp, color = Slate400)
                }
            }
        }
    }

    // Panic Confirmation Modal Dialog
    if (showPanicConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPanicConfirmDialog = false },
            title = {
                Text("Confirmar Alerta de Pánico (SOS)", fontWeight = FontWeight.Bold, color = Rose500)
            },
            text = {
                Text(
                    "Esta acción notificará de forma urgente a los guardias en Garita indicando la ubicación de ${profile.houseNumber}. ¿Desea continuar?",
                    color = Slate200,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onTriggerPanic()
                        showPanicConfirmDialog = false
                        panicSentMessage = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Confirmar SOS", color = White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicConfirmDialog = false }) {
                    Text("Cancelar", color = Slate400)
                }
            },
            containerColor = Slate900,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Helper BooleanState
@Composable
private fun BooleanState(initial: Boolean): MutableState<Boolean> = remember { mutableStateOf(initial) }
