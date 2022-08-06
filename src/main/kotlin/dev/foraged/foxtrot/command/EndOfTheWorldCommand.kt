package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.CommandHelp
import dev.foraged.commons.acf.annotation.*
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.server.MapService
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.Duration
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@CommandAlias("endoftheworld|eotw")
@AutoRegister
object EndOfTheWorldCommand : GoodCommand()
{
    @HelpCommand
    fun help(commandHelp: CommandHelp) {
        commandHelp.showHelp()
    }

    @Subcommand("pre")
    @CommandPermission("foxtrot.eotw")
    @Description("Start the end of the world pre timer")
    fun start(player: Player, duration: Duration) {
        MapService.EOTW_STARTS = System.currentTimeMillis() + duration.get().milliseconds.inWholeMilliseconds
        player.sendMessage("${CC.SEC}You have started the end of the world timer for ${CC.LIGHT_PURPLE}${TimeUtil.formatIntoDetailedString(
            duration.get().seconds.inWholeSeconds.toInt() / 1000
        )}")

        Bukkit.getServer().onlinePlayers.forEach {
            it.playSound(it.location, Sound.WITHER_SPAWN, 1F, 1F)
        }
        Bukkit.broadcastMessage("${CC.RED}███████")
        Bukkit.broadcastMessage("${CC.RED}█${CC.D_RED}█████${CC.RED}█ ${CC.D_RED}[Pre-EOTW]")
        Bukkit.broadcastMessage("${CC.RED}█${CC.D_RED}█${CC.RED}█████ ${CC.B_RED}EOTW is about to commence.")
        Bukkit.broadcastMessage("${CC.RED}█${CC.D_RED}████${CC.RED}██ PvP Protection is disabled.")
        Bukkit.broadcastMessage("${CC.RED}█${CC.D_RED}█${CC.RED}█████ All players have been un-deathbanned.")
        Bukkit.broadcastMessage("${CC.RED}█${CC.D_RED}█████${CC.RED}█ All deathbans are now permanent.")
        Bukkit.broadcastMessage("${CC.RED}███████")
    }

}