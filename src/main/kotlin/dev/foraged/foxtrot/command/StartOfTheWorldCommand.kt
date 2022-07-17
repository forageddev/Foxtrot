package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.CommandHelp
import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.*
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.server.MapService
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
    @Description("Disable your start of the world protection")
    fun enable(player: Player) {
        if (MapService.SOTW_ENABLED.contains(player.uniqueId)) throw ConditionFailedException("You do not have an active start of the world timer.")

        MapService.SOTW_ENABLED.add(player.uniqueId)
        player.sendMessage("${CC.GREEN}Your start of the world timer has been successfully removed.")
    }

    @Subcommand("start|begin|go")
    @CommandPermission("foxtrot.sotw")
    @Description("Start the start of the world protection timer")
    fun start(player: Player, duration: Duration) {
        MapService.SOTW_EXPIRES = System.currentTimeMillis() + duration.get().milliseconds.inWholeMilliseconds
        player.sendMessage("${CC.SEC}You have started the start of the world timer for ${CC.LIGHT_PURPLE}${TimeUtil.formatIntoDetailedString(
            duration.get().seconds.inWholeSeconds.toInt() / 1000
        )}")
    }

    @Subcommand("extend")
    @CommandPermission("foxtrot.sotw")
    @Description("Extend the start of the world protection timer")
    fun extend(player: Player, duration: Duration) {
        MapService.SOTW_EXPIRES = MapService.SOTW_EXPIRES + duration.get().milliseconds.inWholeMilliseconds

        player.sendMessage("${CC.SEC}You have extended the start of the world timer by ${CC.LIGHT_PURPLE}${TimeUtil.formatIntoDetailedString(
            duration.get().seconds.inWholeSeconds.toInt() / 1000
        )}")
    }

    @Subcommand("subtract|decrease")
    @CommandPermission("foxtrot.sotw")
    @Description("Decrease the start of the world protection timer")
    fun subtract(player: Player, duration: Duration) {
        MapService.SOTW_EXPIRES = MapService.SOTW_EXPIRES - duration.get().milliseconds.inWholeMilliseconds

        player.sendMessage("${CC.SEC}You have decreased the start of the world timer by ${CC.LIGHT_PURPLE}${TimeUtil.formatIntoDetailedString(
            duration.get().seconds.inWholeSeconds.toInt() / 1000
        )}")
    }

    @Subcommand("reset-all")
    @CommandPermission("foxtrot.sotw")
    @Description("Reset start of the world protection timer for everyone")
    fun all(player: Player) {
        MapService.SOTW_ENABLED = mutableSetOf()
        player.sendMessage("${CC.SEC}You have reset the enabled players set for start of the world.")
    }

    @Subcommand("reset")
    @CommandPermission("foxtrot.sotw")
    @Description("Reset start of the world protection timer for a target")
    fun reset(player: Player, target: Player) {
        MapService.SOTW_ENABLED.remove(target.uniqueId)
        player.sendMessage("${CC.SEC}You have re enabled ${target.displayName}${CC.SEC}'s start of the world timer.")
    }

    @Subcommand("stop|end")
    @CommandPermission("foxtrot.sotw")
    @Description("Stop the start of the world protection timer")
    fun stop(player: Player) {
        MapService.SOTW_EXPIRES = -1
        MapService.SOTW_ENABLED = mutableSetOf()

        player.sendMessage("${CC.SEC}You have stopped the start of the world timer.")
    }
}