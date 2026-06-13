package com.example.ui

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FamilyMember
import com.example.data.model.EventReminder
import com.example.data.model.MoneyTransaction
import com.example.viewmodel.BandhamViewModel
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BandhamApp(viewModel: BandhamViewModel) {
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val passcode by viewModel.passcode.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    // Collect language state to trigger real-time reactive recomposition
    val currentLanguage by Localizations.selectedLanguage.collectAsStateWithLifecycle()

    val profileName = currentUser?.displayName ?: "Family Organizer"

    var activeTab by remember { mutableStateOf("home") }

    // Forms Dialog states
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bandham_scaffold"),
        bottomBar = {
            if (currentUser != null && !isLocked) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val items = listOf(
                            Triple("home", "Home", Icons.Default.Home),
                            Triple("family", "Family", Icons.Default.Groups),
                            Triple("contacts", "Contacts", Icons.Default.ContactPhone),
                            Triple("events", "Events", Icons.Default.Celebration),
                            Triple("groups", "Groups", Icons.Default.FlightTakeoff),
                            Triple("ledger", "Ledger", Icons.Default.CurrencyRupee),
                            Triple("reminders", "Reminders", Icons.Default.Notifications),
                            Triple("settings", "More", Icons.Default.MoreHoriz)
                        )
                        items.forEach { (route, label, icon) ->
                            val isSelected = activeTab == route
                            val displayColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { activeTab = route }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = displayColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    color = displayColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentUser,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "auth_session_transition"
        ) { userState ->
            if (userState == null) {
                AuthScreen(viewModel = viewModel)
            } else {
                InteractiveAnimatedContent(
                    isLocked = isLocked,
                    viewModel = viewModel,
                    activeTab = activeTab,
                    familyMembers = familyMembers,
                    reminders = reminders,
                    transactions = transactions,
                    profileName = profileName,
                    innerPadding = innerPadding,
                    onLockAgain = { viewModel.lockApp() },
                    showAddMember = { showAddMemberDialog = true },
                    showAddReminder = { showAddReminderDialog = true },
                    showAddTx = { showAddTxDialog = true },
                    onNavigateToTab = { activeTab = it }
                )
            }
        }

        // dialog additions
        if (showAddMemberDialog) {
            AddMemberDialog(
                onDismiss = { showAddMemberDialog = false },
                onSave = { name, rel, phone, email, bday, anniv, notes, photoPath ->
                    viewModel.addFamilyMember(
                        FamilyMember(
                            name = name,
                            relationship = rel,
                            phone = phone,
                            email = email,
                            birthday = bday,
                            anniversary = anniv,
                            notes = notes,
                            photoUri = photoPath
                        )
                    )
                    showAddMemberDialog = false
                }
            )
        }

        if (showAddReminderDialog) {
            AddReminderDialog(
                familyMembers = familyMembers,
                onDismiss = { showAddReminderDialog = false },
                onSave = { title, date, type, notes, memberId ->
                    viewModel.addReminder(
                        EventReminder(
                            title = title,
                            date = date,
                            type = type,
                            notes = notes,
                            memberId = memberId
                        )
                    )
                    showAddReminderDialog = false
                }
            )
        }

        if (showAddTxDialog) {
            AddTransactionDialog(
                familyMembers = familyMembers,
                onDismiss = { showAddTxDialog = false },
                onSave = { name, mId, amount, type, date, purpose, proofPath ->
                    viewModel.addTransaction(
                        MoneyTransaction(
                            memberName = name,
                            memberId = mId,
                            amount = amount,
                            type = type,
                            date = date,
                            purpose = purpose,
                            proofImageUri = proofPath
                        )
                    )
                    showAddTxDialog = false
                }
            )
        }
    }
}

