package dev.tuhkanens.econicscore.utils

import com.mojang.brigadier.exceptions.BuiltInExceptions
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException
import dev.tuhkanens.econicscore.manager.MessagesManager
import io.papermc.paper.command.brigadier.MessageComponentSerializer

object CommandUtils {

    fun message(key: String): WrapperCommandSyntaxException {
        return WrapperCommandSyntaxException(
            CommandSyntaxException(
                BuiltInExceptions().literalIncorrect(),
                MessageComponentSerializer.message().serialize(
                    MessagesManager.getComponent(key)
                )
            )
        )
    }

}