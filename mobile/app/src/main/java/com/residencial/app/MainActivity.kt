package com.residencial.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.residencial.app.data.repository.ResidentRepository
import com.residencial.app.ui.navigation.Screen
import com.residencial.app.ui.screens.amenities.AmenitiesScreen
import com.residencial.app.ui.screens.assembly.AssemblyScreen
import com.residencial.app.ui.screens.home.HomeScreen
import com.residencial.app.ui.screens.passes.PassesScreen
import com.residencial.app.ui.screens.tickets.TicketsScreen
import com.residencial.app.ui.theme.*

class MainActivity : ComponentActivity() {

    private val repository = ResidentRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResidencialTheme {
                MainAppContent(repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(repository: ResidentRepository) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    val profile by repository.resident.collectAsState()
    val passes by repository.passes.collectAsState()
    val amenities by repository.amenities.collectAsState()
    val myBookings by repository.myBookings.collectAsState()
    val tickets by repository.tickets.collectAsState()
    val motions by repository.motions.collectAsState()
    val quorum by repository.quorumPercentage.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Slate950,
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                contentColor = White
            ) {
                Screen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) Emerald400 else Slate400
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Emerald400 else Slate400
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Emerald950
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Slate950
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    profile = profile,
                    onNavigate = { currentScreen = it },
                    onTriggerPanic = { repository.triggerPanicButton() }
                )
                Screen.PASSES -> PassesScreen(
                    passes = passes,
                    onCreatePass = { name, plate -> repository.createVisitorPass(name, plate) }
                )
                Screen.AMENITIES -> AmenitiesScreen(
                    amenities = amenities,
                    myBookings = myBookings,
                    onBookAmenity = { am, date, slot -> repository.createBooking(am, date, slot) }
                )
                Screen.TICKETS -> TicketsScreen(
                    tickets = tickets,
                    onCreateTicket = { t, c, d, p -> repository.createTicket(t, c, d, p) }
                )
                Screen.ASSEMBLY -> AssemblyScreen(
                    quorumPercentage = quorum,
                    motions = motions,
                    onCastVote = { id, choice -> repository.castVote(id, choice) }
                )
            }
        }
    }
}
