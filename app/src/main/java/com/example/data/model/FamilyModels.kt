package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val relationship: String, // e.g. Father, Mother, Spouse, Brother, Son, Friend
    val phone: String = "",
    val email: String = "",
    val birthday: String = "", // YYYY-MM-DD
    val anniversary: String = "", // YYYY-MM-DD
    val notes: String = "",
    val photoUri: String = "", // Profile photo mock/local path
    val userId: String = "" // Added for secure user segregation
)

@Entity(tableName = "event_reminders")
data class EventReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String, // YYYY-MM-DD
    val type: String, // "Birthday", "Anniversary", "Custom", "Reminder"
    val notes: String = "",
    val isCompleted: Boolean = false,
    val memberId: Int? = null, // Associated family member if any
    val userId: String = "" // Added for secure user segregation
)

@Entity(tableName = "money_transactions")
data class MoneyTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberName: String,
    val memberId: Int? = null, // Associated family member if any
    val amount: Double,
    val type: String, // "LENT" (Given - they owe me) or "BORROWED" (Owed - I owe them)
    val date: String, // YYYY-MM-DD
    val purpose: String = "",
    val isSettled: Boolean = false,
    val userId: String = "", // Added for secure user segregation
    val proofImageUri: String = "" // Added for payment proof image storage/Firebase mock
)

data class LoggedInUser(
    val uid: String,
    val displayName: String,
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val provider: String // "GOOGLE", "PHONE", "EMAIL"
)
