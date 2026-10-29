package com.residencial.app.ui.screens.amenities

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.residencial.app.data.model.MobileAmenity
import com.residencial.app.data.model.MobileBooking
import com.residencial.app.ui.theme.*

@Composable
fun AmenitiesScreen(
    amenities: List<MobileAmenity>,
    myBookings: List<MobileBooking>,
    onBookAmenity: (amenity: MobileAmenity, date: String, slot: String) -> Unit
) {
    var selectedAmenityForBooking by remember { mutableStateOf<MobileAmenity?>(null) }
    var selectedDate by remember { mutableStateOf("2026-11-10") }
    var selectedSlot by remember { mutableStateOf("14:00 - 18:00") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Amenidades & Reservas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = White
                )
                Text(
                    text = "Áreas comunes disponibles para copropietarios",
                    fontSize = 12.sp,
                    color = Slate400
                )
            }
        }

        // Amenities Catalog List
        items(amenities) { amenity ->
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
                        Text(
                            text = amenity.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = "Q. ${amenity.rentalCost}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Emerald400
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = amenity.description,
                        fontSize = 12.sp,
                        color = Slate400
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Capacidad: ${amenity.capacity} pers.", fontSize = 11.sp, color = Slate400)
                        Text("Horario: ${amenity.hours}", fontSize = 11.sp, color = Slate400)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { selectedAmenityForBooking = amenity },
                        colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reservar Fecha", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // My Bookings Section
        item {
            Text(
                text = "MIS RESERVACIONES ACTIVAS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(myBookings) { booking ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(booking.amenityName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = White)
                        Text("${booking.date} | ${booking.timeSlot}", fontSize = 11.sp, color = Slate400)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Emerald950
                    ) {
                        Text(
                            text = booking.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Booking Dialog
    selectedAmenityForBooking?.let { amenity ->
        AlertDialog(
            onDismissRequest = { selectedAmenityForBooking = null },
            title = { Text("Reservar ${amenity.name}", fontWeight = FontWeight.Bold, color = White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Costo de Reserva: Q. ${amenity.rentalCost} (Depósito: Q. ${amenity.depositCost})",
                        fontSize = 12.sp,
                        color = Emerald400,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("Fecha (YYYY-MM-DD)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedBorderColor = Purple500
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = selectedSlot,
                        onValueChange = { selectedSlot = it },
                        label = { Text("Franja Horaria") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedBorderColor = Purple500
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Cumplimiento US-302: Su reserva mantendrá la privacidad frente a otros condóminos.",
                        fontSize = 10.sp,
                        color = Slate400
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBookAmenity(amenity, selectedDate, selectedSlot)
                        selectedAmenityForBooking = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple500)
                ) {
                    Text("Confirmar Reserva", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAmenityForBooking = null }) {
                    Text("Cancelar", color = Slate400)
                }
            },
            containerColor = Slate900,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
