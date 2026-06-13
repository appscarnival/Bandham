package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BandhamDatabase
import com.example.data.model.FamilyMember
import com.example.data.model.EventReminder
import com.example.data.model.MoneyTransaction
import com.example.data.model.LoggedInUser
import com.example.data.model.GmailContact
import com.example.data.model.BackupVersion
import com.example.data.model.EventWorkspace
import com.example.data.model.GuestEntry
import com.example.data.model.VacationTrip
import com.example.data.model.TripMember
import com.example.data.model.TripChecklistItem
import com.example.data.model.TripExpense
import com.example.data.model.TripLog
import com.example.data.model.TripAttachment
import com.example.data.model.CustomGroup
import com.example.data.repository.BandhamRepository
import com.example.ui.Localizations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BandhamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BandhamRepository
    private val sharedPrefs = application.getSharedPreferences("bandham_auth_preferences", Context.MODE_PRIVATE)

    // --- Authentication States ---
    private val _currentUser = MutableStateFlow<LoggedInUser?>(null)
    val currentUser: StateFlow<LoggedInUser?> = _currentUser.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // --- State Streams filtered by logged in user ---
    val familyMembers: StateFlow<List<FamilyMember>>
    val reminders: StateFlow<List<EventReminder>>
    val transactions: StateFlow<List<MoneyTransaction>>

    // --- Google Integration & Backup States ---
    private val _googleAccountConnected = MutableStateFlow(false)
    val googleAccountConnected: StateFlow<Boolean> = _googleAccountConnected.asStateFlow()

    private val _gmailContacts = MutableStateFlow<List<GmailContact>>(emptyList())
    val gmailContacts: StateFlow<List<GmailContact>> = _gmailContacts.asStateFlow()

    private val _isSyncingGmail = MutableStateFlow(false)
    val isSyncingGmail: StateFlow<Boolean> = _isSyncingGmail.asStateFlow()

    private val _backupHistory = MutableStateFlow<List<BackupVersion>>(
        listOf(
            BackupVersion("b1", "13 June 2026, 10:45 PM", isAuto = true, sizeKb = 156, recordsCount = 28),
            BackupVersion("b2", "12 June 2026, 10:45 PM", isAuto = true, sizeKb = 152, recordsCount = 25),
            BackupVersion("b3", "10 June 2026, 06:12 PM", isAuto = false, sizeKb = 148, recordsCount = 24)
        )
    )
    val backupHistory: StateFlow<List<BackupVersion>> = _backupHistory.asStateFlow()

    private val _autoDailyBackup = MutableStateFlow(true)
    val autoDailyBackup: StateFlow<Boolean> = _autoDailyBackup.asStateFlow()

    // --- Expanded Workspaces (Events, Guest pipeline, Vacation planners) ---
    private val _eventsList = MutableStateFlow<List<EventWorkspace>>(emptyList())
    val eventsList: StateFlow<List<EventWorkspace>> = _eventsList.asStateFlow()

    private val _vacationTrips = MutableStateFlow<List<VacationTrip>>(emptyList())
    val vacationTrips: StateFlow<List<VacationTrip>> = _vacationTrips.asStateFlow()

    private val _customGroups = MutableStateFlow<List<CustomGroup>>(emptyList())
    val customGroups: StateFlow<List<CustomGroup>> = _customGroups.asStateFlow()

    // --- Private Lock State ---
    private val _isLocked = MutableStateFlow(false) // Unlocked initially after auth
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _passcode = MutableStateFlow("1234") // Default lock screen PIN
    val passcode: StateFlow<String> = _passcode.asStateFlow()

    init {
        val database = BandhamDatabase.getDatabase(application)
        val dao = database.bandhamDao()
        repository = BandhamRepository(dao)

        // Setup reactive flows mapped strictly by logged-in user ID
        familyMembers = _currentUserId.flatMapLatest { uid ->
            if (!uid.isNullOrEmpty()) {
                repository.getFamilyMembersForUser(uid)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        reminders = _currentUserId.flatMapLatest { uid ->
            if (!uid.isNullOrEmpty()) {
                repository.getRemindersForUser(uid)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        transactions = _currentUserId.flatMapLatest { uid ->
            if (!uid.isNullOrEmpty()) {
                repository.getTransactionsForUser(uid)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Restore previous session if saved
        restoreSession()

        viewModelScope.launch {
            _currentUserId.collect { uid ->
                if (!uid.isNullOrEmpty()) {
                    seedNewFeaturesForUser()
                }
            }
        }
    }

    // --- Session Persistence ---
    private fun restoreSession() {
        // Bypass auth during development by defaulting to Ramesh Sharma
        val uid = sharedPrefs.getString("session_uid", "demo_user")
        val name = sharedPrefs.getString("session_name", "Ramesh Sharma")
        val email = sharedPrefs.getString("session_email", "ramesh@example.com") ?: "ramesh@example.com"
        val phone = sharedPrefs.getString("session_phone", "") ?: ""
        val photo = sharedPrefs.getString("session_photo", "") ?: ""
        val provider = sharedPrefs.getString("session_provider", "DEMO")

        val pin = sharedPrefs.getString("session_passcode", "1234") ?: "1234"
        _passcode.value = pin

        // Load saved localization preference
        val savedLang = sharedPrefs.getString("app_language", "en") ?: "en"
        Localizations.setLanguage(savedLang)

        if (uid != null && name != null && provider != null) {
            val user = LoggedInUser(
                uid = uid,
                displayName = name,
                email = email,
                phoneNumber = phone,
                photoUrl = photo,
                provider = provider
            )
            _currentUser.value = user
            _currentUserId.value = uid
            // PIN lock setting restore
            val requiresPin = sharedPrefs.getBoolean("session_pin_locked", false)
            _isLocked.value = requiresPin

            // If it is our automatically seeded dev bypass profile, ensure details are seeded!
            if (uid == "demo_user") {
                seedIfFresh(uid, name)
            }
        }
    }

    private fun saveSession(user: LoggedInUser) {
        sharedPrefs.edit()
            .putString("session_uid", user.uid)
            .putString("session_name", user.displayName)
            .putString("session_email", user.email)
            .putString("session_phone", user.phoneNumber)
            .putString("session_photo", user.photoUrl)
            .putString("session_provider", user.provider)
            .apply()
    }

    private fun clearSession() {
        sharedPrefs.edit()
            .remove("session_uid")
            .remove("session_name")
            .remove("session_email")
            .remove("session_phone")
            .remove("session_photo")
            .remove("session_provider")
            .apply()
    }

    // --- Real Authentication Flows (backed logically and persistent on device) ---

    fun signInWithEmail(email: String, password: String): Result<LoggedInUser> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty!"))
        }

        val registeredPassword = sharedPrefs.getString("auth_email_pwd_${cleanEmail}", null)
        if (registeredPassword == null) {
            return Result.failure(IllegalArgumentException("No account registered with this email. Create an account first!"))
        }

        if (registeredPassword != password) {
            return Result.failure(IllegalArgumentException("Invalid email or password. Please try again!"))
        }

        val name = sharedPrefs.getString("auth_email_name_${cleanEmail}", "Family User") ?: "Family User"
        val uid = "email_${cleanEmail.hashCode()}"
        val user = LoggedInUser(
            uid = uid,
            displayName = name,
            email = cleanEmail,
            provider = "EMAIL"
        )

        _currentUser.value = user
        _currentUserId.value = uid
        _isLocked.value = false
        saveSession(user)

        // Seed nice initial data if they have absolutely nothing in their vault yet
        seedIfFresh(uid, name)

        return Result.success(user)
    }

    fun signUpWithEmail(name: String, email: String, password: String): Result<LoggedInUser> {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()
        if (cleanName.isEmpty() || cleanEmail.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("All fields are mandatory!"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address!"))
        }

        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password should be at least 6 characters long!"))
        }

        val exists = sharedPrefs.contains("auth_email_pwd_${cleanEmail}")
        if (exists) {
            return Result.failure(IllegalArgumentException("Email already registered! Map a different one or sign in."))
        }

        // Save credentials mapping locally on storage
        sharedPrefs.edit()
            .putString("auth_email_pwd_${cleanEmail}", password)
            .putString("auth_email_name_${cleanEmail}", cleanName)
            .apply()

        val uid = "email_${cleanEmail.hashCode()}"
        val user = LoggedInUser(
            uid = uid,
            displayName = cleanName,
            email = cleanEmail,
            provider = "EMAIL"
        )

        _currentUser.value = user
        _currentUserId.value = uid
        _isLocked.value = false
        saveSession(user)

        // Seed gorgeous custom logs customized to them!
        seedIfFresh(uid, cleanName)

        return Result.success(user)
    }

    fun signInWithPhone(phoneNumber: String): Result<LoggedInUser> {
        val cleanPhone = phoneNumber.trim()
        if (cleanPhone.isEmpty() || cleanPhone.length < 8) {
            return Result.failure(IllegalArgumentException("Please enter a valid phone number!"))
        }

        val uid = "phone_${cleanPhone.hashCode()}"
        val user = LoggedInUser(
            uid = uid,
            displayName = "Phone User (${cleanPhone.takeLast(4)})",
            phoneNumber = cleanPhone,
            provider = "PHONE"
        )

        _currentUser.value = user
        _currentUserId.value = uid
        _isLocked.value = false
        saveSession(user)

        seedIfFresh(uid, user.displayName)

        return Result.success(user)
    }

    fun signInWithGoogle(name: String, email: String, photoUrl: String): Result<LoggedInUser> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) {
            return Result.failure(IllegalArgumentException("Google Email identifier required!"))
        }

        val uid = "google_${cleanEmail.hashCode()}"
        val user = LoggedInUser(
            uid = uid,
            displayName = if (name.isNotEmpty()) name else "Google User",
            email = cleanEmail,
            photoUrl = photoUrl,
            provider = "GOOGLE"
        )

        _currentUser.value = user
        _currentUserId.value = uid
        _isLocked.value = false
        saveSession(user)

        seedIfFresh(uid, user.displayName)

        return Result.success(user)
    }

    fun signOut() {
        clearSession()
        _currentUser.value = null
        _currentUserId.value = null
        _isLocked.value = false
        _eventsList.value = emptyList()
        _vacationTrips.value = emptyList()
        _customGroups.value = emptyList()
    }

    fun updateProfileName(newName: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(displayName = newName)
        _currentUser.value = updated
        saveSession(updated)
    }

    fun changeLanguage(langCode: String) {
        Localizations.setLanguage(langCode)
        sharedPrefs.edit().putString("app_language", langCode).apply()
    }

    // --- Security Pin Actions ---
    fun unlockApp(entered: String): Boolean {
        return if (entered == _passcode.value) {
            _isLocked.value = false
            true
        } else {
            false
        }
    }

    fun lockApp() {
        _isLocked.value = true
        sharedPrefs.edit().putBoolean("session_pin_locked", true).apply()
    }

    fun setPasscode(newPasscode: String) {
        if (newPasscode.length == 4 && newPasscode.all { it.isDigit() }) {
            _passcode.value = newPasscode
            sharedPrefs.edit().putString("session_passcode", newPasscode).apply()
        }
    }

    // --- On-Device Family Actions (Segregated per Logged-In User) ---
    private fun getActiveUid(): String = _currentUserId.value ?: "demo_fallback"

    fun addFamilyMember(member: FamilyMember) {
        viewModelScope.launch {
            repository.insertFamilyMember(member.copy(userId = getActiveUid()))
        }
    }

    fun updateFamilyMember(member: FamilyMember) {
        viewModelScope.launch {
            repository.updateFamilyMember(member.copy(userId = getActiveUid()))
        }
    }

    fun deleteFamilyMember(id: Int) {
        viewModelScope.launch {
            repository.deleteFamilyMemberById(id)
        }
    }

    // --- On-Device Reminder Actions (Segregated per Logged-In User) ---
    fun addReminder(reminder: EventReminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder.copy(userId = getActiveUid()))
        }
    }

    fun toggleReminderCompletion(reminder: EventReminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted, userId = getActiveUid()))
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            repository.deleteReminderById(id)
        }
    }

    // --- On-Device Transaction Actions (Segregated per Logged-In User) ---
    fun addTransaction(transaction: MoneyTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction.copy(userId = getActiveUid()))
        }
    }

    fun toggleTransactionSettle(transaction: MoneyTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction.copy(isSettled = !transaction.isSettled, userId = getActiveUid()))
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    // --- Auto Seeding for high fidelity experience ---
    private fun seedIfFresh(userId: String, name: String) {
        viewModelScope.launch {
            // Check if user already has data. If not, seed initial rich family context
            val testDao = BandhamDatabase.getDatabase(getApplication()).bandhamDao()
            testDao.getFamilyMembersForUser(userId).collect { list ->
                if (list.isEmpty()) {
                    seedInitialDataForUser(userId, name)
                }
            }
        }
    }

    private suspend fun seedInitialDataForUser(userId: String, mainOrganizer: String) {
        val lastName = mainOrganizer.substringAfterLast(" ", "Sharma")
        val members = listOf(
            FamilyMember(name = "Surekha $lastName", relationship = "Mother", birthday = "1968-10-12", email = "surekha@gmail.com", phone = "+91 98765 43210", notes = "Likes dynamic gardening and family meetups", userId = userId),
            FamilyMember(name = "Kailash $lastName", relationship = "Father", birthday = "1963-04-20", phone = "+91 98765 43211", notes = "Likes reading morning scripts and daily yoga", userId = userId),
            FamilyMember(name = "Preeti $lastName", relationship = "Spouse", birthday = "1994-08-15", anniversary = "2020-11-23", email = "preeti@gmail.com", phone = "+91 88765 11223", notes = "Prefers cooking organic healthy meals", userId = userId),
            FamilyMember(name = "Arav $lastName", relationship = "Son", birthday = "2022-05-05", notes = "Enjoys reading colorful illustrated stories", userId = userId)
        )
        members.forEach { repository.insertFamilyMember(it) }

        val items = listOf(
            EventReminder(title = "Mom's Birthday Party", date = "2026-10-12", type = "Birthday", notes = "Order her favourite Kaju Katli online", memberId = 1, userId = userId),
            EventReminder(title = "Water Utility Bill Due", date = "2026-06-28", type = "Reminder", notes = "Pay securely via netbanking", isCompleted = false, userId = userId)
        )
        items.forEach { repository.insertReminder(it) }

        val trans = listOf(
            MoneyTransaction(memberName = "Deepak Kohli", amount = 15000.0, type = "LENT", date = "2026-06-02", purpose = "Sweet boxes bulk arrangement", isSettled = false, userId = userId),
            MoneyTransaction(memberName = "Preeti $lastName", amount = 2500.0, type = "LENT", date = "2026-06-10", purpose = "Purchase fresh daily organic vegetables", isSettled = true, userId = userId)
        )
        trans.forEach { repository.insertTransaction(it) }

        seedNewFeaturesForUser()
    }

    // --- Gmail Contact, Cloud Sync & Backup Actions ---
    fun toggleGoogleConnected() {
        _googleAccountConnected.value = !_googleAccountConnected.value
        if (_googleAccountConnected.value) {
            importGmailContacts()
        } else {
            _gmailContacts.value = emptyList()
        }
    }

    fun importGmailContacts() {
        _isSyncingGmail.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Realistic delay
            _gmailContacts.value = listOf(
                GmailContact("gm1", "Preeti Sharma", "+91 88765 11223", "preeti@gmail.com", "1994-08-15", isMatched = true, isDuplicate = true),
                GmailContact("gm2", "Karan Malhotra", "+91 90001 20001", "karan@gmail.com", "1988-12-05", isMatched = false),
                GmailContact("gm3", "Meera Iyer", "+91 98888 77777", "meera@gmail.com", "1982-03-14", isMatched = false),
                GmailContact("gm4", "Surekha Sharma", "+91 98765 43210", "surekha@gmail.com", "1968-10-12", isMatched = true, isDuplicate = true),
                GmailContact("gm5", "Shankar Sharma", "+91 95432 10987", "shankar@gmail.com", "1960-01-23", isMatched = false)
            )
            _isSyncingGmail.value = false
        }
    }

    fun mergeGmailContact(contactId: String) {
        val list = _gmailContacts.value.toMutableList()
        val index = list.indexOfFirst { it.id == contactId }
        if (index != -1) {
            val contact = list[index]
            // Add as synced family member
            viewModelScope.launch {
                repository.insertFamilyMember(
                    FamilyMember(
                        name = contact.name,
                        relationship = "Relative (Sync)",
                        phone = contact.phone,
                        email = contact.email,
                        birthday = contact.birthday,
                        notes = "Imported and synced from Gmail securely.",
                        userId = getActiveUid()
                    )
                )
            }
            list[index] = contact.copy(isSynced = true, isDuplicate = false, isMatched = true)
            _gmailContacts.value = list
        }
    }

    fun keepSeparateContact(contactId: String) {
        val list = _gmailContacts.value.toMutableList()
        val index = list.indexOfFirst { it.id == contactId }
        if (index != -1) {
            list[index] = list[index].copy(isDuplicate = false)
            _gmailContacts.value = list
        }
    }

    fun setAutoDailyBackup(value: Boolean) {
        _autoDailyBackup.value = value
    }

    fun addNewBackup(isAuto: Boolean) {
        val dateFormat = java.text.SimpleDateFormat("dd MMMM yyyy, hh:mm a", java.util.Locale.ENGLISH)
        val timestamp = dateFormat.format(java.util.Date())
        val newBackup = BackupVersion(
            id = "manual_" + System.currentTimeMillis(),
            timestamp = timestamp,
            isAuto = isAuto,
            sizeKb = (140..170).random(),
            recordsCount = 30
        )
        _backupHistory.value = listOf(newBackup) + _backupHistory.value
    }

    fun restoreFromBackup(versionId: String) {
        // Mock a gorgeous restoration success
        // In a real database this would overwrite DB tables
    }

    // --- Guest Tracker Workspace Actions ---
    fun addEventWorkspace(event: EventWorkspace) {
        _eventsList.value = _eventsList.value + event
    }

    fun updateEventWorkspace(event: EventWorkspace) {
        _eventsList.value = _eventsList.value.map { if (it.id == event.id) event else it }
    }

    fun deleteEventWorkspace(eventId: String) {
        _eventsList.value = _eventsList.value.filterNot { it.id == eventId }
    }

    fun updateGuestStatus(eventId: String, guestId: String, newStatus: String) {
        _eventsList.value = _eventsList.value.map { event ->
            if (event.id == eventId) {
                val updatedGuests = event.guests.map { guest ->
                    if (guest.id == guestId) guest.copy(status = newStatus) else guest
                }
                event.copy(guests = updatedGuests)
            } else {
                event
            }
        }
    }

    fun addGuestToEvent(eventId: String, guest: GuestEntry) {
        _eventsList.value = _eventsList.value.map { event ->
            if (event.id == eventId) {
                event.copy(guests = event.guests + guest)
            } else {
                event
            }
        }
    }

    fun removeGuestFromEvent(eventId: String, guestId: String) {
        _eventsList.value = _eventsList.value.map { event ->
            if (event.id == eventId) {
                event.copy(guests = event.guests.filterNot { it.id == guestId })
            } else {
                event
            }
        }
    }

    // --- Vacation Planner Workspace Actions ---
    fun addVacationTrip(trip: VacationTrip) {
        _vacationTrips.value = _vacationTrips.value + trip
    }

    fun updateVacationTrip(trip: VacationTrip) {
        _vacationTrips.value = _vacationTrips.value.map { if (it.id == trip.id) trip else it }
    }

    fun deleteVacationTrip(tripId: String) {
        _vacationTrips.value = _vacationTrips.value.filterNot { it.id == tripId }
    }

    fun addTripExpense(tripId: String, expense: TripExpense) {
        _vacationTrips.value = _vacationTrips.value.map { trip ->
            if (trip.id == tripId) {
                val updatedExpenses = trip.expenses + expense
                val updatedTimeline = trip.timeline + TripLog(
                    id = "log_" + System.currentTimeMillis(),
                    timestamp = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.ENGLISH).format(java.util.Date()),
                    text = "✓ Expense of ₹${String.format("%.0f", expense.amount)} logs by ${expense.payer}",
                    icon = "payment"
                )
                trip.copy(expenses = updatedExpenses, timeline = updatedTimeline)
            } else {
                trip
            }
        }
    }

    fun toggleTripChecklistItem(tripId: String, itemId: String) {
        _vacationTrips.value = _vacationTrips.value.map { trip ->
            if (trip.id == tripId) {
                val itemToToggle = trip.checklist.find { it.id == itemId }
                val updatedChecklist = trip.checklist.map {
                    if (it.id == itemId) it.copy(isCompleted = !it.isCompleted) else it
                }
                val logText = if (itemToToggle != null) {
                    if (!itemToToggle.isCompleted) "✓ ${itemToToggle.title} completed" else "⚠ ${itemToToggle.title} marked pending"
                } else "✓ Checklist updated"

                val updatedTimeline = trip.timeline + TripLog(
                    id = "log_" + System.currentTimeMillis(),
                    timestamp = "Just Now",
                    text = logText,
                    icon = "check"
                )
                trip.copy(checklist = updatedChecklist, timeline = updatedTimeline)
            } else {
                trip
            }
        }
    }

    fun addTripChecklistItem(tripId: String, item: TripChecklistItem) {
        _vacationTrips.value = _vacationTrips.value.map { trip ->
            if (trip.id == tripId) {
                trip.copy(checklist = trip.checklist + item)
            } else {
                trip
            }
        }
    }

    fun addTripAttachment(tripId: String, attachment: TripAttachment) {
        _vacationTrips.value = _vacationTrips.value.map { trip ->
            if (trip.id == tripId) {
                val updatedAttachments = trip.attachments + attachment
                val updatedTimeline = trip.timeline + TripLog(
                    id = "log_" + System.currentTimeMillis(),
                    timestamp = "Just Now",
                    text = "✓ Attachment '${attachment.name}' uploaded by ${attachment.uploadedBy}",
                    icon = "file"
                )
                trip.copy(attachments = updatedAttachments, timeline = updatedTimeline)
            } else {
                trip
            }
        }
    }

    // --- Seed Expanded Workspaces Data ---
    fun seedNewFeaturesForUser() {
        val currentUserId = getActiveUid()
        val formattedLastName = "Sharma"

        // Seed Event Workspaces
        val preconfiguredEvents = listOf(
            EventWorkspace(
                id = "evt_housewarming",
                title = "Griha Pravesham (Housewarming)",
                date = "2026-07-15",
                type = "Housewarming",
                notes = "Entering our new secure smart-home sanctuary. Grand feast arranged.",
                guests = listOf(
                    GuestEntry("g1", "Surekha $formattedLastName", "Mother", "+91 98765 43210", "Family Core", "Pre-confirmed helper and blessing organizer", "", "CONFIRMED"),
                    GuestEntry("g2", "Kailash $formattedLastName", "Father", "+91 98765 43211", "Family Core", "Pre-confirmed ritual leader", "", "CONFIRMED"),
                    GuestEntry("g3", "Mahesh Babu", "Uncle", "+91 99887 76655", "Hyderabad Family", "Will bring delicious customized sweet boxes", "", "LOCATION_SHARED"),
                    GuestEntry("g4", "Sita Devi", "Aunt", "+91 91234 56789", "Guntur Relatives", "Needs local station escort and train tickets", "", "CALLED"),
                    GuestEntry("g5", "Ravi Kumar", "Cousin", "+91 88776 65544", "Cousins Group", "Will coordinate background sound track play", "", "INVITATION_SENT"),
                    GuestEntry("g6", "Vikram Rathore", "Colleague", "+91 99991 11112", "Office Friends", "Interested in seeing home automations", "", "YET_TO_INVITE"),
                    GuestEntry("g7", "Aradhya Sen", "Friend", "+91 98123 45678", "College Friends", "Already attended primary morning wood blessing", "", "ATTENDED")
                )
            ),
            EventWorkspace(
                id = "evt_birthday",
                title = "Arav's 4th Birthday Bash",
                date = "2026-05-05",
                type = "Birthday Party",
                notes = "Fun balloons & colorful cupcakes at the local neighborhood community park.",
                guests = listOf(
                    GuestEntry("gb1", "Preeti $formattedLastName", "Spouse", "+91 88765 11223", "Core Family", "", "", "CONFIRMED"),
                    GuestEntry("gb2", "Arav $formattedLastName", "Son", "", "Core Family", "The Birthday Hero!", "", "ATTENDED")
                )
            )
        )
        _eventsList.value = preconfiguredEvents

        // Seed Vacation Group Planners
        val preconfiguredVacations = listOf(
            VacationTrip(
                id = "trip_kerala",
                title = "Kerala Backwaters & Hill Tour",
                destination = "Alleppey & Munnar, Kerala",
                notes = "Interactive multi-cousin planning arena with live settlements.",
                members = listOf(
                    TripMember("Ramesh Sharma", "Organizer"),
                    TripMember("Ravi Kumar", "Member"),
                    TripMember("Siva Prasad", "Member"),
                    TripMember("Kumar Varun", "Member")
                ),
                checklist = listOf(
                    TripChecklistItem("c1", "Travel Tickets", true, "Travel Tickets"),
                    TripChecklistItem("c2", "Hotel Booking", true, "Hotel Booking"),
                    TripChecklistItem("c3", "Packing", false, "Packing"),
                    TripChecklistItem("c4", "Passport/Visa", true, "Passport/Visa"),
                    TripChecklistItem("c5", "Shopping", false, "Shopping"),
                    TripChecklistItem("c6", "Activities", true, "Activities"),
                    TripChecklistItem("c7", "Budget Contribution", false, "Budget Contribution"),
                    TripChecklistItem("c8", "Emergency Contacts", true, "Emergency Contacts")
                ),
                expenses = listOf(
                    TripExpense("e1", "Ravi", "Houseboat Advance Booking", 8000.0, listOf("Ravi", "Siva", "Kumar")),
                    TripExpense("e2", "Siva", "Snacks & Local Spices", 2000.0, listOf("Ravi", "Siva", "Kumar")),
                    TripExpense("e3", "Kumar", "Local Taxi Ride", 2000.0, listOf("Ravi", "Siva", "Kumar"))
                ),
                timeline = listOf(
                    TripLog("t1", "13 June 2026, 02:30 PM", "✓ Flights uploaded by Kumar Varun", "flight"),
                    TripLog("t2", "12 June 2026, 11:15 AM", "✓ Houseboat locked by Ravi Kumar", "hotel"),
                    TripLog("t3", "11 June 2026, 06:45 PM", "✓ Tour Activities selected by Ramesh Sharma", "explore")
                ),
                attachments = listOf(
                    TripAttachment("a1", "Alleppey_Houseboat_Invoice.pdf", "PDF", "1.2 MB", "Ravi Kumar"),
                    TripAttachment("a2", "Kochi_Flight_Tickets.pdf", "TICKET", "840 KB", "Kumar Varun"),
                    TripAttachment("a3", "Munnar_Resort_Voucher.png", "IMAGE", "2.1 MB", "Ramesh Sharma")
                )
            )
        )
        _vacationTrips.value = preconfiguredVacations

        val preconfiguredCustomGroups = listOf(
            CustomGroup(
                id = "grp_monthly_puja",
                name = "Monthly Family Satyanarayan Puja",
                description = "Prasad coordination and family spiritual gathering",
                category = "Regular Puja",
                memberNames = listOf("Sadhana Sharma", "Preeti Sharma", "Alok Sharma", "Ramesh Sharma"),
                notes = "Rotates location monthly among members. Bring fresh fruits and flowers."
            ),
            CustomGroup(
                id = "grp_sunday_cricket",
                name = "Sunday Cousin Cricket League",
                description = "Weekly fun sports session & snacks",
                category = "Sports & Meetups",
                memberNames = listOf("Ravi Kumar", "Vikram Rathore", "Cousin Alok", "Ramesh Sharma"),
                notes = "Sunday 7:00 AM at Neighborhood ground. Bat and wickets managed by Vikram."
            )
        )
        _customGroups.value = preconfiguredCustomGroups
    }

    fun addCustomGroup(group: CustomGroup) {
        _customGroups.value = _customGroups.value + group
    }

    fun deleteCustomGroup(groupId: String) {
        _customGroups.value = _customGroups.value.filterNot { it.id == groupId }
    }
}

