package dev.tuhkanens.econicscore.command

import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyAPI
import dev.tuhkanens.econicscore.manager.ConfigManager
import dev.tuhkanens.econicscore.manager.CurrencyManager
import dev.tuhkanens.econicscore.manager.MessagesManager
import dev.tuhkanens.econicscore.placeholder.EconicsPlaceholderExpansion
import dev.tuhkanens.econicscore.utils.CommandUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

class EconicsCommand {

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
                        EconicsPlaceholderExpansion.registerPlaceholders()

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

                CurrencyManager.getCurrencies().values.forEach { currency ->
                    literalArgument(currency.id) {
                        executes(CommandExecutor { _, _ ->
                            throw CommandUtils.message("errors.commands.required-confirm")
                        })

                        literalArgument("confirm") {
                            executes(CommandExecutor { sender, _ ->
                                EconicsAPI.getAPI<CurrencyAPI>().removeCurrency(currency.id)

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

}