package app.wesplit.domain.export

import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.toInstant
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.Participant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Single

@Single
class CsvExportUseCase {
    /**
     * Generates CSV content from expenses.
     * 
     * @param group The group containing participants
     * @param expenses List of expenses to export (including settled ones)
     * @param includeShares Whether to include share information showing what each person owes
     * @return CSV content as string
     */
    fun generateCsv(
        group: Group,
        expenses: List<Expense>,
        includeShares: Boolean,
    ): String {
        val csvBuilder = StringBuilder()
        
        // Get all unique participants from the group
        val allParticipants = group.participants.sortedBy { it.name }
        
        // Add header
        if (includeShares) {
            val participantHeaders = allParticipants.joinToString(",") { escapeCsvValue(it.name) }
            csvBuilder.appendLine("Date,Title,Amount,Currency,Paid By,Category,Status,$participantHeaders")
        } else {
            csvBuilder.appendLine("Date,Title,Amount,Currency,Paid By,Category,Status")
        }
        
        // Add expense rows
        expenses.forEach { expense ->
            val date = expense.date.toInstant().toLocalDateTime(TimeZone.currentSystemDefault())
            val dateStr = "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
            val title = escapeCsvValue(expense.title)
            val amount = expense.totalAmount.value
            val currency = expense.totalAmount.currencyCode
            val paidBy = escapeCsvValue(expense.payedBy.name)
            val category = expense.category.name
            val status = expense.status.name
            
            if (includeShares) {
                // Create a map of participant ID to share amount
                val shareMap = expense.shares.associate { it.participant.id to it.amount.value }
                
                // Create row with share amounts for each participant
                val participantShares = allParticipants.joinToString(",") { participant ->
                    shareMap[participant.id]?.toString() ?: "0.0"
                }
                
                csvBuilder.appendLine("$dateStr,$title,$amount,$currency,$paidBy,$category,$status,$participantShares")
            } else {
                // Simple export without share details
                csvBuilder.appendLine("$dateStr,$title,$amount,$currency,$paidBy,$category,$status")
            }
        }
        
        return csvBuilder.toString()
    }
    
    /**
     * Escapes CSV values that contain special characters (comma, quotes, newlines).
     */
    private fun escapeCsvValue(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}

