package io.github.sushaant.messq.ui.staff.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.sushaant.messq.data.local.room.entity.MenuItemEntity

@Composable
fun StaffMenuTab(
    menuItems: List<MenuItemEntity>,
    onSaveMenuItem: (
        existingId: String?,
        name: String,
        category: String,
        description: String?
    ) -> Unit
) {
    var editingItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Saved Menus (${menuItems.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap a dish to update it; AI automatically estimates calories & macros.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (menuItems.isEmpty()) {
                item {
                    Text(
                        text = "No menu items saved yet. Tap + to add a dish.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(menuItems, key = { it.id }) { item ->
                    Card(
                        onClick = { editingItem = item },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                AssistChip(onClick = {}, label = { Text(item.category) })
                            }

                            item.description?.takeIf { it.isNotBlank() }?.let { desc ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = "${item.calories ?: 0} kcal",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${item.proteinGrams ?: 0.0}g Protein",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingItem = null
                showAddDialog = true
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Menu Item")
        }
    }

    if (showAddDialog || editingItem != null) {
        val current = editingItem
        var name by remember(current) { mutableStateOf(current?.name.orEmpty()) }
        var category by remember(current) { mutableStateOf(current?.category ?: "Lunch") }
        var description by remember(current) { mutableStateOf(current?.description.orEmpty()) }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingItem = null
            },
            title = {
                Text(if (current != null) "Update Menu Item" else "Add Menu Item")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Nutrition values are auto-estimated by AI on save.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Dish Name*") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (e.g., Breakfast, Lunch, Dinner)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Ingredients (optional)") },
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank() && category.isNotBlank(),
                    onClick = {
                        onSaveMenuItem(
                            current?.id,
                            name.trim(),
                            category.trim(),
                            description.trim().ifBlank { null }
                        )
                        showAddDialog = false
                        editingItem = null
                    }
                ) {
                    Text("Save with AI")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        editingItem = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}