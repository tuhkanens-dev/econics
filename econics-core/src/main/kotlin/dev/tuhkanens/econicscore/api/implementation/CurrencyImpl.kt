package dev.tuhkanens.econicscore.api.implementation

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyAPI
import dev.tuhkanens.econicsapi.api.CurrencyFileAPI
import dev.tuhkanens.econicsapi.data.CurrencyCommands
import dev.tuhkanens.econicsapi.data.CurrencyData
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.command.CurrencyCommand
import dev.tuhkanens.econicscore.database.table.CurrenciesTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Files

class CurrencyImpl : CurrencyAPI {

    private val plugin = Main.plugin
    private val api = EconicsAPI.getAPI<CurrencyFileAPI>()

    override fun addCurrency(currencyData: CurrencyFileData): EconicsResult<Nothing> {
        return try {
            transaction(api.getDatabase(currencyData.id)) {
                val exists = CurrenciesTable.selectAll()
                    .where { CurrenciesTable.id eq currencyData.id }
                    .singleOrNull() != null

                if (exists) {
                    return@transaction EconicsResult.Already
                }

                CurrenciesTable.insert {
                    it[CurrenciesTable.id] = currencyData.id
                    it[CurrenciesTable.name] = currencyData.name
                    it[CurrenciesTable.defaultAmount] = currencyData.defaultAmount
                    it[CurrenciesTable.decimalPattern] = currencyData.decimalPattern
                }

                addFileCurrency(currencyData)
                CurrencyCommand.reload()
                EconicsResult.Success
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    private fun addFileCurrency(currencyData: CurrencyFileData) {
        val currencyId = currencyData.id
        val currencyFile = getCurrencyFile(currencyId)

        currencyFile.parentFile.mkdirs()
        if (currencyFile.exists()) return

        val loader = YamlConfigurationLoader.builder()
            .file(currencyFile)
            .build()

        val root = loader.createNode()
        val currencyNode = root.node("currency")

        currencyNode.node("name").set(currencyData.name)
        currencyNode.node("default-amount").set(currencyData.defaultAmount.toPlainString())
        currencyNode.node("decimal-pattern").set(currencyData.decimalPattern)
        currencyNode.node("local-currency").set(currencyData.localCurrency)

        val commandsNode = currencyNode.node("commands")

        CurrencyCommands.entries.forEach { action ->
            val actionName = action.name.lowercase()
            val commandNode = commandsNode.node(actionName)

            val commandData = currencyData.commands[action]

            commandNode.node("enabled").set(commandData?.enabled ?: true)

            val permNode = commandNode.node("permission")
            permNode.node("value").set(
                commandData?.permission?.value ?: "econics.$currencyId.$actionName"
            )
            permNode.node("required").set(
                commandData?.permission?.required ?: true
            )
        }

        loader.save(root)
    }

    override fun removeCurrency(currencyId: String): EconicsResult<Nothing> {
        return try {
            transaction(api.getDatabase(currencyId)) {
                val deleted = CurrenciesTable.deleteWhere { CurrenciesTable.id eq currencyId }
                if (deleted > 0) {
                    removeFileCurrency(currencyId)
                    CurrencyCommand.reload()
                    EconicsResult.Success
                } else EconicsResult.NotFound
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    private fun removeFileCurrency(currencyId: String) {
        try {
            Files.deleteIfExists(getCurrencyFile(currencyId).toPath())
        } catch (e: Exception) {
            plugin.logger.severe("Failed to delete currency $currencyId: ${e.message}")
        }
    }

    private fun getCurrencyFile(currencyId: String): File {
        return plugin.dataFolder.toPath()
            .resolve("currencies")
            .resolve("$currencyId.yml")
            .normalize()
            .toFile()
    }

    override fun getCurrency(currencyId: String): EconicsResult<CurrencyData> {
        return try {
            transaction(api.getDatabase(currencyId)) {
                val currencyRow = CurrenciesTable.selectAll()
                    .where { CurrenciesTable.id eq currencyId }
                    .singleOrNull() ?: return@transaction EconicsResult.NotFound

                val currency = CurrencyData(
                    id = currencyRow[CurrenciesTable.id],
                    name = currencyRow[CurrenciesTable.name],
                    defaultAmount = currencyRow[CurrenciesTable.defaultAmount],
                    decimalPattern = currencyRow[CurrenciesTable.decimalPattern],
                )

                EconicsResult.GetSuccess(currency)
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun hasCurrency(currencyId: String): EconicsResult<Nothing> {
        return try {
            transaction(api.getDatabase(currencyId)) {
                val exists = CurrenciesTable.selectAll()
                    .where { CurrenciesTable.id eq currencyId }
                    .singleOrNull() != null

                if (exists) EconicsResult.Success else EconicsResult.NotFound
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

}