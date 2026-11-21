package io.github.sushaant.messq.ui.customer.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.state.customer.StudentUiState

@Composable
fun StudentSlotsTab(
    uiState: StudentUiState,
    onBookSlot: (SlotDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AI Crowd & Wait Forecast Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live AI Crowd & Wait Insight",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val pred = uiState.latestAiPrediction
                    if (pred != null) {
                        Text(
                            text = "${pred.crowd_level} CROWD • ~${pred.expected_wait_minutes} min wait",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        pred.ai_insight_summary?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Text(
                            text = "Checking live dining hall telemetry...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Available Meal Slots",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.availableSlots, key = { it.id }) { slot ->
            val spotsLeft = (slot.max_capacity - slot.current_booked_count).coerceAtLeast(0)
            val isFull = spotsLeft <= 0
            val ratio = (slot.current_booked_count.toFloat() / slot.max_capacity.coerceAtLeast(1)).coerceIn(0f, 1f)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = slot.meal_type,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${slot.start_time.take(5)} - ${slot.end_time.take(5)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isFull) "Slot Full" else "$spotsLeft spots left (${slot.current_booked_count}/${slot.max_capacity})",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { onBookSlot(slot) },
                            enabled = !isFull && !uiState.isLoading
                        ) {
                            Text(if (isFull) "Full" else "Book")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                    )
                }
            }
        }
    }
}