package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FamilyMember
import com.example.data.model.EventReminder
import com.example.data.model.MoneyTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BandhamDao {
    // --- Family Members ---
    @Query("SELECT * FROM family_members WHERE userId = :userId ORDER BY name ASC")
    fun getFamilyMembersForUser(userId: String): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members ORDER BY name ASC")
    fun getAllFamilyMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getFamilyMemberById(id: Int): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMember): Long

    @Update
    suspend fun updateFamilyMember(member: FamilyMember)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteFamilyMemberById(id: Int)

    // --- Event Reminders ---
    @Query("SELECT * FROM event_reminders WHERE userId = :userId ORDER BY date ASC")
    fun getRemindersForUser(userId: String): Flow<List<EventReminder>>

    @Query("SELECT * FROM event_reminders ORDER BY date ASC")
    fun getAllReminders(): Flow<List<EventReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: EventReminder): Long

    @Update
    suspend fun updateReminder(reminder: EventReminder)

    @Query("DELETE FROM event_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)

    // --- Money Transactions ---
    @Query("SELECT * FROM money_transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsForUser(userId: String): Flow<List<MoneyTransaction>>

    @Query("SELECT * FROM money_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<MoneyTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MoneyTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: MoneyTransaction)

    @Query("DELETE FROM money_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)
}
