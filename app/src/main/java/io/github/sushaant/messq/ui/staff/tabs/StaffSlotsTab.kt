package io.github.sushaant.messq.ui.staff.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.sushaant.messq.data.model.SlotDto

@Composable
fun StaffSlotsTab(
    slots: List<SlotDto>,
    onUpdateSlot: (SlotDto, Int, String, String) -> Unit
) {
    var editingSlot by remember { mutableStateOf<SlotDto?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(slots, key = { it.id }) { slot ->
            val progress = (slot.current_booked_count.toFloat() / slot.max_capacity.coerceAtLeast(1)).coerceIn(0f, 1f)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${slot.meal_type} (${slot.start_time.take(5)} - ${slot.end_time.take(5)})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Occupancy: ${slot.current_booked_count} / ${slot.max_capacity}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = { editingSlot = slot }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Slot")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }
        }
    }

    editingSlot?.let { slot ->
        var capacityText by remember { mutableStateOf(slot.max_capacity.toString()) }
        var startText by remember { mutableStateOf(slot.start_time) }
        var endText by remember { mutableStateOf(slot.end_time) }

        AlertDialog(
            onDismissRequest = { editingSlot = null },
            title = { Text("Configure ${slot.meal_type} Slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { capacityText = it.filter(Char::isDigit) },
                        label = { Text("Max Capacity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startText,
                        onValueChange = { startText = it },
                        label = { Text("Start Time (HH:mm:ss)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endText,
                        onValueChange = { endText = it },
                        label = { Text("End Time (HH:mm:ss)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val cap = capacityText.toIntOrNull() ?: slot.max_capacity
                    onUpdateSlot(slot, cap, startText.trim(), endText.trim())
                    editingSlot = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSlot = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}