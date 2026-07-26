package dev.tuhkanens.econicscore.command

import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyAPI
import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.manager.ConfigManager
import dev.tuhkanens.econicscore.manager.CurrencyManager
import dev.tuhkanens.econicscore.manager.MessagesManager
import dev.tuhkanens.econicscore.placeholder.EconicsPlaceholder
import dev.tuhkanens.econicscore.utils.CommandUtils
import dev.tuhkanens.econicscore.utils.FoliaUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

class EconicsCommand {

    private val plugin = Main.plugin

    fun register() {
        commandTree("econics") {
            executes(CommandExecutor { _, _ ->
                throw CommandUtils.message("errors.commands.not-enough-arguments")
            })

            literalArgument("reload") {
                withPermission("econics.admin.plugin-reload")

                literalArgument("config") {
                    executes(CommandExecutor { sender, _ ->
                        ConfigManager.reload()

                        sender.sendMessage(MessagesManager.getComponent("messages.commands.econics.reload.config"))
                    })
                }

                literalArgument("messages") {
                    executes(CommandExecutor { sender, _ ->
                        MessagesManager.reload()

                        sender.sendMessage(MessagesManager.getComponent("messages.commands.econics.reload.messages"))
                    })
                }

                literalArgument("currencies") {
                    executes(CommandExecutor { sender, _ ->
                        CurrencyManager.reload()

                        if (FoliaUtils.getLib().isFolia) {
                            plugin.logger.info(FoliaUtils.getUnsupportedCommandsMessage())
                        }

                        sender.sendMessage(MessagesManager.getComponent("messages.commands.econics.reload.currencies"))
                    })
                }

                executes(CommandExecutor { _, _ ->
                    throw CommandUtils.message("errors.commands.not-enough-arguments")
                })
            }

            literalArgument("remove") {
                withPermission("econics.admin.currency-remove")

                executes(CommandExecutor { _, _ ->
                    throw CommandUtils.message("errors.commands.not-enough-arguments")
                })

                argument(
                    StringArgument("currency")
                        .replaceSuggestions { _, builder ->
                            CurrencyManager.getCurrencies().keys.forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                ) {
                    executes(CommandExecutor { _, _ ->
                        throw CommandUtils.message("errors.commands.required-confirm")
                    })

                    literalArgument("confirm") {
                        executes(CommandExecutor { sender, args ->
                            val currencyId = args["currency"] as String
                            val currency = CurrencyManager.getCurrencies()[currencyId]
                                ?: throw CommandUtils.message("errors.commands.unknown-currency")

                            EconicsAPI.getAPI<CurrencyAPI>().removeCurrency(currency.id)

                            if (!FoliaUtils.getLib().isFolia) {
                                CurrencyCommand.reload()
                            }
                            EconicsPlaceholder.reload()

                            sender.sendMessage(
                                MessagesManager.getComponent(
                                    "messages.commands.econics.remove",
                                    Placeholder.parsed("currency_name", currency.name
                                    )
                                )
                            )
                        })
                    }
                }
            }
        }
    }

}