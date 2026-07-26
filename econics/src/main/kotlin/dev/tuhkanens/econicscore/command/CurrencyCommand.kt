package dev.tuhkanens.econicscore.command

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.EntitySelectorArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyFileAPI
import dev.tuhkanens.econicsapi.api.PlayerCurrencyAPI
import dev.tuhkanens.econicsapi.data.CurrencyCommands
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.manager.CurrencyManager
import dev.tuhkanens.econicscore.manager.MessagesManager
import dev.tuhkanens.econicscore.utils.CommandUtils
import dev.tuhkanens.econicscore.utils.FoliaUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.math.BigDecimal

object CurrencyCommand {

    private val plugin = Main.plugin
    private val registeredCommands = mutableSetOf<String>()

    fun register() {
        CurrencyManager.getCurrencies().values.forEach { currency ->
            if (currency.commands.enabled) {
                registerCurrency(currency.id)
            }
        }
    }

    private fun registerCurrency(currencyId: String) {
        if (registeredCommands.contains(currencyId)) return

        val currency = CurrencyManager.getCurrencies()[currencyId] ?: return

        val actionSubcommands = CurrencyCommands.entries
            .filter { command ->
                val command = EconicsAPI.getAPI<CurrencyFileAPI>().getCommand(currency.id, command)
                command?.enabled != false
            }
            .map { command -> buildActionCommand(currency.id, command) }
            .toTypedArray()

        CommandAPICommand(currency.id)
            .withSubcommands(*actionSubcommands)
            .executes(CommandExecutor { _, _ ->
                throw CommandUtils.message("errors.commands.not-enough-arguments")
            })
            .register()

        registeredCommands.add(currencyId)

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands)
    }

    private fun unregisterCurrency(currencyId: String) {
        if (!registeredCommands.contains(currencyId)) return

        CommandAPI.unregister(currencyId)
        registeredCommands.remove(currencyId)

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands)
    }

    fun reload() {
        if (FoliaUtils.getLib().isFolia) {
            plugin.logger.info(FoliaUtils.getUnsupportedCommandsMessage())
            return
        }
        registeredCommands.toList().forEach { unregisterCurrency(it) }
        register()
    }

    private fun buildActionCommand(currencyId: String, command: CurrencyCommands): CommandAPICommand {
        return CommandAPICommand(command.name.lowercase()).apply {
            val permission = EconicsAPI.getAPI<CurrencyFileAPI>().getPermission(currencyId, command)

            if (permission?.required == true && permission.value.isNotBlank()) {
                withPermission(permission.value)
            }

            val arguments = mutableListOf<Argument<*>>()
            arguments.add(EntitySelectorArgument.OnePlayer("player"))
            if (command != CurrencyCommands.GET) {
                arguments.add(StringArgument("value"))
            }
            withArguments(arguments)

            executes(CommandExecutor { sender, args ->
                val player = args[0] as OfflinePlayer
                val amount = if (command != CurrencyCommands.GET) {
                    try {
                        BigDecimal(args[1] as String)
                    } catch (_: Exception) {
                        throw CommandUtils.message("errors.commands.incorrect-number-format")
                    }
                } else BigDecimal.ZERO

                execute(sender, player, currencyId, command, amount)
            })
        }
    }

    private fun execute(
        sender: CommandSender,
        player: OfflinePlayer,
        currencyId: String,
        command: CurrencyCommands,
        amount: BigDecimal
    ) {
        val currencyName = EconicsAPI.getAPI<CurrencyFileAPI>().getName(currencyId)
            ?: throw CommandUtils.message("errors.commands.unknown-currency")

        val api = EconicsAPI.getAPI<PlayerCurrencyAPI>()

        val actualAmount: BigDecimal = when (command) {
            CurrencyCommands.ADD -> {
                api.addPlayerCurrency(player.uniqueId, currencyId, amount)
                amount
            }
            CurrencyCommands.REMOVE -> {
                api.removePlayerCurrency(player.uniqueId, currencyId, amount)
                amount
            }
            CurrencyCommands.SET -> {
                api.setPlayerCurrency(player.uniqueId, currencyId, amount)
                amount
            }
            CurrencyCommands.GET -> {
                val result = api.getPlayerCurrency(player.uniqueId, currencyId).join()
                if (result is EconicsResult.GetSuccess) result.data else BigDecimal.ZERO
            }
            CurrencyCommands.PAY -> {
                if (sender !is Player) throw CommandUtils.message("errors.commands.only-player")
                if (sender == player) throw CommandUtils.message("errors.commands.can-not-self")

                val result = api.getPlayerCurrency(sender.uniqueId, currencyId).join()
                val balance = if (result is EconicsResult.GetSuccess) result.data else BigDecimal.ZERO

                if (balance < amount) throw CommandUtils.message("errors.commands.not-enough-currency")

                api.removePlayerCurrency(sender.uniqueId, currencyId, amount)
                api.addPlayerCurrency(player.uniqueId, currencyId, amount)

                amount
            }
        }

        val resolver = TagResolver.resolver(
            Placeholder.parsed("target", player.name ?: "Unknown"),
            Placeholder.parsed("sender", sender.name),
            Placeholder.parsed("amount", EconicsAPI.getAPI<CurrencyFileAPI>().getFormatDecimalPattern(currencyId, actualAmount) ?: actualAmount.toPlainString()),
            Placeholder.parsed("currency_name", currencyName)
        )

        val commandName = command.name.lowercase()

        sender.sendMessage(
            MessagesManager.getComponent(
                "messages.commands.currency.$commandName.sender",
                resolver
            )
        )

        if (sender != player) {
            (player as? Player)?.takeIf { it.isOnline }?.sendMessage(
                MessagesManager.getComponent(
                    "messages.commands.currency.$commandName.target",
                    resolver
                )
            )
        }
    }
}