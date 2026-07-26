package dev.tuhkanens.econicscore.manager

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyAPI
import dev.tuhkanens.econicsapi.api.PlayerAPI
import dev.tuhkanens.econicsapi.api.PlayerCurrencyAPI
import dev.tuhkanens.econicsapi.data.CurrencyCommandData
import dev.tuhkanens.econicsapi.data.CurrencyCommands
import dev.tuhkanens.econicsapi.data.CurrencyCommandsData
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import dev.tuhkanens.econicsapi.data.CurrencyPermissionData
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.command.CurrencyCommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val currenciesFolder = File(plugin.dataFolder, "currencies")
            if (!currenciesFolder.exists()) currenciesFolder.mkdirs()

            copyAllDefaultCurrencies(currenciesFolder)

            val newFiles = mutableMapOf<String, File>()
            val newCurrencies = mutableMapOf<String, CurrencyFileData>()

            val ymlFiles = currenciesFolder.listFiles { _, name ->
                name.endsWith(".yml", ignoreCase = true)
            }
            ymlFiles?.forEach { file ->
                val id = file.nameWithoutExtension
                val data = loadCurrency(id, file)

                newFiles[id] = file
                newCurrencies[id] = data
            }

            Bukkit.getScheduler().runTask(plugin, Runnable {
                files.clear()
                files.putAll(newFiles)

                currencies.clear()
                currencies.putAll(newCurrencies)

                newCurrencies.values.forEach { EconicsAPI.getAPI<CurrencyAPI>().addCurrency(it) }

                CurrencyCommand.reload()

                Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
                    setPlayerDefaultCurrencies(onlinePlayer)
                }
            })
        })

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

    private fun loadCurrency(currencyId: String, file: File): CurrencyFileData {
        val root = YamlConfigurationLoader.builder()
            .file(file)
            .build()
            .load()

        val currencyNode = root.node("currency")

        val currencyName = currencyNode.node("name").getString("Currency")

        val commands = mutableMapOf<CurrencyCommands, CurrencyCommandData>()

        val commandsNode = currencyNode.node("commands")
        val commandsEnabled = commandsNode.node("enabled").getBoolean(true)

        for (command in CurrencyCommands.entries) {
            val key = command.name.lowercase()
            val commandNode = commandsNode.node("commands").node(key)

            val enabled = commandNode.node("enabled").getBoolean(false)

            val permissionNode = commandNode.node("permission")

            val value = permissionNode.node("value").string ?: ""
            val required = permissionNode.node("required").getBoolean(false)

            val permission = CurrencyPermissionData(value, required)

            commands[command] = CurrencyCommandData(enabled, permission)
        }

        val commandsData = CurrencyCommandsData(commandsEnabled, commands)

        val defaultAmountStr = currencyNode.node("default-amount").string
        val defaultAmount: BigDecimal = if (defaultAmountStr.isNullOrBlank()) {
            plugin.logger.warning("'default-amount' not specified in ${file.name}, using 0")
            BigDecimal.ZERO
        } else {
            try {
                BigDecimal(defaultAmountStr)
            } catch (_: NumberFormatException) {
                plugin.logger.severe("'default-amount' has invalid numeric value '$defaultAmountStr' in ${file.name}")
                BigDecimal.ZERO
            }
        }

        val decimalPattern = currencyNode.node("decimal-pattern").string ?: "#.##"

        val localCurrency = currencyNode.node("local-currency").getBoolean(false)

        return CurrencyFileData(
            id = currencyId,
            name = currencyName,
            defaultAmount = defaultAmount,
            decimalPattern = decimalPattern,
            localCurrency = localCurrency,
            commands = commandsData
        )
    }

    fun setPlayerDefaultCurrencies(player: Player) {
        val uuid = player.uniqueId

        val playerAPI = EconicsAPI.getAPI<PlayerAPI>()
        val currencyAPI = EconicsAPI.getAPI<PlayerCurrencyAPI>()

        when (val result = playerAPI.ensurePlayer(uuid, player.name)) {
            is EconicsResult.Success, is EconicsResult.Already -> {
                getCurrencies().forEach { (currencyId, data) ->
                    if (currencyAPI.hasPlayerCurrency(uuid, currencyId) is EconicsResult.NotFound) {
                        currencyAPI.setPlayerCurrency(uuid, currencyId, data.defaultAmount)
                    }
                }
            }
            is EconicsResult.Failure -> plugin.logger.severe(result.error)
            else -> {}
        }
    }

    fun getCurrencies(): ConcurrentHashMap<String, CurrencyFileData> = currencies

}