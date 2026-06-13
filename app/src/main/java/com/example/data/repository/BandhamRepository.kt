package com.example.data.repository

import com.example.data.local.BandhamDao
import com.example.data.model.FamilyMember
import com.example.data.model.EventReminder
import com.example.data.model.MoneyTransaction
import kotlinx.coroutines.flow.Flow

class BandhamRepository(private val bandhamDao: BandhamDao) {

    // --- Family Member Repository Calls ---
    val allFamilyMembers: Flow<List<FamilyMember>> = bandhamDao.getAllFamilyMembers()

    fun getFamilyMembersForUser(userId: String): Flow<List<FamilyMember>> {
        return bandhamDao.getFamilyMembersForUser(userId)
    }

    suspend fun getFamilyMemberById(id: Int): FamilyMember? {
        return bandhamDao.getFamilyMemberById(id)
    }

    suspend fun insertFamilyMember(member: FamilyMember): Long {
        return bandhamDao.insertFamilyMember(member)
    }

    suspend fun updateFamilyMember(member: FamilyMember) {
        bandhamDao.updateFamilyMember(member)
    }

    suspend fun deleteFamilyMemberById(id: Int) {
        bandhamDao.deleteFamilyMemberById(id)
    }

    // --- Event Reminders Repository Calls ---
    val allReminders: Flow<List<EventReminder>> = bandhamDao.getAllReminders()

    fun getRemindersForUser(userId: String): Flow<List<EventReminder>> {
        return bandhamDao.getRemindersForUser(userId)
    }

    suspend fun insertReminder(reminder: EventReminder): Long {
        return bandhamDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: EventReminder) {
        bandhamDao.updateReminder(reminder)
    }

    suspend fun deleteReminderById(id: Int) {
        bandhamDao.deleteReminderById(id)
    }

    // --- Money Transactions Repository Calls ---
    val allTransactions: Flow<List<MoneyTransaction>> = bandhamDao.getAllTransactions()

    fun getTransactionsForUser(userId: String): Flow<List<MoneyTransaction>> {
        return bandhamDao.getTransactionsForUser(userId)
    }

    suspend fun insertTransaction(transaction: MoneyTransaction): Long {
        return bandhamDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: MoneyTransaction) {
        bandhamDao.updateTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        bandhamDao.deleteTransactionById(id)
    }
}
