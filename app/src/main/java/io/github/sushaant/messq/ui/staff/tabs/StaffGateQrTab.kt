package io.github.sushaant.messq.ui.staff.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.state.staff.StaffUiState
import io.github.sushaant.messq.utils.QrCodeGenerator

@Composable
fun StaffGateQrTab(
    uiState: StaffUiState,
    onSelectSlot: (SlotDto) -> Unit,
    onResetAuto: () -> Unit
) {
    val qrBitmap = remember(uiState.gateQrPayload) {
        uiState.gateQrPayload?.let { QrCodeGenerator.generateQrImageBitmap(it, 600) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mess Entry Check-In QR",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Students scan this QR to verify bookings, log walk-ins, and update live crowd levels.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "Gate Check-In QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.selectedSlotForQr?.let { slot ->
                        Text(
                            text = "${slot.meal_type} • ${slot.start_time.take(5)} - ${slot.end_time.take(5)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Checked-in / Booked: ${slot.current_booked_count} / ${slot.max_capacity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                        Text("No active slot configured for today")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Switch Slot Manually:", style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = onResetAuto) {
                Text("Auto-Sync Clock")
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.todaySlots, key = { it.id }) { slot ->
                FilterChip(
                    selected = uiState.selectedSlotForQr?.id == slot.id,
                    onClick = { onSelectSlot(slot) },
                    label = { Text("${slot.meal_type} ${slot.start_time.take(5)}") }
                )
            }
        }
    }
}