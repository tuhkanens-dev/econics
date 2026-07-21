package dev.tuhkanens.econicscore.database.table

import org.jetbrains.exposed.v1.core.Table

object CurrenciesTable : Table("currencies") {
    val id = varchar("currency_id", 32).uniqueIndex()
    val name = varchar("currency_name", 64)
    val defaultAmount = decimal("default_amount", 32, 3)
    val decimalPattern = varchar("decimal_pattern", 32)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}