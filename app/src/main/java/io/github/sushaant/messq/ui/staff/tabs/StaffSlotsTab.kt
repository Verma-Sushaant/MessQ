package io.github.sushaant.messq.ui.staff.tabs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.sushaant.messq.data.model.SlotDto
import java.time.Duration
import java.time.LocalTime
import java.util.Locale

private enum class SlotWindowStatus(val label: String) {
    PAST("Past"),
    CURRENT("Current"),
    UPCOMING("Upcoming")
}

@RequiresApi(Build.VERSION_CODES.O)
private fun classifySlot(slot: SlotDto, now: LocalTime): SlotWindowStatus {
    val start = runCatching { LocalTime.parse(slot.start_time) }.getOrNull()
    val end = runCatching { LocalTime.parse(slot.end_time) }.getOrNull()
    return when {
        start != null && end != null && !now.isBefore(start) && !now.isAfter(end) -> SlotWindowStatus.CURRENT
        end != null && now.isAfter(end) -> SlotWindowStatus.PAST
        else -> SlotWindowStatus.UPCOMING
    }
}

private fun formatHhMm(hour: Int, minute: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StaffSlotsTab(
    slots: List<SlotDto>,
    walkInCountsBySlotTime: Map<String, Int> = emptyMap(),
    onUpdateSlot: (slot: SlotDto, maxCap: Int, startTime: String, endTime: String) -> Unit,
    onGenerateHourlySlots: (startHhMm: String, endHhMm: String, maxCapacity: Int, mealType: String?) -> Unit
) {
    var editingSlot by remember { mutableStateOf<SlotDto?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val now = remember(slots) { LocalTime.now() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Today's Slots (${slots.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pick start & end time and set max capacity — 1-hour slots are generated automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (slots.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "No active slots for today",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Button(onClick = { showCreateDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate 1-Hour Slots")
                            }
                        }
                    }
                }
            } else {
                items(slots, key = { it.id }) { slot ->
                    val status = classifySlot(slot, now)
                    val progress = (slot.current_booked_count.toFloat() / slot.max_capacity.coerceAtLeast(1))
                        .coerceIn(0f, 1f)
                    val rangeKey = "${slot.start_time.take(5)}-${slot.end_time.take(5)}"
                    val walkIns = walkInCountsBySlotTime[slot.start_time]
                        ?: walkInCountsBySlotTime[rangeKey]
                        ?: 0

                    val badgeColor = when (status) {
                        SlotWindowStatus.CURRENT -> MaterialTheme.colorScheme.primaryContainer
                        SlotWindowStatus.UPCOMING -> MaterialTheme.colorScheme.secondaryContainer
                        SlotWindowStatus.PAST -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${slot.start_time.take(5)} - ${slot.end_time.take(5)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = badgeColor,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = status.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = slot.meal_type,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = { editingSlot = slot }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Slot")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Booked: ${slot.current_booked_count} / ${slot.max_capacity}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            if (status == SlotWindowStatus.PAST) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Walk-ins: $walkIns",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
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
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Generate 1-Hour Slots")
        }
    }

    if (showCreateDialog) {
        GenerateHourlySlotsDialog(
            onDismiss = { showCreateDialog = false },
            onGenerate = onGenerateHourlySlots
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun GenerateHourlySlotsDialog(
    onDismiss: () -> Unit,
    onGenerate: (startHhMm: String, endHhMm: String, maxCapacity: Int, mealType: String?) -> Unit
) {
    val initialNow = remember { LocalTime.now() }
    var startHour by remember { mutableIntStateOf(initialNow.hour) }
    var startMinute by remember { mutableIntStateOf(0) }

    val defaultEnd = remember(startHour) {
        val candidate = startHour + 2
        if (candidate <= 23) candidate else 23
    }
    var endHour by remember { mutableIntStateOf(defaultEnd) }
    var endMinute by remember { mutableIntStateOf(0) }

    var pickingTarget by remember { mutableStateOf<String?>(null) } // "START" or "END"
    var capacityText by remember { mutableStateOf("100") }
    var optionalMealType by remember { mutableStateOf("") }

    val startHhMm = formatHhMm(startHour, startMinute)
    val endHhMm = formatHhMm(endHour, endMinute)

    val isValid = remember(startHour, startMinute, endHour, endMinute, capacityText) {
        val cap = capacityText.toIntOrNull() ?: 0
        val s = LocalTime.of(startHour, startMinute)
        val e = LocalTime.of(endHour, endMinute)
        e.isAfter(s) && Duration.between(s, e).toMinutes() >= 60 && cap > 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate 1-Hour Slots", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { pickingTarget = "START" },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start: $startHhMm", maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { pickingTarget = "END" },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("End: $endHhMm", maxLines = 1)
                    }
                }

                if (!isValid) {
                    Text(
                        text = "Select an end time at least 1 hour after start time and capacity > 0.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it.filter(Char::isDigit) },
                    label = { Text("Max Capacity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = optionalMealType,
                    onValueChange = { optionalMealType = it },
                    label = { Text("Meal Label (optional — auto if blank)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val cap = capacityText.toIntOrNull() ?: 100
                    onGenerate(startHhMm, endHhMm, cap, optionalMealType.trim().ifBlank { null })
                    onDismiss()
                }
            ) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (pickingTarget != null) {
        val isStart = pickingTarget == "START"
        val pickerState = rememberTimePickerState(
            initialHour = if (isStart) startHour else endHour,
            initialMinute = if (isStart) startMinute else endMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { pickingTarget = null },
            title = { Text(if (isStart) "Select Start Time" else "Select End Time") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = pickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (isStart) {
                        startHour = pickerState.hour
                        startMinute = pickerState.minute
                    } else {
                        endHour = pickerState.hour
                        endMinute = pickerState.minute
                    }
                    pickingTarget = null
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}