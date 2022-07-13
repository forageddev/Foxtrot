package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.CommandHelp
import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.HelpCommand
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.server.ServerHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.Duration
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@CommandAlias("startoftheworld|sotw")
@AutoRegister
object StartOfTheWorldCommand : GoodCommand()
{
    @HelpCommand
    fun help(commandHelp: CommandHelp) {
        commandHelp.showHelp()
    }

    @Subcommand("enable|disable")
    fun enable(player: Player) {
        if (ServerHandler.SOTW_ENABLED.contains(player.uniqueId)) throw ConditionFailedException("You do not have an active start of the world timer.")

        ServerHandler.SOTW_ENABLED.add(player.uniqueId)
        player.sendMessage("${CC.GREEN}Your start of the world timer has been successfully removed.")
    }

    @Subcommand("start|begin|go")
    @CommandPermission("foxtrot.sotw")
    fun start(player: Player, duration: Duration) {
        ServerHandler.SOTW_EXPIRES = System.currentTimeMillis() + duration.get().milliseconds.inWholeMilliseconds
        player.sendMessage("${CC.SEC}You have started the start of the world timer for ${CC.LIGHT_PURPLE}${TimeUtil.formatIntoDetailedString(
            duration.get().seconds.inWholeSeconds.toInt() / 1000
        )}")
    }

    @Subcommand("extend")
    @CommandPermission("foxtrot.sotw")
    fun extend(player: Player, duration: Duration) {
        ServerHandler.SOTW_EXPIRES = ServerHandler.SOTW_EXPIRES + duration.get().milliseconds.inWholeMilliseconds

        player.sendMessage("${CC.SEC}You have extended the start of the world timer by ${CC.LIGHT_PURPLE}${TimeUtil.formatIntoDetailedString(
            duration.get().seconds.inWholeSeconds.toInt() / 1000
        )}")
    }

    @Subcommand("subtract|decrease")
    @CommandPermission("foxtrot.sotw")
    fun subtract(player: Player, duration: Duration) {
        ServerHandler.SOTW_EXPIRES = ServerHandler.SOTW_EXPIRES - duration.get().milliseconds.inWholeMilliseconds

        player.sendMessage("${CC.SEC}You have decreased the start of the world timer by ${CC.LIGHT_PURPLE}${TimeUtil.formatIntoDetailedString(
            duration.get().seconds.inWholeSeconds.toInt() / 1000
        )}")
    }

    @Subcommand("reset-all")
    @CommandPermission("foxtrot.sotw")
    fun all(player: Player) {
        ServerHandler.SOTW_ENABLED = mutableSetOf()
        player.sendMessage("${CC.SEC}You have reset the enabled players set for start of the world.")
    }

    @Subcommand("reset")
    @CommandPermission("foxtrot.sotw")
    fun reset(player: Player, target: Player) {
        ServerHandler.SOTW_ENABLED.remove(target.uniqueId)
        player.sendMessage("${CC.SEC}You have re enabled ${target.displayName}${CC.SEC}'s start of the world timer.")
    }

    @Subcommand("stop|end")
    @CommandPermission("foxtrot.sotw")
    fun stop(player: Player, duration: Duration) {
        ServerHandler.SOTW_EXPIRES = -1
        ServerHandler.SOTW_ENABLED = mutableSetOf()

        player.sendMessage("${CC.SEC}You have stopped the start of the world timer.")
    }
}