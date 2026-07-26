package dev.tuhkanens.econicscore.database.table

import org.jetbrains.exposed.v1.core.Table

object PlayerCurrenciesTable : Table("econics_player_currencies") {
    val player = reference("player_id", PlayersTable)
    val currency = varchar("currency_id", 64)

    val amount = decimal("amount", 32, 3)

    override val primaryKey: PrimaryKey = PrimaryKey(player, currency)
}