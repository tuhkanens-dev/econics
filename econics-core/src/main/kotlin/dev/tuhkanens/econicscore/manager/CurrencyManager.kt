package dev.tuhkanens.econicscore.manager

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyAPI
import dev.tuhkanens.econicsapi.data.CurrencyCommands
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.command.CurrencyCommand
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.math.BigDecimal
import java.net.JarURLConnection
import java.util.concurrent.ConcurrentHashMap

object CurrencyManager {

    private val plugin = Main.plugin

    private val files: ConcurrentHashMap<String, File> = ConcurrentHashMap()
    private val currencies: ConcurrentHashMap<String, CurrencyFileData> = ConcurrentHashMap()

    fun load() {
        reload()
    }

    fun reload() {

        currencies.clear()
        files.clear()

        val currenciesFolder = File(plugin.dataFolder, "currencies")
        if (!currenciesFolder.exists()) {
            currenciesFolder.mkdirs()
        }

        copyAllDefaultCurrencies(currenciesFolder)
        loadAllCurrencyFiles(currenciesFolder)
        CurrencyCommand.reload()

    }

    private fun copyAllDefaultCurrencies(targetFolder: File) {
        val resourcePath = "currencies/"

        try {
            val classLoader = this::class.java.classLoader
            val resources = classLoader.getResources(resourcePath).toList()

            for (url in resources) {
                if (url.protocol != "jar") continue

                val connection = url.openConnection() as JarURLConnection
                val jarFile = connection.jarFile
                val entries = jarFile.entries()

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val entryName = entry.name

                    if (entryName.startsWith(resourcePath) &&
                        entryName.endsWith(".yml", ignoreCase = true) &&
                        !entry.isDirectory) {

                        val fileName = entryName.substringAfterLast("/")
                        val targetFile = File(targetFolder, fileName)

                        if (!targetFile.exists()) {
                            classLoader.getResourceAsStream(entryName)?.use { input ->
                                targetFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Could not copy default currencies from JAR: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadAllCurrencyFiles(folder: File) {
        val ymlFiles = folder.listFiles { _, name -> name.endsWith(".yml", ignoreCase = true) }
            ?: emptyArray()

        if (ymlFiles.isEmpty()) {
            plugin.logger.warning("No .yml files found in currencies/ folder!")
            return
        }

        for (file in ymlFiles) {
            val currencyId = file.nameWithoutExtension
            files[currencyId] = file
            loadCurrency(currencyId, file)
        }
    }

    private fun loadCurrency(currencyId: String, file: File) {
        val root = YamlConfigurationLoader.builder()
            .file(file)
            .build()
            .load()

        val currencyNode = root.node("currency")

        val currencyName = currencyNode.node("name").string ?: run {
            plugin.logger.severe("Currency name is not specified in ${file.name}")
            return
        }

        val commands = mutableMapOf<CurrencyCommands, CurrencyFileData.CommandData>()
        val commandsNode = currencyNode.node("commands")

        for (action in CurrencyCommands.entries) {
            val key = action.name.lowercase()
            val actionNode = commandsNode.node(key)

            val enabled = actionNode.node("enabled").getBoolean(false)

            val permissionNode = actionNode.node("permission")

            val value = permissionNode.node("value").string ?: ""
            val required = permissionNode.node("required").getBoolean(false)

            val permission = CurrencyFileData.PermissionData(value, required)

            commands[action] = CurrencyFileData.CommandData(enabled, permission)
        }

        val defaultAmountStr = currencyNode.node("default-amount").string
        val defaultAmount: BigDecimal = if (defaultAmountStr.isNullOrBlank()) {
            plugin.logger.warning("'default-amount' not specified in ${file.name}, using 0")
            BigDecimal.ZERO
        } else {
            try {
                BigDecimal(defaultAmountStr)
            } catch (_: NumberFormatException) {
                plugin.logger.severe("'default-amount' has invalid numeric value '$defaultAmountStr' in ${file.name}")
                return
            }
        }

        val decimalPattern = currencyNode.node("decimal-pattern").string ?: "#.##"

        val localCurrency = currencyNode.node("local-currency").getBoolean(false)

        val currencyFileData = CurrencyFileData(
            id = currencyId,
            name = currencyName,
            defaultAmount = defaultAmount,
            decimalPattern = decimalPattern,
            localCurrency = localCurrency,
            commands = commands
        )

        currencies[currencyId] = currencyFileData

        EconicsAPI.getAPI<CurrencyAPI>().addCurrency(currencyFileData)
    }

    fun getCurrencies(): ConcurrentHashMap<String, CurrencyFileData> = currencies

}