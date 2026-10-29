package com.residencial.app.ui.screens.tickets

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.residencial.app.data.model.MobileTicket
import com.residencial.app.ui.theme.*

@Composable
fun TicketsScreen(
    tickets: List<MobileTicket>,
    onCreateTicket: (title: String, category: String, desc: String, priority: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Reporte de Fallas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = White
                )
                Text(
                    text = "Incidencias de mantenimiento e infraestructura",
                    fontSize = 12.sp,
                    color = Slate400
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Slate950, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reportar", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tickets List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tickets) { ticket ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ticket.folio,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (ticket.status) {
                                    "RESOLVED" -> Emerald950
                                    "IN_PROGRESS" -> Amber950
                                    else -> Blue950
                                }
                            ) {
                                Text(
                                    text = ticket.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (ticket.status) {
                                        "RESOLVED" -> Emerald400
                                        "IN_PROGRESS" -> Amber400
                                        else -> Blue400
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(ticket.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = White)
                        Text(ticket.description, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(top = 2.dp))

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Categoría: ${ticket.category}", fontSize = 10.sp, color = Slate400)
                            Text("Prioridad: ${ticket.priority}", fontSize = 10.sp, color = Rose400, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Create Ticket Dialog
    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("PLUMBING") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("MEDIUM") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Reportar Incidencia Técnica", fontWeight = FontWeight.Bold, color = White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la Falla") },
                        placeholder = { Text("Ej. Luminaria fundida frente a casa") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedBorderColor = Amber500
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción Detallada") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedBorderColor = Amber500
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("US-501: Se notificará a la administración para asignación de técnico.", fontSize = 11.sp, color = Slate400)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateTicket(title.trim(), category, description.trim(), priority)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500)
                ) {
                    Text("Enviar Reporte", color = Slate950, fontWeight = FontWeight.Bold)
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
}
