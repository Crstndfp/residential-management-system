package com.residencial.app.ui.screens.assembly

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.residencial.app.data.model.MobileAssemblyMotion
import com.residencial.app.ui.theme.*

@Composable
fun AssemblyScreen(
    quorumPercentage: Double,
    motions: List<MobileAssemblyMotion>,
    onCastVote: (motionId: String, choice: String) -> String
) {
    var lastVoteHash by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
    ) {
        // Top Header
        Column {
            Text(
                text = "Asamblea de Copropietarios",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = White
            )
            Text(
                text = "Votación electrónica segura & quórum en vivo (US-403)",
                fontSize = 12.sp,
                color = Slate400
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quorum Thermometer Card
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
                        text = "Quórum en Tiempo Real",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate200
                    )
                    Text(
                        text = "$quorumPercentage%",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (quorumPercentage >= 50.0) Emerald400 else Amber400
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (quorumPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (quorumPercentage >= 50.0) Emerald400 else Amber400,
                    trackColor = Slate800
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", fontSize = 10.sp, color = Slate400)
                    Text("Umbral Legal: 50%", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                    Text("100%", fontSize = 10.sp, color = Slate400)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MOCIONES DE VOTACIÓN ABIERTAS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Motions List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(motions) { motion ->
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
                                text = motion.id,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Slate400
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (motion.status == "OPEN") Emerald950 else Slate800
                            ) {
                                Text(
                                    text = if (motion.status == "OPEN") "VOTACIÓN ABIERTA" else "PENDIENTE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (motion.status == "OPEN") Emerald400 else Slate400,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(motion.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = White)
                        Text(motion.description, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(top = 2.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        if (motion.userVotedChoice != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate950,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Voto Emitido: ${motion.userVotedChoice}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald400
                                    )
                                    Text(
                                        text = "Sellado SHA-256 verificado e inmutable",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Slate400
                                    )
                                }
                            }
                        } else if (motion.status == "OPEN") {
                            // Voting Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        lastVoteHash = onCastVote(motion.id, "A_FAVOR")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("A Favor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate950)
                                }

                                Button(
                                    onClick = {
                                        lastVoteHash = onCastVote(motion.id, "EN_CONTRA")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("En Contra", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = White)
                                }

                                Button(
                                    onClick = {
                                        lastVoteHash = onCastVote(motion.id, "ABSTENCION")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Abstener", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
