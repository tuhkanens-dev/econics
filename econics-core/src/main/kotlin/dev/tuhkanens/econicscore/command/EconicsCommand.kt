package dev.tuhkanens.econicscore.command

import com.mojang.brigadier.exceptions.BuiltInExceptions
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.tuhkanens.econicscore.manager.ConfigManager
import dev.tuhkanens.econicscore.manager.CurrencyManager
import dev.tuhkanens.econicscore.manager.MessagesManager
import io.papermc.paper.command.brigadier.MessageComponentSerializer

class EconicsCommand {

    fun register() {
        commandTree("econics") {
            executes(CommandExecutor { _, _ ->
                throw WrapperCommandSyntaxException(
                    CommandSyntaxException(
                        BuiltInExceptions().literalIncorrect(),
                        MessageComponentSerializer.message().serialize(
                            MessagesManager.getComponent("errors.commands.not-enough-arguments")
                        )
                    )
                )
            })

            literalArgument("reload") {
                withPermission("econics.admin.reload")

                executes(CommandExecutor { sender, _ ->
                    ConfigManager.reload()
                    MessagesManager.reload()
                    CurrencyManager.reload()

                    sender.sendMessage(MessagesManager.getComponent("messages.commands.econics.reload"))
                })
            }
        }
    }

}