package com.example.data.model

data class GmailContact(
    val id: String,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val birthday: String = "",
    val photoUrl: String = "",
    val isMatched: Boolean = false,
    val matchedMemberId: Int? = null,
    val isDuplicate: Boolean = false,
    val isSynced: Boolean = true
)

data class BackupVersion(
    val id: String,
    val timestamp: String, // e.g. "13 June 2026, 10:45 PM"
    val isAuto: Boolean,
    val sizeKb: Int,
    val recordsCount: Int
)

data class EventWorkspace(
    val id: String,
    val title: String,
    val date: String, // YYYY-MM-DD
    val type: String, // "Housewarming", "Wedding", "Birthday Party" etc
    val guests: List<GuestEntry> = emptyList(),
    val notes: String = ""
)

data class GuestEntry(
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String,
    val familyGroup: String = "",
    val notes: String = "",
    val photoUrl: String = "",
    val status: String // "YET_TO_INVITE", "CALLED", "INVITATION_SENT", "LOCATION_SHARED", "CONFIRMED", "ATTENDED"
)

data class VacationTrip(
    val id: String,
    val title: String,
    val destination: String,
    val notes: String = "",
    val members: List<TripMember> = emptyList(),
    val checklist: List<TripChecklistItem> = emptyList(),
    val expenses: List<TripExpense> = emptyList(),
    val timeline: List<TripLog> = emptyList(),
    val attachments: List<TripAttachment> = emptyList()
)

data class TripMember(
    val name: String,
    val role: String // "Organizer", "Member", "Viewer"
)

data class TripChecklistItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val category: String // "Travel Tickets", "Hotel Booking", "Packing", "Passport/Visa", "Shopping", "Activities", "Budget Contribution", "Emergency Contacts"
)

data class TripExpense(
    val id: String,
    val payer: String,
    val description: String,
    val amount: Double,
    val splitWith: List<String> = emptyList() // List of member names who share this expense
)

data class TripLog(
    val id: String,
    val timestamp: String,
    val text: String,
    val icon: String = "info"
)

data class TripAttachment(
    val id: String,
    val name: String,
    val fileType: String, // "PDF", "IMAGE", "TICKET"
    val size: String,
    val uploadedBy: String
)

data class CustomGroup(
    val id: String,
    val name: String,
    val description: String,
    val category: String, // e.g., "Family Meetup", "Regular Puja", "Festivals", "General"
    val memberNames: List<String> = emptyList(),
    val notes: String = ""
)

