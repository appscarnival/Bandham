package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.viewmodel.BandhamViewModel

// ==========================================
// 1. GMAIL SYNC & BACKUP WORKSPACE SCREEN
// ==========================================
@Composable
fun GmailSyncAndBackupView(
    viewModel: BandhamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val googleConnected by viewModel.googleAccountConnected.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncingGmail.collectAsStateWithLifecycle()
    val gmailContacts by viewModel.gmailContacts.collectAsStateWithLifecycle()
    val backupHistory by viewModel.backupHistory.collectAsStateWithLifecycle()
    val autoBackup by viewModel.autoDailyBackup.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("contacts") } // "contacts" or "backup"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Headers
        TabRow(
            selectedTabIndex = if (activeSubTab == "contacts") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeSubTab == "contacts",
                onClick = { activeSubTab = "contacts" },
                text = { Text("Gmail Contact Sync", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ContactPhone, contentDescription = "Contacts") }
            )
            Tab(
                selected = activeSubTab == "backup",
                onClick = { activeSubTab = "backup" },
                text = { Text("Vault Backup & Restore", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CloudUpload, contentDescription = "Backup") }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeSubTab == "contacts") {
                item {
                    // Connection Status Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (googleConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Google",
                                        tint = if (googleConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (googleConnected) "Google Account Connected" else "Connect Google Account",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = if (googleConnected) "msstejappa@gmail.com" else "Sync contacts & store secure backups",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Switch(
                                    checked = googleConnected,
                                    onCheckedChange = {
                                        viewModel.toggleGoogleConnected()
                                        val msg = if (!googleConnected) "Connected to Google Account!" else "Disconnected Google Account."
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

                if (!googleConnected) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.SyncDisabled,
                                    contentDescription = "Sync Off",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Google Sync Inactive",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Enable the Google Account connection above to download secure cloud contact lists, map duplicates, and activate automatic backup logs on this device.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Imported Gmail Contacts (${gmailContacts.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                TextButton(onClick = { viewModel.importGmailContacts() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Sync Now", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Force Sync")
                                }
                            }
                        }
                    }

                    if (gmailContacts.isEmpty() && !isSyncing) {
                        item {
                            Text("Loading Gmail contacts list...", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        items(gmailContacts) { contact ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (contact.isDuplicate) Color(0xFFFFF9E6) else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    contact.name.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(contact.name, fontWeight = FontWeight.Bold)
                                                Text(contact.phone.ifEmpty { contact.email }, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                            }
                                        }

                                        if (contact.isDuplicate) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("Duplicate detected", color = Color(0xFFD84315), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (contact.isSynced) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Synced", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    if (contact.isDuplicate) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "This contact's records match an existing member in your Bandham local tree list.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.keepSeparateContact(contact.id)
                                                    Toast.makeText(context, "Resolved: Kept Separate.", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Keep Separate", fontSize = 10.sp)
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Button(
                                                onClick = {
                                                    viewModel.mergeGmailContact(contact.id)
                                                    Toast.makeText(context, "Merged updates into Bandham vault profile of ${contact.name}!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("Merge & Link Profiles", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    } else if (!contact.isSynced) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                            Button(
                                                onClick = {
                                                    viewModel.mergeGmailContact(contact.id)
                                                    Toast.makeText(context, "Imported ${contact.name}!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.height(32.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Import Contact", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // BACKUP TAB
                item {
                    // Daily Auto Toggle Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Update,
                                        contentDescription = "Auto Backup",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Automatic Daily Backup", fontWeight = FontWeight.Bold)
                                        Text("Daily incremental backups to Google Drive", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                Switch(checked = autoBackup, onCheckedChange = { viewModel.setAutoDailyBackup(it) })
                            }
                        }
                    }
                }

                item {
                    // Manual Backup Action Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Google Drive Cloud Vault",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Last backup completed: 13 June 2026, 10:45 PM\nStatus: ✓ Synced",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.addNewBackup(isAuto = false)
                                    Toast.makeText(context, "Full App Vault Backup uploaded to Drive!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Backup, contentDescription = "Upload", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Trigger Manual Backup Now", color = Color.White)
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Stored Backup Versions",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(backupHistory) { version ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (version.isAuto) Icons.Default.Schedule else Icons.Default.Fingerprint,
                                        contentDescription = "Backup Type",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (version.isAuto) "Automated System Log" else "Manual App Export",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text("Date: ${version.timestamp}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
                                Text("Details: ${version.recordsCount} items • ${version.sizeKb} KB", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.restoreFromBackup(version.id)
                                        Toast.makeText(context, "Full system state of ${version.timestamp} restored successfully!", Toast.LENGTH_LONG).show()
                                    },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "Restore", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Restore", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. GIFT SUGGESTION ASSISTANT COMPONENT
// ==========================================
@Composable
fun GiftAssistantDialog(
    occasionType: String,
    relationship: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedBudget by remember { mutableStateOf("₹1,000") }
    var selectedCategory by remember { mutableStateOf("Personalized Gifts") }
    var ageString by remember { mutableStateOf("28") }
    var genderString by remember { mutableStateOf("Unspecified") }

    val budgets = listOf("₹500", "₹1,000", "₹2,500", "₹5,000", "Custom")
    val categories = listOf("Personalized Gifts", "Electronics", "Fashion", "Books", "Home Decor", "Toys", "Experiences", "Flowers", "Gift Cards")

    val mockGifts = mapOf(
        "Personalized Gifts" to listOf(
            Triple("Engraved Bamboo Wooden Desk Clock", "₹1,250", "https://example.com/clock"),
            Triple("Custom Brass Nameplate with Family Tree Symbol", "₹2,200", "https://example.com/nameplate"),
            Triple("Classic Personalized Leather Passport Cover Set", "₹890", "https://example.com/passport")
        ),
        "Electronics" to listOf(
            Triple("Smart Bluetooth Relational LED Occasion Lamp", "₹2,499", "https://example.com/lamp"),
            Triple("High-Precision Digital Smart Fitness Ring", "₹4,999", "https://example.com/ring"),
            Triple("Waterproof Portable Travel Wireless Speaker", "₹1,800", "https://example.com/speaker")
        ),
        "Fashion" to listOf(
            Triple("Premium Pure Handwoven Cotton Silk Shawl", "₹1,500", "https://example.com/shawl"),
            Triple("Bespoke Handcrafted Real Leather Laptop Satchel", "₹3,200", "https://example.com/bag")
        ),
        "Books" to listOf(
            Triple("The Great Indian Epic Masterpieces Trilogy Set", "₹1,100", "https://example.com/books"),
            Triple("Hardcover Premium Family Recipe Log Book", "₹650", "https://example.com/recipes")
        ),
        "Home Decor" to listOf(
            Triple("Aura Terracotta Ultrasonic Oil Diffuser Aura", "₹1,450", "https://example.com/diffuser"),
            Triple("Hand-glazed Handcrafted Stoneware Planter Pots Set", "₹950", "https://example.com/planter")
        ),
        "Toys" to listOf(
            Triple("Educational Multi-Generational Board Game Set", "₹1,200", "https://example.com/toys")
        ),
        "Experiences" to listOf(
            Triple("All-inclusive Organic Spice Plantation Couple Tour", "₹4,500", "https://example.com/tour"),
            Triple("Relaxing Deep-Tissue Ayurvedic Spa Voucher", "₹2,500", "https://example.com/spa")
        ),
        "Flowers" to listOf(
            Triple("Luxe Red Rose Velvet Box arrangement", "₹999", "https://example.com/flowers")
        ),
        "Gift Cards" to listOf(
            Triple("Amazon Premium E-Gift Shopping Card", "₹2,000", "https://example.com/amazon"),
            Triple("Flipkart Shopping Festival Gift Voucher", "₹1,500", "https://example.com/flipkart")
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎁", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Smart Gift Assistant", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                            Text("Tailored suggestions for $relationship's $occasionType", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Input Parameters Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageString,
                        onValueChange = { ageString = it },
                        label = { Text("Age (Years)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = genderString,
                        onValueChange = { genderString = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Budgets Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Budget Filter:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(budgets) { bud ->
                            FilterChip(
                                selected = selectedBudget == bud,
                                onClick = { selectedBudget = bud },
                                label = { Text(bud, fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // Categories Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Suggested Category:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Suggestion Cards
                val listForCategory = mockGifts[selectedCategory] ?: emptyList()
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "AI-Generated Smart Recommendations:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    if (listForCategory.isEmpty()) {
                        Text("No specific item mapped. Select an alternate category.", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall)
                    }

                    listForCategory.forEach { (giftName, price, mockLink) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🎁", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(giftName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("Category: $selectedCategory", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }

                                    Text(price, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            Toast.makeText(context, "Saved to Buy Later wishlist logs!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Buy Later", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            Toast.makeText(context, "Marked as purchased secure log!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircleOutline, contentDescription = "Purchased", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "Hey! Check out this gift suggestion for $relationship's $occasionType: $giftName ($price) on Amazon/Flipkart!")
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, null)
                                            context.startActivity(shareIntent)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.in/s?k=" + Uri.encode(giftName)))
                                            context.startActivity(browserIntent)
                                        },
                                        modifier = Modifier.height(28.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("Buy Link", fontSize = 10.sp, color = Color.White)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(Icons.Default.ArrowOutward, contentDescription = "Amazon", modifier = Modifier.size(10.dp), tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Affiliate integration enabled: clicking 'Buy Link' searches verified catalogs on Amazon and Flipkart securely.",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. EVENT GUEST TRACKER SCREEN (KANBAN PIPELINE)
// ==========================================
@Composable
fun EventGuestTrackerWorkspace(
    viewModel: BandhamViewModel,
    modifier: Modifier = Modifier,
    initialEventId: String? = null
) {
    val context = LocalContext.current
    val events by viewModel.eventsList.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()

    var selectedEventIndex by remember(events) {
        val idx = events.indexOfFirst { it.id == initialEventId }
        mutableStateOf(if (idx >= 0) idx else 0)
    }
    var showAddGuestDialog by remember { mutableStateOf(false) }

    if (events.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val activeEvent = events.getOrNull(selectedEventIndex) ?: events.first()

    // Dashboard calculations
    val totalGuests = activeEvent.guests.size
    val invitedCount = activeEvent.guests.count { it.status != "YET_TO_INVITE" }
    val confirmedCount = activeEvent.guests.count { it.status == "CONFIRMED" || it.status == "ATTENDED" }
    val pendingCount = activeEvent.guests.count { it.status == "YET_TO_INVITE" || it.status == "CALLED" || it.status == "INVITATION_SENT" }
    val attendedCount = activeEvent.guests.count { it.status == "ATTENDED" }

    // Automation notification calculations
    val yetToInviteCount = activeEvent.guests.count { it.status == "YET_TO_INVITE" }
    val missingLocationCount = activeEvent.guests.count { it.status == "CONFIRMED" && it.status != "LOCATION_SHARED" } // Simplified alert logic
    val confirmationPendingCount = activeEvent.guests.count { it.status == "INVITATION_SENT" || it.status == "CALLED" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Event Dropdown Switcher Toolbar
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Event Workspaces Central",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(events.size) { index ->
                        val ev = events[index]
                        val isSelected = index == selectedEventIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedEventIndex = index },
                            label = { Text(ev.title, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (ev.type.lowercase().contains("wedding")) Icons.Default.Favorite else Icons.Default.Celebration,
                                    contentDescription = "Event",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Automation Reminders Bar
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (yetToInviteCount > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Alert", tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "\"$yetToInviteCount guests are yet to be invited.\"",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }

                    if (confirmationPendingCount > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = BorderStroke(1.dp, Color(0xFF81C784)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = "Alert", tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "\"$confirmationPendingCount confirmations pending.\"",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }

            // Dashboard Grid Cards
            item {
                Text("Dashboard Live Metrics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardMetricsItem("Total Guests", "$totalGuests", MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
                    CardMetricsItem("Invited", "$invitedCount", Color(0xFFE3F2FD), Modifier.weight(1f))
                    CardMetricsItem("Confirmed", "$confirmedCount", Color(0xFFE8F5E9), Modifier.weight(1f))
                    CardMetricsItem("Pending", "$pendingCount", Color(0xFFFFFDE7), Modifier.weight(1f))
                }
            }

            // Kanban Header Row with Add Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Interactive Status Pipeline",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { showAddGuestDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Guest")
                    }
                }
            }

            // Horizontally Scrollable Kanban Columns Board!
            item {
                val statuses = listOf(
                    Triple("YET_TO_INVITE", "Yet to Invite", Color(0xFF9E9E9E)),
                    Triple("CALLED", "Called", Color(0xFFFF9800)),
                    Triple("INVITATION_SENT", "Invitation Sent", Color(0xFF2196F3)),
                    Triple("LOCATION_SHARED", "Location Shared", Color(0xFF9C27B0)),
                    Triple("CONFIRMED", "Confirmed", Color(0xFF4CAF50)),
                    Triple("ATTENDED", "Attended", Color(0xFF009688))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    statuses.forEach { (statusId, statusLabel, color) ->
                        val guestsInStatus = activeEvent.guests.filter { it.status == statusId }

                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .fillMaxHeight(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Column Header
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(statusLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Badge(containerColor = color.copy(alpha = 0.2f), contentColor = color) {
                                        Text("${guestsInStatus.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

                                if (guestsInStatus.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No guests", color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        guestsInStatus.forEach { guest ->
                                            KanbanGuestCard(
                                                guest = guest,
                                                onStatusChange = { targetStatus ->
                                                    viewModel.updateGuestStatus(activeEvent.id, guest.id, targetStatus)
                                                    Toast.makeText(context, "Moved ${guest.name} to $targetStatus", Toast.LENGTH_SHORT).show()
                                                },
                                                onRemove = {
                                                    viewModel.removeGuestFromEvent(activeEvent.id, guest.id)
                                                    Toast.makeText(context, "Removed ${guest.name}", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGuestDialog) {
        var gName by remember { mutableStateOf("") }
        var gRel by remember { mutableStateOf("") }
        var gPhone by remember { mutableStateOf("") }
        var gGroup by remember { mutableStateOf("") }
        var gNotes by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddGuestDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add New Guest to Event", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = gName,
                        onValueChange = { gName = it },
                        label = { Text("Guest Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = gRel,
                        onValueChange = { gRel = it },
                        label = { Text("Relationship (e.g. Cousin, Friend)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = gPhone,
                        onValueChange = { gPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = gGroup,
                        onValueChange = { gGroup = it },
                        label = { Text("Family Group Tag (e.g. maternal side)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = gNotes,
                        onValueChange = { gNotes = it },
                        label = { Text("Private Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddGuestDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (gName.trim().isEmpty()) {
                                    Toast.makeText(context, "Name are mandatory!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.addGuestToEvent(
                                    activeEvent.id,
                                    GuestEntry(
                                        id = "g_user_" + System.currentTimeMillis(),
                                        name = gName.trim(),
                                        relationship = gRel.trim(),
                                        phone = gPhone.trim(),
                                        familyGroup = gGroup.trim(),
                                        notes = gNotes.trim(),
                                        status = "YET_TO_INVITE"
                                    )
                                )
                                showAddGuestDialog = false
                            }
                        ) {
                            Text("Add Guest", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanGuestCard(
    guest: GuestEntry,
    onStatusChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var expandedDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { expandedDetails = !expandedDetails }
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(guest.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(guest.relationship, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                    }
                    Icon(
                        imageVector = if (expandedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (expandedDetails) {
                Spacer(modifier = Modifier.height(6.dp))
                if (guest.phone.isNotEmpty()) {
                    Text("Phone: ${guest.phone}", fontSize = 10.sp)
                }
                if (guest.familyGroup.isNotEmpty()) {
                    Text("Group: ${guest.familyGroup}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                if (guest.notes.isNotEmpty()) {
                    Text("Notes: ${guest.notes}", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Communication Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                if (guest.phone.isNotEmpty()) {
                                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${guest.phone}"))
                                    context.startActivity(callIntent)
                                } else {
                                    Toast.makeText(context, "No phone logged", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Dial", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        }

                        IconButton(
                            onClick = {
                                val url = "https://api.whatsapp.com/send?phone=" + guest.phone + "&text=" + Uri.encode("Hi ${guest.name}! You are cordially invited to our upcoming family gathering celebration! Please find details on Bandham.")
                                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(i)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(12.dp))
                        }

                        IconButton(
                            onClick = {
                                val textIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${guest.phone}")).apply {
                                    putExtra("sms_body", "Hi ${guest.name}, here is the family invitation!")
                                }
                                context.startActivity(textIntent)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = "SMS", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                        }
                    }

                    // Move Status controls
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val nextStatus = when (guest.status) {
                            "YET_TO_INVITE" -> "CALLED"
                            "CALLED" -> "INVITATION_SENT"
                            "INVITATION_SENT" -> "LOCATION_SHARED"
                            "LOCATION_SHARED" -> "CONFIRMED"
                            "CONFIRMED" -> "ATTENDED"
                            else -> null
                        }

                        val prevStatus = when (guest.status) {
                            "CALLED" -> "YET_TO_INVITE"
                            "INVITATION_SENT" -> "CALLED"
                            "LOCATION_SHARED" -> "INVITATION_SENT"
                            "CONFIRMED" -> "LOCATION_SHARED"
                            "ATTENDED" -> "CONFIRMED"
                            else -> null
                        }

                        if (prevStatus != null) {
                            IconButton(
                                onClick = { onStatusChange(prevStatus) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(12.dp))
                            }
                        }

                        if (nextStatus != null) {
                            IconButton(
                                onClick = { onStatusChange(nextStatus) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next", modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. COLLABORATIVE VACATION PLANNER SCREEN
// ==========================================
@Composable
fun VacationPlannerWorkspace(
    viewModel: BandhamViewModel,
    modifier: Modifier = Modifier,
    initialTripId: String? = null
) {
    val context = LocalContext.current
    val trips by viewModel.vacationTrips.collectAsStateWithLifecycle()

    var activeTripIndex by remember(trips) {
        val idx = trips.indexOfFirst { it.id == initialTripId }
        mutableStateOf(if (idx >= 0) idx else 0)
    }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showAddChecklistDialog by remember { mutableStateOf(false) }

    if (trips.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val trip = trips.getOrNull(activeTripIndex) ?: trips.first()

    // 1-0 Binary checklist process calculation
    val totalChecklist = trip.checklist.size
    val completedChecklist = trip.checklist.count { it.isCompleted }
    val progressPercentage = if (totalChecklist > 0) ((completedChecklist.toFloat() / totalChecklist) * 100).toInt() else 0

    // Shared Expense Settlement Calculation
    // Model: paid totals of each user
    val userPaidTotals = mutableMapOf<String, Double>()
    trip.members.forEach { userPaidTotals[it.name] = 0.0 }

    trip.expenses.forEach { exp ->
        val currentPaid = userPaidTotals[exp.payer] ?: 0.0
        userPaidTotals[exp.payer] = currentPaid + exp.amount
    }

    val totalSpent = trip.expenses.sumOf { it.amount }
    val userCount = trip.members.size
    val shareVal = if (userCount > 0) totalSpent / userCount else 0.0

    // Settlement Suggestion logic
    val outstanding = mutableMapOf<String, Double>()
    trip.members.forEach { m ->
        val paid = userPaidTotals[m.name] ?: 0.0
        outstanding[m.name] = paid - shareVal
    }

    // suggest settlement actions
    val debtors = outstanding.filter { it.value < 0 }.map { it.key to -it.value }.toMutableList()
    val creditors = outstanding.filter { it.value > 0 }.map { it.key to it.value }.toMutableList()

    val settlementSuggestions = mutableListOf<String>()
    var dIdx = 0
    var cIdx = 0
    while (dIdx < debtors.size && cIdx < creditors.size) {
        val (debName, debAmt) = debtors[dIdx]
        val (credName, credAmt) = creditors[cIdx]

        val settleAmt = minOf(debAmt, credAmt)
        settlementSuggestions.add("• $debName pays $credName ₹${String.format("%.0f", settleAmt)}")

        if (debAmt > credAmt) {
            debtors[dIdx] = debName to (debAmt - settleAmt)
            cIdx++
        } else if (debAmt < credAmt) {
            creditors[cIdx] = credName to (credAmt - settleAmt)
            dIdx++
        } else {
            dIdx++
            cIdx++
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dropdown Switcher for Vacation
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Vacation & Tour Group Planners",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(trips.size) { index ->
                        val t = trips[index]
                        val isSelected = index == activeTripIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeTripIndex = index },
                            label = { Text(t.title, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.FlightTakeoff, contentDescription = "Vacation", modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Role: Organizer",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Collaborative", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = trip.destination,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = trip.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Planning Pipeline Process (Binary check)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Trip Preparation Progress", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                            Text("$progressPercentage%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { completedChecklist.toFloat() / totalChecklist.coerceAtLeast(1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Grid of stage checklist indicators
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            trip.checklist.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleTripChecklistItem(trip.id, item.id) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Status",
                                            tint = if (item.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (item.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (item.isCompleted) "1" else "0",
                                            color = if (item.isCompleted) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showAddChecklistDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Custom Task Stage")
                        }
                    }
                }
            }

            // Shared Expense Tracker
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Shared Expense Tracker", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                            Button(
                                onClick = { showExpenseDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Log Cost", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Table Headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                .padding(8.dp)
                        ) {
                            Text("Member", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("Paid", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("Share", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        }

                        // Users Rows
                        trip.members.forEach { member ->
                            val paidAmt = userPaidTotals[member.name] ?: 0.0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(member.name, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("₹${String.format("%.0f", paidAmt)}", fontSize = 12.sp, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                Text("₹${String.format("%.0f", shareVal)}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Outstanding balances suggestions:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (settlementSuggestions.isEmpty()) {
                            Text("All balances fully settled!", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Column {
                                settlementSuggestions.forEach { sugg ->
                                    Text(sugg, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Shared Attachments
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Shared Group Documents (${trip.attachments.size})", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                            IconButton(
                                onClick = {
                                    viewModel.addTripAttachment(
                                        trip.id,
                                        TripAttachment(
                                            id = "a_" + System.currentTimeMillis(),
                                            name = "Aadhaar_and_Passports.pdf",
                                            fileType = "PDF",
                                            size = "1.8 MB",
                                            uploadedBy = "Ramesh Sharma"
                                        )
                                    )
                                    Toast.makeText(context, "Mock Document uploaded!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = "Upload", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            trip.attachments.forEach { doc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = when (doc.fileType) {
                                                "PDF" -> Icons.Default.PictureAsPdf
                                                "TICKET" -> Icons.Default.ConfirmationNumber
                                                else -> Icons.Default.Image
                                            },
                                            contentDescription = "File Type",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(doc.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${doc.size} • Uploaded by ${doc.uploadedBy}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            Toast.makeText(context, "Downloading simulated attachment: ${doc.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Activity feed timeline
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Group Activity Timeline Feed", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            trip.timeline.forEach { log ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = when (log.icon) {
                                            "flight" -> Icons.Default.FlightTakeoff
                                            "hotel" -> Icons.Default.Hotel
                                            "check" -> Icons.Default.AssignmentTurnedIn
                                            "payment" -> Icons.Default.Payments
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = "Log",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(log.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(log.timestamp, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddChecklistDialog) {
        var itemTitle by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddChecklistDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Custom Checklist Task Stage", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = itemTitle, onValueChange = { itemTitle = it }, label = { Text("Task Title *") })
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddChecklistDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            if (itemTitle.trim().isNotEmpty()) {
                                viewModel.addTripChecklistItem(
                                    trip.id,
                                    TripChecklistItem("it_" + System.currentTimeMillis(), itemTitle.trim(), false, "Custom")
                                )
                                showAddChecklistDialog = false
                            }
                        }) {
                            Text("Add Task", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showExpenseDialog) {
        var costDesc by remember { mutableStateOf("") }
        var costAmt by remember { mutableStateOf("") }
        var costPayer by remember { mutableStateOf(trip.members.firstOrNull()?.name ?: "Ramesh Sharma") }
        var payerExpanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showExpenseDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Log Shared Vacation Expense", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = costDesc,
                        onValueChange = { costDesc = it },
                        label = { Text("Expense Description *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = costAmt,
                        onValueChange = { costAmt = it },
                        label = { Text("Amount (₹) *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Payer Selector Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { payerExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Who Paid: $costPayer")
                        }

                        DropdownMenu(expanded = payerExpanded, onDismissRequest = { payerExpanded = false }) {
                            trip.members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = {
                                        costPayer = m.name
                                        payerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showExpenseDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            val amt = costAmt.toDoubleOrNull()
                            if (costDesc.trim().isNotEmpty() && amt != null) {
                                viewModel.addTripExpense(
                                    trip.id,
                                    TripExpense(
                                        id = "exp_" + System.currentTimeMillis(),
                                        payer = costPayer,
                                        description = costDesc.trim(),
                                        amount = amt,
                                        splitWith = trip.members.map { it.name }
                                    )
                                )
                                showExpenseDialog = false
                            } else {
                                Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Save Cost", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardMetricsItem(
    title: String,
    value: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun GroupsLandingScreen(
    viewModel: BandhamViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.eventsList.collectAsStateWithLifecycle()
    val vacations by viewModel.vacationTrips.collectAsStateWithLifecycle()
    val customGroups by viewModel.customGroups.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    var currentSubMode by remember { mutableStateOf("landing") } // "landing", "event", "vacation"
    var selectedId by remember { mutableStateOf<String?>(null) }

    if (currentSubMode == "event" && selectedId != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentSubMode = "landing" }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to Groups landing",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            EventGuestTrackerWorkspace(
                viewModel = viewModel,
                initialEventId = selectedId,
                modifier = Modifier.weight(1f)
            )
        }
        return
    }

    if (currentSubMode == "vacation" && selectedId != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentSubMode = "landing" }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to Groups landing",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            VacationPlannerWorkspace(
                viewModel = viewModel,
                initialTripId = selectedId,
                modifier = Modifier.weight(1f)
            )
        }
        return
    }

    // Filter lists
    val filteredEvents = events.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.type.contains(searchQuery, ignoreCase = true) ||
        it.notes.contains(searchQuery, ignoreCase = true)
    }

    val filteredVacations = vacations.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.destination.contains(searchQuery, ignoreCase = true) ||
        it.notes.contains(searchQuery, ignoreCase = true)
    }

    val filteredCustom = customGroups.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true) ||
        it.notes.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.GroupAdd, contentDescription = "Add Group") },
                text = { Text("Create Circle") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 60.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Bandham Secure Groups Hub",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Coordinate private family events, collaborative vacation travels, and custom sub-circles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search groups by name or category...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Quick Metrics Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardMetricsItem(
                        title = "Event Circles",
                        value = events.size.toString(),
                        bgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f)
                    )
                    CardMetricsItem(
                        title = "Trips Configured",
                        value = vacations.size.toString(),
                        bgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                    CardMetricsItem(
                        title = "Custom Circles",
                        value = customGroups.size.toString(),
                        bgColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 1. EVENT GROUPS SECTION
            item {
                SectionHeaderRow(
                    title = "Event Coordination Groups",
                    icon = Icons.Default.Celebration,
                    count = filteredEvents.size
                )
            }

            if (filteredEvents.isEmpty()) {
                item {
                    EmptySectionPlaceholder("No active event coordination groups found.")
                }
            } else {
                items(filteredEvents) { event ->
                    EventGroupCard(
                        event = event,
                        onOpen = {
                            selectedId = event.id
                            currentSubMode = "event"
                        }
                    )
                }
            }

            // 2. VACATION GROUPS SECTION
            item {
                SectionHeaderRow(
                    title = "Collaborative Vacation Hubs",
                    icon = Icons.Default.FlightTakeoff,
                    count = filteredVacations.size
                )
            }

            if (filteredVacations.isEmpty()) {
                item {
                    EmptySectionPlaceholder("No active collaborative vacation planners found.")
                }
            } else {
                items(filteredVacations) { trip ->
                    VacationGroupCard(
                        trip = trip,
                        onOpen = {
                            selectedId = trip.id
                            currentSubMode = "vacation"
                        }
                    )
                }
            }

            // 3. CUSTOM GROUPS SECTION
            item {
                SectionHeaderRow(
                    title = "Custom Family Circles",
                    icon = Icons.Default.Groups,
                    count = filteredCustom.size
                )
            }

            if (filteredCustom.isEmpty()) {
                item {
                    EmptySectionPlaceholder("No active custom sub-circles. Tap 'Create Group' to get started!")
                }
            } else {
                items(filteredCustom) { uGroup ->
                    CustomGroupCard(
                        group = uGroup,
                        onDelete = { viewModel.deleteCustomGroup(uGroup.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Create New Custom Group Dialog
    if (showCreateDialog) {
        var groupName by remember { mutableStateOf("") }
        var groupDesc by remember { mutableStateOf("") }
        var groupCat by remember { mutableStateOf("Family Meetup") }
        var memberInput by remember { mutableStateOf("") }
        var rawNotes by remember { mutableStateOf("") }

        val categoriesList = listOf("Family Meetup", "Regular Puja", "Festivals", "Sports & Meetups", "General")

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Custom Circle")
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Circle / Group Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Short Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text("Category", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.forEach { cat ->
                            FilterChip(
                                selected = groupCat == cat,
                                onClick = { groupCat = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = memberInput,
                        onValueChange = { memberInput = it },
                        label = { Text("Initial Members (comma-separated)") },
                        placeholder = { Text("e.g., Alok Sharma, Sadhana, Preeti") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = rawNotes,
                        onValueChange = { rawNotes = it },
                        label = { Text("Quick Notes / Guidelines") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            val parsedMembers = memberInput.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            val newGrp = CustomGroup(
                                id = "custom_" + System.currentTimeMillis(),
                                name = groupName,
                                description = groupDesc,
                                category = groupCat,
                                memberNames = parsedMembers,
                                notes = rawNotes
                            )
                            viewModel.addCustomGroup(newGrp)
                            showCreateDialog = false
                        }
                    },
                    enabled = groupName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionHeaderRow(
    title: String,
    icon: ImageVector,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptySectionPlaceholder(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun EventGroupCard(
    event: EventWorkspace,
    onOpen: () -> Unit
) {
    val totalGuests = event.guests.size
    val confirmedCount = event.guests.count { it.status == "CONFIRMED" || it.status == "ATTENDED" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.type,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (event.notes.isNotEmpty()) {
                Text(
                    text = event.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL GUESTS", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
                        Text(totalGuests.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("CONFIRMED", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
                        Text(confirmedCount.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }

                Button(
                    onClick = onOpen,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun VacationGroupCard(
    trip: VacationTrip,
    onOpen: () -> Unit
) {
    val totalChecklist = trip.checklist.size
    val completedChecklist = trip.checklist.count { it.isCompleted }
    val progressPercentage = if (totalChecklist > 0) ((completedChecklist.toFloat() / totalChecklist) * 100).toInt() else 0
    val totalExpenses = trip.expenses.sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = trip.destination,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${trip.members.size} Members",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Preparation Checklist", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
                    Text("$completedChecklist/$totalChecklist ($progressPercentage%)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { if (totalChecklist > 0) completedChecklist.toFloat() / totalChecklist else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL EXPENSES", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
                    Text("₹%,.0f".format(totalExpenses), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = onOpen,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Explore Trip", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun CustomGroupCard(
    group: CustomGroup,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = group.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = group.category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (group.memberNames.isNotEmpty()) {
                Text(
                    text = "MEMBERS INVOLVED",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    group.memberNames.forEach { name ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(name, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Note,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = group.notes.ifEmpty { "No custom guidelines written." },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.65f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

