package com.residencial.app.ui.screens.passes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.residencial.app.data.model.QrPass
import com.residencial.app.ui.theme.*

@Composable
fun PassesScreen(
    passes: List<QrPass>,
    onCreatePass: (name: String, plate: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPassForQr by remember { mutableStateOf<QrPass?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pases de Visita QR",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = White
                )
                Text(
                    text = "Generación de pases temporales para garita",
                    fontSize = 12.sp,
                    color = Slate400
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Slate950, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nuevo Pase", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Passes List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(passes) { pass ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPassForQr = pass }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pass.expectedPlate,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Amber400
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Emerald950
                                ) {
                                    Text(
                                        text = "VÁLIDO 24H",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald400,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pass.visitorName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = White
                            )
                            Text(
                                text = "Vence: ${pass.validUntil}",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Ver QR",
                            tint = Emerald400,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }

    // Create Pass Dialog
    if (showCreateDialog) {
        var visitorName by remember { mutableStateOf("") }
        var plate by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Generar Nuevo Pase QR", fontWeight = FontWeight.Bold, color = White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = visitorName,
                        onValueChange = { visitorName = it },
                        label = { Text("Nombre del Visitante") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedBorderColor = Emerald500
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = plate,
                        onValueChange = { plate = it.uppercase() },
                        label = { Text("Placa del Vehículo (Opcional)") },
                        placeholder = { Text("P-123XYZ") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedBorderColor = Emerald500
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "El pase tendrá una validez de 24 horas y puede compartirse por mensajería instantánea.",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (visitorName.isNotBlank()) {
                            onCreatePass(visitorName.trim(), plate.trim().ifEmpty { "SIN-PLACA" })
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Text("Generar Pase", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = Slate400)
                }
            },
            containerColor = Slate900,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // QR Pass Display Modal
    selectedPassForQr?.let { pass ->
        AlertDialog(
            onDismissRequest = { selectedPassForQr = null },
            title = {
                Text(
                    text = "Pase de Acceso Garita",
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Simulated QR Grid Visualizer
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = Slate950,
                                modifier = Modifier.size(130.dp)
                            )
                            Text(
                                text = pass.expectedPlate,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Slate950
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(pass.visitorName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = White)
                    Text("Destino: ${pass.houseNumber}", fontSize = 12.sp, color = Slate400)
                    Text("Válido hasta: ${pass.validUntil}", fontSize = 11.sp, color = Emerald400, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Token SHA-256 verificado por Garita",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedPassForQr = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Slate900,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