@Composable
fun InteractiveAnimatedContent(
    isLocked: Boolean,
    viewModel: BandhamViewModel,
    activeTab: String,
    familyMembers: List<FamilyMember>,
    reminders: List<EventReminder>,
    transactions: List<MoneyTransaction>,
    profileName: String,
    innerPadding: PaddingValues,
    onLockAgain: () -> Unit,
    showAddMember: () -> Unit,
    showAddReminder: () -> Unit,
    showAddTx: () -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    AnimatedContent(
        targetState = isLocked,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "login_transition"
    ) { locked ->
        if (locked) {
            LockScreen(
                onUnlock = { entered -> viewModel.unlockApp(entered) }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (activeTab) {
                    "home" -> HomeDashboardScreen(
                        profileName = profileName,
                        familyMembers = familyMembers,
                        reminders = reminders,
                        transactions = transactions,
                        onLock = { onLockAgain() },
                        showAddMember = { showAddMember() },
                        showAddReminder = { showAddReminder() },
                        onToggleReminder = { viewModel.toggleReminderCompletion(it) },
                        onDeleteMember = { viewModel.deleteFamilyMember(it) },
                        viewModel = viewModel,
                        onNavigateToTab = onNavigateToTab
                    )
                    "family" -> {
                        var familySubTab by remember { mutableStateOf("tree") } // "tree" or "list"
                        Column(modifier = Modifier.fillMaxSize()) {
                            TabRow(
                                selectedTabIndex = if (familySubTab == "tree") 0 else 1,
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Tab(
                                    selected = familySubTab == "tree",
                                    onClick = { familySubTab = "tree" },
                                    text = { Text("Family Tree (Vansha)", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.AccountTree, contentDescription = "Family Tree") }
                                )
                                Tab(
                                    selected = familySubTab == "list",
                                    onClick = { familySubTab = "list" },
                                    text = { Text("Members Vault", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.ContactPage, contentDescription = "Members list") }
                                )
                            }

                            if (familySubTab == "tree") {
                                FamilyTreeScreen(
                                    familyMembers = familyMembers,
                                    showAddMember = showAddMember
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item {
                                        Button(
                                            onClick = showAddMember,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add Family Member")
                                        }
                                    }

                                    if (familyMembers.isEmpty()) {
                                        item {
                                            Text("No members registered in secure vault yet.", color = MaterialTheme.colorScheme.outline)
                                        }
                                    } else {
                                        items(familyMembers) { member ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(member.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            Text(member.relationship, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                                        }
                                                    }

                                                    IconButton(onClick = { viewModel.deleteFamilyMember(member.id) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "contacts" -> GmailSyncAndBackupView(viewModel = viewModel)
                    "events" -> EventGuestTrackerWorkspace(viewModel = viewModel)
                    "groups" -> GroupsLandingScreen(viewModel = viewModel)
                    "reminders" -> RemindersScreen(
                        reminders = reminders,
                        familyMembers = familyMembers,
                        showAddReminder = { showAddReminder() },
                        onToggle = { viewModel.toggleReminderCompletion(it) },
                        onDelete = { viewModel.deleteReminder(it) }
                    )
                    "ledger" -> LedgerScreen(
                        transactions = transactions,
                        familyMembers = familyMembers,
                        showAddTx = { showAddTx() },
                        onToggleSettle = { viewModel.toggleTransactionSettle(it) },
                        onDelete = { viewModel.deleteTransaction(it) }
                    )
                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        profileName = profileName,
                        onLock = { onLockAgain() }
                    )
                }
            }
        }
    }
}

// ==================== LOCK SCREEN COMPOSABLE ====================
@Composable
fun LockScreen(
    onUnlock: (String) -> Boolean
) {
    var enteredText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6750A4), // Geometric Deep Primary
                        Color(0xFF21005D)  // Geometric Dark Primary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Lock Logo",
                tint = Color(0xFFEADDFF), // Beautiful Geometric Lavender Accent
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "BANDHAM",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your Private Family Vault",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFEADDFF),
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "“Family tree, reminders, contacts, & money tracking in one private place.”",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
            )

            // Passcode Input Area
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter Private Passcode",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Dots indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        for (i in 1..4) {
                            val active = enteredText.length >= i
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (active) Color(0xFFEADDFF) else Color.White.copy(
                                            alpha = 0.25f
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (active) Color(0xFFEADDFF) else Color.White.copy(
                                            alpha = 0.4f
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFFFB4AB), // readable error shade
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Keypad rows
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("Clear", "0", "OK")
                    )

                    keys.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            row.forEach { key ->
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        if (key == "Clear") {
                                            if (enteredText.isNotEmpty()) {
                                                enteredText = enteredText.dropLast(1)
                                            }
                                        } else if (key == "OK") {
                                            if (enteredText == "1234") {
                                                onUnlock(enteredText)
                                            } else {
                                                errorMessage = "Incorrect Passcode! Try 1234"
                                                enteredText = ""
                                            }
                                        } else {
                                            if (enteredText.length < 4) {
                                                enteredText += key
                                                // Trigger unlock when 4 digits are completed automatically
                                                if (enteredText.length == 4) {
                                                    val success = onUnlock(enteredText)
                                                    if (!success) {
                                                        errorMessage = "Incorrect Passcode! Try 1234"
                                                        enteredText = ""
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (key == "OK") Color(0xFF6750A4) else Color.White.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(55.dp)
                                        .testTag("keypad_$key")
                                ) {
                                    if (key == "Clear") {
                                        Icon(
                                            Icons.Default.Backspace,
                                            contentDescription = "Backspace",
                                            tint = Color.White
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Demo defaults: Passcode is 1234\nYou can customize this in Security Settings.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Safety statement
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Private Symbol",
                    tint = Color(0xFFEADDFF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "On-Device SQLite Protection. 100% Private.",
                    color = Color(0xFFEADDFF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ==================== HOME/DASHBOARD SCREEN ====================
@Composable
fun HomeDashboardScreen(
    profileName: String,
    familyMembers: List<FamilyMember>,
    reminders: List<EventReminder>,
    transactions: List<MoneyTransaction>,
    onLock: () -> Unit,
    showAddMember: () -> Unit,
    showAddReminder: () -> Unit,
    onToggleReminder: (EventReminder) -> Unit,
    onDeleteMember: (Int) -> Unit,
    viewModel: BandhamViewModel,
    onNavigateToTab: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredMembers = if (searchQuery.isBlank()) {
        familyMembers
    } else {
        familyMembers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.relationship.contains(searchQuery, ignoreCase = true)
        }
    }

    // Money statistics
    val totalLent = transactions.filter { it.type == "LENT" && !it.isSettled }.sumOf { it.amount }
    val totalBorrowed = transactions.filter { it.type == "BORROWED" && !it.isSettled }.sumOf { it.amount }

    // Aggregate helper for upcoming agenda Items
    data class UpcomingItem(
        val title: String,
        val dateStr: String,
        val type: String,
        val daysLeft: Long,
        val memberName: String = "",
        val originalReminder: EventReminder? = null
    )

    fun calculateDaysUntilRecurring(dateStr: String): Long {
        if (dateStr.isBlank()) return 999L
        return try {
            val today = LocalDate.now()
            val parsed = LocalDate.parse(dateStr)
            var anniversaryThisYear = parsed.withYear(today.year)
            if (anniversaryThisYear.isBefore(today)) {
                anniversaryThisYear = anniversaryThisYear.plusYears(1)
            }
            ChronoUnit.DAYS.between(today, anniversaryThisYear)
        } catch (e: Exception) {
            999L
        }
    }

    fun calculateDaysUntilOneTime(dateStr: String): Long {
        if (dateStr.isBlank()) return 999L
        return try {
            val today = LocalDate.now()
            val parsed = LocalDate.parse(dateStr)
            ChronoUnit.DAYS.between(today, parsed)
        } catch (e: Exception) {
            999L
        }
    }

    // Process and sort upcoming items
    val upcomingItems = remember(familyMembers, reminders, transactions) {
        val list = mutableListOf<UpcomingItem>()

        // 1. Collect birthdays from family members
        familyMembers.forEach { member ->
            if (!member.birthday.isNullOrBlank()) {
                val days = calculateDaysUntilRecurring(member.birthday)
                if (days in 0..365) {
                    list.add(
                        UpcomingItem(
                            title = "${member.name}'s Birthday 🎂",
                            dateStr = member.birthday,
                            type = "Birthday",
                            daysLeft = days,
                            memberName = member.name
                        )
                    )
                }
            }
            if (!member.anniversary.isNullOrBlank()) {
                val days = calculateDaysUntilRecurring(member.anniversary)
                if (days in 0..365) {
                    list.add(
                        UpcomingItem(
                            title = "${member.name}'s Anniversary 💍",
                            dateStr = member.anniversary,
                            type = "Anniversary",
                            daysLeft = days,
                            memberName = member.name
                        )
                    )
                }
            }
        }

        // 2. Collect db reminders
        reminders.filter { !it.isCompleted }.forEach { r ->
            val days = calculateDaysUntilOneTime(r.date)
            if (days >= 0) {
                list.add(
                    UpcomingItem(
                        title = "${r.title} ⏰",
                        dateStr = r.date,
                        type = "Reminder",
                        daysLeft = days,
                        originalReminder = r
                    )
                )
            }
        }

        // 3. Collect pending ledger transactions due
        transactions.filter { !it.isSettled }.forEach { t ->
            val days = calculateDaysUntilOneTime(t.date)
            if (days >= 0) {
                list.add(
                    UpcomingItem(
                        title = "Pending ledger: ${t.type} ₹${t.amount} (${t.memberName})",
                        dateStr = t.date,
                        type = "Ledger",
                        daysLeft = days
                    )
                )
            }
        }

        list.sortedBy { it.daysLeft }.take(5)
    }

    // Counts for stats summary
    val birthdayCount = familyMembers.filter { !it.birthday.isNullOrBlank() }.size
    val anniversaryCount = familyMembers.filter { !it.anniversary.isNullOrBlank() }.size
    val pendingTxCount = transactions.filter { !it.isSettled }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Hero Header with Language Selector
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Localizations.string("welcome"),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            )
                            Text(
                                text = Localizations.string("vault_title", profileName),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Globe Language Switcher
                            var showLangDropdown by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { showLangDropdown = true },
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = "Switch language",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                DropdownMenu(
                                    expanded = showLangDropdown,
                                    onDismissRequest = { showLangDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("English (EN)") },
                                        onClick = {
                                            viewModel.changeLanguage("en")
                                            showLangDropdown = false
                                        },
                                        leadingIcon = { Text("🇬🇧") }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("हिन्दी (HI)") },
                                        onClick = {
                                            viewModel.changeLanguage("hi")
                                            showLangDropdown = false
                                        },
                                        leadingIcon = { Text("🇮🇳") }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("తెలుగు (TE)") },
                                        onClick = {
                                            viewModel.changeLanguage("te")
                                            showLangDropdown = false
                                        },
                                        leadingIcon = { Text("🇮🇳") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = onLock,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                                    .testTag("lock_action")
                            ) {
                                Icon(
                                    Icons.Default.LockOpen,
                                    contentDescription = "Tap to seal vault",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = Localizations.string("home_desc"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // REDESIGNED Quick Widgets Dashboard Grid (Part 5)
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Localizations.string("quick_reminders"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickWidgetCard(
                        title = Localizations.string("birthdays"),
                        value = birthdayCount.toString(),
                        subtitle = "Recorded logs",
                        icon = Icons.Default.Cake,
                        color = Color(0xFFF57C00),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab("reminders") }
                    )

                    QuickWidgetCard(
                        title = Localizations.string("anniversaries"),
                        value = anniversaryCount.toString(),
                        subtitle = "Wedding milestones",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFD81B60),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab("reminders") }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickWidgetCard(
                        title = "Pending ₹",
                        value = "₹%,.0f".format(totalLent),
                        subtitle = "Udhaar/Karz balance",
                        icon = Icons.Default.CurrencyRupee,
                        color = com.example.ui.theme.GeometricTealAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab("ledger") }
                    )

                    QuickWidgetCard(
                        title = "Reminders",
                        value = reminders.filter { !it.isCompleted }.size.toString(),
                        subtitle = "Incomplete items",
                        icon = Icons.Default.NotificationsActive,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab("reminders") }
                    )
                }
            }
        }

        // Today's Agenda (Merged next upcoming items)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Agenda: Closest Milestones & Reminders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (upcomingItems.isEmpty()) {
                        Text(
                            text = "• " + Localizations.string("empty_reminders"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        upcomingItems.forEach { item ->
                            val daysText = when (item.daysLeft) {
                                0L -> "Today ⚡"
                                1L -> "Tomorrow 📅"
                                else -> "In ${item.daysLeft} days"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (item.type == "Ledger") {
                                            onNavigateToTab("ledger")
                                        } else {
                                            onNavigateToTab("reminders")
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val icon = when (item.type) {
                                        "Birthday" -> Icons.Default.Cake
                                        "Anniversary" -> Icons.Default.Favorite
                                        "Ledger" -> Icons.Default.TrendingUp
                                        else -> Icons.Default.Notifications
                                    }
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = daysText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.daysLeft == 0L) Color.Red else MaterialTheme.colorScheme.secondary
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }

        // Search Bar for relatives
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(Localizations.string("search_at")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input"),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Section Title: Members Vault
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localizations.string("members_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                TextButton(onClick = showAddMember) {
                    Icon(Icons.Default.Add, contentDescription = "Add Person")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Localizations.string("add_member"))
                }
            }
        }

        // Main listings of members
        if (filteredMembers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = "Empty friends",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) Localizations.string("empty_members") else "No matches found.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "Tap 'Add Member' to record family members and relations." else "Double check name or relation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredMembers, key = { it.id }) { member ->
                FamilyMemberCard(
                    member = member,
                    onDelete = onDeleteMember
                )
            }
        }
    }
}

@Composable
fun QuickWidgetCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun CardEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FamilyMemberCard(
    member: FamilyMember,
    onDelete: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar profile placeholder
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (member.photoUri.isNotEmpty()) {
                            LocalProfileImage(
                                photoUri = member.photoUri,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = member.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        // Relation Tag
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = member.relationship,
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                IconButton(onClick = { onDelete(member.id) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete relative outline",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (member.phone.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = "Phone icon", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(member.phone, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (member.email.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Email, contentDescription = "Email icon", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(member.email, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (member.birthday.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Cake, contentDescription = "Cake icon", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Birthday: ${member.birthday}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (member.anniversary.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Favorite, contentDescription = "Anniversary icon", modifier = Modifier.size(16.dp), tint = Color.Red.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anniversary: ${member.anniversary}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (member.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Private Notes:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Text(member.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== FAMILY TREE VIEW SCREEN ====================
@Composable
fun FamilyTreeScreen(
    familyMembers: List<FamilyMember>,
    showAddMember: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var expandedGrandparents by remember { mutableStateOf(true) }
    var expandedParents by remember { mutableStateOf(true) }
    var expandedPeers by remember { mutableStateOf(true) }
    var expandedChildren by remember { mutableStateOf(true) }
    var expandedGrandchildren by remember { mutableStateOf(true) }

    var selectedMemberForDetail by remember { mutableStateOf<FamilyMember?>(null) }

    // Generational categorization (Part 3)
    val gpList = familyMembers.filter {
        val rel = it.relationship.lowercase()
        rel.contains("grandparent") || rel.contains("grandfather") || rel.contains("grandmother") ||
                rel.contains("dada") || rel.contains("dadi") || rel.contains("nana") || rel.contains("nani")
    }

    val parentsList = familyMembers.filter {
        val rel = it.relationship.lowercase()
        (rel.contains("father") || rel.contains("mother") || rel.contains("parent") ||
                rel.contains("uncle") || rel.contains("aunt")) && !rel.contains("grand")
    }

    // Peers includes Self and spouse, brothers, sisters, cousins
    val peersList = familyMembers.filter {
        val rel = it.relationship.lowercase()
        rel.contains("spouse") || rel.contains("brother") || rel.contains("sister") ||
                rel.contains("cousin") || rel.contains("wife") || rel.contains("husband") ||
                rel.contains("self")
    }

    val childrenList = familyMembers.filter {
        val rel = it.relationship.lowercase()
        (rel.contains("son") || rel.contains("daughter") || rel.contains("nephew") ||
                rel.contains("niece") || rel.contains("child")) && !rel.contains("grand")
    }

    val grandchildrenList = familyMembers.filter {
        val rel = it.relationship.lowercase()
        rel.contains("grandchild") || rel.contains("grandson") || rel.contains("granddaughter")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(androidx.compose.ui.graphics.RectangleShape)
    ) {
        // Pannable / Zoomable Arena (Part 3)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                        offset = Offset(offset.x + pan.x, offset.y + pan.y)
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            // Real tree layout in a scrollable column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Vansha Tree (वंश)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "5-Generational Interactive View",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tier 1: Grandparents (Gen -2)
                GenerationalRow(
                    title = "GRANDPARENTS (GEN -2)",
                    isExpanded = expandedGrandparents,
                    onToggleExpand = { expandedGrandparents = !expandedGrandparents },
                    members = gpList,
                    onMemberClick = { selectedMemberForDetail = it },
                    onAdd = showAddMember,
                    tagColor = Color(0xFFE91E63)
                )

                if (expandedGrandparents && gpList.isNotEmpty()) {
                    TreeConnectorLine()
                }

                // Tier 2: Parents (Gen -1)
                GenerationalRow(
                    title = "PARENTS (GEN -1)",
                    isExpanded = expandedParents,
                    onToggleExpand = { expandedParents = !expandedParents },
                    members = parentsList,
                    onMemberClick = { selectedMemberForDetail = it },
                    onAdd = showAddMember,
                    tagColor = com.example.ui.theme.GeometricYellowAccent
                )

                if (expandedParents && parentsList.isNotEmpty()) {
                    TreeConnectorLine()
                }

                // Tier 3: Peers / Self (Gen 0)
                GenerationalRow(
                    title = "YOUR GENERATION (GEN 0)",
                    isExpanded = expandedPeers,
                    onToggleExpand = { expandedPeers = !expandedPeers },
                    members = peersList,
                    onMemberClick = { selectedMemberForDetail = it },
                    onAdd = showAddMember,
                    tagColor = com.example.ui.theme.GeometricPrimary
                )

                if (expandedPeers && peersList.isNotEmpty()) {
                    TreeConnectorLine()
                }

                // Tier 4: Children (Gen +1)
                GenerationalRow(
                    title = "CHILDREN (GEN +1)",
                    isExpanded = expandedChildren,
                    onToggleExpand = { expandedChildren = !expandedChildren },
                    members = childrenList,
                    onMemberClick = { selectedMemberForDetail = it },
                    onAdd = showAddMember,
                    tagColor = com.example.ui.theme.GeometricTealAccent
                )

                if (expandedChildren && childrenList.isNotEmpty()) {
                    TreeConnectorLine()
                }

                // Tier 5: Grandchildren (Gen +2)
                GenerationalRow(
                    title = "GRANDCHILDREN (GEN +2)",
                    isExpanded = expandedGrandchildren,
                    onToggleExpand = { expandedGrandchildren = !expandedGrandchildren },
                    members = grandchildrenList,
                    onMemberClick = { selectedMemberForDetail = it },
                    onAdd = showAddMember,
                    tagColor = Color(0xFF9C27B0)
                )
            }
        }

        // Floating Zoom / Pan Controls (Aesthetic Overlay in Corner for Emulators)
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
            elevation = CardDefaults.cardElevation(6.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { scale = (scale + 0.15f).coerceAtMost(3.0f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = { scale = (scale - 0.15f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = { scale = 1.0f; offset = Offset.Zero },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    // Detail Dialog popup when tapping a member card (Part 4)
    selectedMemberForDetail?.let { member ->
        AlertDialog(
            onDismissRequest = { selectedMemberForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (member.photoUri.isNotEmpty()) {
                            LocalProfileImage(photoUri = member.photoUri, modifier = Modifier.fillMaxSize())
                        } else {
                            Text(member.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(member.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(member.relationship, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    if (member.phone.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = "Phone", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(member.phone, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (member.email.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(member.email, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (member.birthday.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cake, contentDescription = "Birthday", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Birthday: ${member.birthday}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (member.anniversary.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = "Anniversary", tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anniversary: ${member.anniversary}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (member.notes.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Private Notes:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(member.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMemberForDetail = null }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun GenerationalRow(
    title: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    members: List<FamilyMember>,
    onMemberClick: (FamilyMember) -> Unit,
    onAdd: () -> Unit,
    tagColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expand/collapse generational header bar (Part 3)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(tagColor.copy(alpha = 0.12f))
                .border(1.dp, tagColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .clickable { onToggleExpand() }
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = tagColor,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle Generation Expand",
                tint = tagColor,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(visible = isExpanded) {
            if (members.isEmpty()) {
                OutlinedButton(
                    onClick = onAdd,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add Relative", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center
                ) {
                    members.forEach { m ->
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .width(120.dp)
                                .clickable { onMemberClick(m) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(2.dp, tagColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (m.photoUri.isNotEmpty()) {
                                    LocalProfileImage(photoUri = m.photoUri, modifier = Modifier.fillMaxSize())
                                } else {
                                    Text(
                                        m.name.take(1).uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = tagColor,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = m.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = m.relationship,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TreeConnectorLine() {
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(2.dp)
            .drawBehind {
                drawLine(
                    color = outlineColor,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
    )
}

// ==================== REMINDERS SCREEN ====================
@Composable
fun RemindersScreen(
    reminders: List<EventReminder>,
    familyMembers: List<FamilyMember>,
    showAddReminder: () -> Unit,
    onToggle: (EventReminder) -> Unit,
    onDelete: (Int) -> Unit
) {
    var activeGiftOccasion by remember { mutableStateOf<EventReminder?>(null) }

    activeToneGiftOccasionDialog(activeGiftOccasion, familyMembers) {
        activeGiftOccasion = null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Family Calendar & Reminders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track Indian Shubh birthdays, anniversaries, and customizable family events.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = showAddReminder,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
        }

        if (reminders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    CardEmptyState(
                        icon = Icons.Default.EventNote,
                        title = "Vault Schedule is Clean",
                        description = "No birthdays or wedding reminders currently saved."
                    )
                }
            }
        } else {
            items(reminders, key = { it.id }) { item ->
                ReminderRowItem(
                    reminder = item,
                    onToggle = onToggle,
                    onDelete = onDelete,
                    onGiftClick = { activeGiftOccasion = it }
                )
            }
        }
    }
}

@Composable
fun activeToneGiftOccasionDialog(
    reminder: EventReminder?,
    familyMembers: List<FamilyMember>,
    onDismiss: () -> Unit
) {
    reminder?.let { rem ->
        val matchingMember = familyMembers.find { m -> m.id == rem.memberId }
        val memberName = matchingMember?.name ?: "Relative"
        val relationship = matchingMember?.relationship ?: "Family Member"
        GiftAssistantDialog(
            occasionType = rem.type,
            relationship = "$memberName ($relationship)",
            onDismiss = onDismiss
        )
    }
}

@Composable
fun ReminderRowItem(
    reminder: EventReminder,
    onToggle: (EventReminder) -> Unit,
    onDelete: (Int) -> Unit,
    onGiftClick: ((EventReminder) -> Unit)? = null
) {
    val isCompleted = reminder.isCompleted
    val colorAccent = when (reminder.type) {
        "Birthday" -> com.example.ui.theme.GeometricYellowAccent
        "Anniversary" -> com.example.ui.theme.GeometricRedAccent
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onToggle(reminder) }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(colorAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                reminder.type,
                                fontSize = 10.sp,
                                color = colorAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Event trigger date",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reminder.date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    if (reminder.notes.isNotEmpty()) {
                        Text(
                            text = reminder.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val isOccasion = reminder.type == "Birthday" || reminder.type == "Anniversary" || reminder.type == "Occasion" || reminder.type == "Festival"
                if (isOccasion && onGiftClick != null) {
                    IconButton(onClick = { onGiftClick(reminder) }) {
                        Text("🎁", fontSize = 16.sp)
                    }
                }

                IconButton(onClick = { onDelete(reminder.id) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Reminder outline",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }
}

// ==================== PRIVATE LEDGER SCREEN (BAHI KHATA) ====================
@Composable
fun LedgerScreen(
    transactions: List<MoneyTransaction>,
    familyMembers: List<FamilyMember>,
    showAddTx: () -> Unit,
    onToggleSettle: (MoneyTransaction) -> Unit,
    onDelete: (Int) -> Unit
) {
    val lentList = transactions.filter { it.type == "LENT" }
    val borrowedList = transactions.filter { it.type == "BORROWED" }

    var filterType by remember { mutableStateOf("ALL") } // ALL, LENT, BORROWED

    val activeList = when (filterType) {
        "LENT" -> lentList
        "BORROWED" -> borrowedList
        else -> transactions
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Indian Ledger styled card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Udhaar & Karz Ledger (लेन-देन बही)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "100% encrypted, locally backed ledger keeping track of money given, taken or pending.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = showAddTx,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add ledger item")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Entry")
                        }
                    }
                }
            }
        }

        // Filters Tab row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All Book",
                    "LENT" to "You Lent (Udhaar)",
                    "BORROWED" to "You Borrowed (Karz)"
                )
                filters.forEach { (key, value) ->
                    val active = filterType == key
                    Button(
                        onClick = { filterType = key },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        if (activeList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    CardEmptyState(
                        icon = Icons.Default.ReceiptLong,
                        title = "Ledger Book Empty",
                        description = "No loans or borrowings recorded. Everything is fully balanced."
                    )
                }
            }
        } else {
            items(activeList, key = { it.id }) { tx ->
                LedgerRowItem(
                    tx = tx,
                    onToggleSettle = onToggleSettle,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
fun LedgerRowItem(
    tx: MoneyTransaction,
    onToggleSettle: (MoneyTransaction) -> Unit,
    onDelete: (Int) -> Unit
) {
    val isSettled = tx.isSettled
    val isLent = tx.type == "LENT"
    val colorText = if (isLent) com.example.ui.theme.GeometricTealAccent else com.example.ui.theme.GeometricYellowAccent
    val badgeLabel = if (isLent) "LENT / GIVEN" else "BORROWED / OWED"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSettled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isSettled) MaterialTheme.colorScheme.outline.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Settle Checkbox
                IconButton(onClick = { onToggleSettle(tx) }) {
                    Icon(
                        imageVector = if (isSettled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle settle transaction status",
                        tint = if (isSettled) com.example.ui.theme.GeometricTealAccent else MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.memberName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSettled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (isSettled) TextDecoration.LineThrough else TextDecoration.None
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(colorText.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                badgeLabel,
                                fontSize = 8.sp,
                                color = colorText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹${"%,.0f".format(tx.amount)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSettled) MaterialTheme.colorScheme.outline else colorText
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(Icons.Default.CalendarToday, contentDescription = "Transaction recorded date", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tx.date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    if (tx.purpose.isNotEmpty()) {
                        Text(
                            text = "Note: ${tx.purpose}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (tx.proofImageUri.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = "Receipt", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Payment Proof Attached", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(120.dp, 80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            LocalProfileImage(
                                photoUri = tx.proofImageUri,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            IconButton(onClick = { onDelete(tx.id) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete ledger item trace",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.65f)
                )
            }
        }
    }
}

// ==================== SECURITY & SETTINGS SCREEN ====================
@Composable
fun SettingsScreen(
    viewModel: BandhamViewModel,
    profileName: String,
    onLock: () -> Unit
) {
    var newPasscode by remember { mutableStateOf("") }
    var changeProfileName by remember { mutableStateOf(profileName) }
    val passcodeState by viewModel.passcode.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked State indicator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Private Isolation Settings",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Bandham is fully localized on your secure cellular storage. Resetting database deletes all information.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f)
                        )
                    }
                }
            }
        }

        // Profile details config
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Vault Title Configuration",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = changeProfileName,
                        onValueChange = { changeProfileName = it },
                        label = { Text("Family Circle Title Name") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.updateProfileName(changeProfileName) }) {
                                Icon(Icons.Default.Save, contentDescription = "Update profile name")
                            }
                        }
                    )
                    Text(
                        "e.g. Vikas Sharma, Gupta Clan or Sunder Residence.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Passcode settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Lock Vault Passcode",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Currently: $passcodeState",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPasscode,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                newPasscode = it
                            }
                        },
                        label = { Text("Enter New 4-Digit Passcode") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newPasscode.length == 4) {
                                        viewModel.setPasscode(newPasscode)
                                        newPasscode = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.LockReset, contentDescription = "Update passcode key")
                            }
                        }
                    )
                    Text(
                        "Enter numeric 4 digits to upgrade device lock passcode. Must be integers.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Educational panel highlighting backend scaling
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Architecture icon info", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Next.js, Next/PWA & Firebase Schema Guidance",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Guidance migrating current on-device storage to Web Progressive Web App (PWA) using Next.js/React & Firebase Firestore:\n\n" +
                                "1. Next.js PWA Config: Enable 'next-pwa' plugin in 'next.config.js' so users can 'Add to Home Screen' via mobile browser manifests.\n" +
                                "2. Private Firestore Structure: Maintain user isolation under Firebase rules:\n" +
                                "   'match /family_members/{id} { allow read, write: if request.auth.uid == resource.data.ownerId; }'\n" +
                                "3. Secure Fields: Always encrypt phone and financial ledger entries natively or restrict indexes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = onLock,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Lock padlock icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock Vault and Seal Vault Database", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==================== DIALOGS ====================

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, rel: String, phone: String, email: String, birthday: String, anniversary: String, notes: String, photoPath: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rel by remember { mutableStateOf("Father") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bday by remember { mutableStateOf("") }
    var anniv by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf("") }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "profile_${System.currentTimeMillis()}.png"
                val file = java.io.File(context.filesDir, fileName)
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                photoUri = file.absolutePath
            } catch (e: Exception) {
                photoUri = uri.toString()
            }
        }
    }

    val relations = listOf(
        "Father", "Mother", "Spouse", "Son", "Daughter", "Brother", "Sister",
        "Uncle", "Aunt", "Cousin", "Grandfather", "Grandmother", "Relative", "Friend"
    )
    var exRelationDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Member / Contact", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile picture picker circle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotEmpty()) {
                            LocalProfileImage(
                                photoUri = photoUri,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Add Profile Picture",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (photoUri.isEmpty()) "Add Profile Photo" else "Change Photo",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("member_name_input")
                )

                // Relation Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = rel,
                        onValueChange = {},
                        label = { Text("Relationship *") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { exRelationDropdown = !exRelationDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = exRelationDropdown,
                        onDismissRequest = { exRelationDropdown = false }
                    ) {
                        relations.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    rel = r
                                    exRelationDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bday,
                    onValueChange = { bday = it },
                    label = { Text("Birthday (YYYY-MM-DD)") },
                    placeholder = { Text("e.g. 1995-10-24") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = anniv,
                    onValueChange = { anniv = it },
                    label = { Text("Wedding Anniversary (YYYY-MM-DD)") },
                    placeholder = { Text("e.g. 2021-11-23") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Private Bond Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, rel, phone, email, bday, anniv, notes, photoUri) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("member_save_button")
            ) {
                Text("Confirm & Secure")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddReminderDialog(
    familyMembers: List<FamilyMember>,
    onDismiss: () -> Unit,
    onSave: (title: String, date: String, type: String, notes: String, memberId: Int?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Birthday") }
    var notes by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf<Int?>(null) }

    val types = listOf("Birthday", "Anniversary", "Reminder", "Festival")
    var exTypeDropdown by remember { mutableStateOf(false) }

    var exMemberDropdown by remember { mutableStateOf(false) }
    val selectedMemberName = familyMembers.find { it.id == selectedMemberId }?.name ?: "No specific relative"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Event Reminder", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD) *") },
                    placeholder = { Text("e.g. 2026-11-23") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Event Type Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        label = { Text("Event Type *") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { exTypeDropdown = !exTypeDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown event type")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = exTypeDropdown,
                        onDismissRequest = { exTypeDropdown = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    type = t
                                    exTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                // Associate relative Link dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedMemberName,
                        onValueChange = {},
                        label = { Text("Link relative") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { exMemberDropdown = !exMemberDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown family member")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = exMemberDropdown,
                        onDismissRequest = { exMemberDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No specific relative") },
                            onClick = {
                                selectedMemberId = null
                                exMemberDropdown = false
                            }
                        )
                        familyMembers.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.name} (${m.relationship})") },
                                onClick = {
                                    selectedMemberId = m.id
                                    exMemberDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special reminders / Gifts") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && date.isNotBlank()) onSave(title, date, type, notes, selectedMemberId) },
                enabled = title.isNotBlank() && date.isNotBlank()
            ) {
                Text("Confirm & Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddTransactionDialog(
    familyMembers: List<FamilyMember>,
    onDismiss: () -> Unit,
    onSave: (name: String, memberId: Int?, amount: Double, type: String, date: String, purpose: String, proofPath: String) -> Unit
) {
    var rawName by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf<Int?>(null) }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("LENT") } // LENT or BORROWED
    var date by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var proofUri by remember { mutableStateOf("") }

    val context = LocalContext.current
    val proofPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "proof_${System.currentTimeMillis()}.png"
                val file = java.io.File(context.filesDir, fileName)
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                proofUri = file.absolutePath
            } catch (e: Exception) {
                proofUri = uri.toString()
            }
        }
    }

    var exMemberDropdown by remember { mutableStateOf(false) }
    val displaySelectedName = if (selectedMemberId == null) {
        if (rawName.isBlank()) "Select or Enter direct name" else rawName
    } else {
        familyMembers.find { it.id == selectedMemberId }?.name ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Money Transaction", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Member Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = displaySelectedName,
                        onValueChange = {
                            rawName = it
                            selectedMemberId = null
                        },
                        label = { Text("Person Name *") },
                        placeholder = { Text("Type name or tap dropdown") },
                        trailingIcon = {
                            IconButton(onClick = { exMemberDropdown = !exMemberDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown ledger links")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = exMemberDropdown,
                        onDismissRequest = { exMemberDropdown = false }
                    ) {
                        familyMembers.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.name} (${m.relationship})") },
                                onClick = {
                                    selectedMemberId = m.id
                                    rawName = m.name
                                    exMemberDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            amountText = it
                        }
                    },
                    label = { Text("Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // LENT vs BORROWED Row buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { type = "LENT" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "LENT") com.example.ui.theme.GeometricTealAccent else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("You Lent (Udhaar)", color = Color.White)
                    }

                    Button(
                        onClick = { type = "BORROWED" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "BORROWED") com.example.ui.theme.GeometricYellowAccent else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("You Borrowed (Karz)", color = Color.White)
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    placeholder = { Text("e.g. 2026-06-13") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose / Festival tag") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment Proof Picker section
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .clickable { proofPickerLauncher.launch("image/*") }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (proofUri.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(120.dp, 80.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            LocalProfileImage(photoUri = proofUri, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Change Proof of Payment Document",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Upload Receipt",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Upload Receipt / Proof of Payment",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            "Secure base64 local vault storage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        },
        confirmButton = {
            val amount = amountText.toDoubleOrNull() ?: 0.0
            val valid = (rawName.isNotBlank() || selectedMemberId != null) && amount > 0.0
            Button(
                onClick = {
                    val finalName = if (selectedMemberId == null) rawName else (familyMembers.find { it.id == selectedMemberId }?.name ?: rawName)
                    val finalDate = if (date.isBlank()) "2026-06-13" else date
                    onSave(finalName, selectedMemberId, amount, type, finalDate, purpose, proofUri)
                },
                enabled = valid
            ) {
                Text("Secure Entry Book")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AuthScreen(viewModel: BandhamViewModel) {
    var authMode by remember { mutableStateOf("EMAIL") } // EMAIL, PHONE, GOOGLE
    var isRegisterMode by remember { mutableStateOf(false) } // for EMAIL: Login vs Signup

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") } // for EMAIL registration

    var phoneNum by remember { mutableStateOf("") }
    var mockOtpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var phoneName by remember { mutableStateOf("") }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }

    // Helpers for Email validation
    val isEmailValid = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Password strength calculation
    val passwordStrength = remember(password) {
        when {
            password.isEmpty() -> "Empty"
            password.length < 6 -> "Weak (min 6 chars)"
            password.length < 10 -> "Fair"
            else -> "Strong"
        }
    }
    val strengthColor = when (passwordStrength) {
        "Empty" -> Color.Gray
        "Weak (min 6 chars)" -> Color.Red
        "Fair" -> Color(0xFFFFA500) // Orange
        else -> com.example.ui.theme.GeometricTealAccent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6750A4), // Deep Purple Primary
                        Color(0xFF21005D)  // Dark Vault Background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Devotional/Relationship Motif Icon
            Icon(
                imageVector = Icons.Default.VpnKey,
                contentDescription = "Key Icon",
                tint = Color(0xFFEADDFF),
                modifier = Modifier
                    .size(60.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "BANDHAM (बंधम)",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your Secure Private Family Vault",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFEADDFF),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Segmented controls for switching auth providers
            TabRow(
                selectedTabIndex = when (authMode) {
                    "EMAIL" -> 0
                    "PHONE" -> 1
                    else -> 2
                },
                containerColor = Color.White.copy(alpha = 0.08f),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[when (authMode) {
                            "EMAIL" -> 0
                            "PHONE" -> 1
                            else -> 2
                        }]),
                        color = Color(0xFFEADDFF)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = authMode == "EMAIL",
                    onClick = { authMode = "EMAIL"; feedbackMessage = null },
                    text = { Text("Email", fontWeight = FontWeight.Bold, color = Color.White) }
                )
                Tab(
                    selected = authMode == "PHONE",
                    onClick = { authMode = "PHONE"; feedbackMessage = null },
                    text = { Text("Phone", fontWeight = FontWeight.Bold, color = Color.White) }
                )
                Tab(
                    selected = authMode == "GOOGLE",
                    onClick = { authMode = "GOOGLE"; feedbackMessage = null },
                    text = { Text("Google", fontWeight = FontWeight.Bold, color = Color.White) }
                )
            }

            // Auth Dialog Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = when (authMode) {
                            "EMAIL" -> if (isRegisterMode) "Create Private Account" else "Sign In"
                            "PHONE" -> "Verify Phone Number"
                            else -> "One-Tap Identity Verification"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    feedbackMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFCE8E6), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFC5221F)
                            )
                        }
                    }

                    // Content switcher depending on selected mode
                    when (authMode) {
                        "EMAIL" -> {
                            if (isRegisterMode) {
                                OutlinedTextField(
                                    value = displayName,
                                    onValueChange = { displayName = it },
                                    label = { Text("Your Name *") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address *") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth()
                            )

                            var passwordVisible by remember { mutableStateOf(false) }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password *") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (password.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Strength: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        Text(passwordStrength, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = strengthColor)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    feedbackMessage = null
                                    if (!isEmailValid) {
                                        feedbackMessage = "Please enter a valid email address (e.g. name@domain.com)."
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        feedbackMessage = "Password is too weak. Must be at least 6 characters."
                                        return@Button
                                    }
                                    if (isRegisterMode) {
                                        if (displayName.isBlank()) {
                                            feedbackMessage = "Please enter your name."
                                            return@Button
                                        }
                                        val result = viewModel.signUpWithEmail(displayName, email, password)
                                        if (result.isFailure) {
                                            feedbackMessage = result.exceptionOrNull()?.message ?: "Account with this email already exists."
                                        }
                                    } else {
                                        val result = viewModel.signInWithEmail(email, password)
                                        if (result.isFailure) {
                                            feedbackMessage = result.exceptionOrNull()?.message ?: "Invalid credentials or account does not exist."
                                        }
                                    }
                                },
                                enabled = email.isNotBlank() && password.isNotBlank() && (!isRegisterMode || displayName.isNotBlank()),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("email_auth_btn")
                            ) {
                                Text(
                                    text = if (isRegisterMode) "Register Vault" else "Open Vault",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            TextButton(
                                onClick = { isRegisterMode = !isRegisterMode; feedbackMessage = null },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = if (isRegisterMode) "Already have an account? Sign In" else "New to Bandham? Link Private Vault",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        "PHONE" -> {
                            if (!mockOtpSent) {
                                OutlinedTextField(
                                    value = phoneName,
                                    onValueChange = { phoneName = it },
                                    label = { Text("Your Name *") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = phoneNum,
                                    onValueChange = { phoneNum = it },
                                    label = { Text("Mobile Number (with +91) *") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                                    placeholder = { Text("e.g. +91 98765-43210") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        feedbackMessage = null
                                        if (phoneName.isBlank()) {
                                            feedbackMessage = "Please enter your name."
                                            return@Button
                                        }
                                        if (phoneNum.length < 10) {
                                            feedbackMessage = "Please enter a valid phone number with country/regional code."
                                            return@Button
                                        }
                                        // Simulate delivery
                                        mockOtpSent = true
                                        feedbackMessage = "SUCCESS: Verification passcode sent to $phoneNum! Use verification code: 829503"
                                    },
                                    enabled = phoneNum.isNotBlank() && phoneName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Send One-Time Passcode (OTP)", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = "Enter the 6-digit OTP passcode dispatched via SMS to secure your session.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { otpCode = it },
                                    label = { Text("Enter OTP Code") },
                                    leadingIcon = { Icon(Icons.Default.Sms, contentDescription = "OTP") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        val result = viewModel.signInWithPhone(phoneNum)
                                        if (result.isSuccess) {
                                            if (phoneName.isNotBlank()) {
                                                viewModel.updateProfileName(phoneName)
                                            }
                                        } else {
                                            feedbackMessage = result.exceptionOrNull()?.message ?: "Validation failed. Connection error."
                                        }
                                    },
                                    enabled = otpCode.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.GeometricTealAccent),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Confirm OTP & Lock In", fontWeight = FontWeight.Bold)
                                }

                                TextButton(
                                    onClick = { mockOtpSent = false; otpCode = ""; feedbackMessage = null },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Change Phone Number", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        "GOOGLE" -> {
                            Text(
                                text = "Sign in quickly and safely using your synchronized on-device Google credentials.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Clean, offical-type Google Button
                            Button(
                                onClick = { showGoogleAccountPicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Custom visual rendering of simple Google colors
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .drawBehind {
                                                drawCircle(color = Color(0xFFEA4335), radius = size.minDimension / 2) // red
                                            }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Sign In with Google", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trust lock security badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Encrypted Lock Badge",
                    tint = Color(0xFFEADDFF).copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Zero-Knowledge Local Encryption. Fully Private.",
                    fontSize = 11.sp,
                    color = Color(0xFFEADDFF).copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Google accounts mockup system picker dialog
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountPicker = false },
            title = { Text("Choose a Google Account", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val users = listOf(
                        "Ramesh Sharma" to "ramesh.sharma@gmail.com",
                        "Sita Patel" to "sita.patel@gmail.com"
                    )
                    users.forEach { (name, mail) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showGoogleAccountPicker = false
                                    viewModel.signInWithGoogle(name, mail, "")
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(mail, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleAccountPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LocalProfileImage(photoUri: String, modifier: Modifier) {
    if (photoUri.isNotEmpty()) {
        val bitmap = remember(photoUri) {
            try {
                if (photoUri.startsWith("/")) {
                    BitmapFactory.decodeFile(photoUri)?.asImageBitmap()
                } else {
                    null
                }
            } catch (e: java.lang.Exception) {
                null
            }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
            return
        }
    }
    // Fallback or default icon
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "No Photo",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.fillMaxSize(0.6f)
        )
    }
}
