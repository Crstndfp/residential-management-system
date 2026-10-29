package com.residencial.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val title: String, val icon: ImageVector) {
    HOME("Inicio", Icons.Default.Home),
    PASSES("Pases QR", Icons.Default.QrCode),
    AMENITIES("Amenidades", Icons.Default.CalendarToday),
    TICKETS("Fallas", Icons.Default.Build),
    ASSEMBLY("Asamblea", Icons.Default.HowToVote)
}
