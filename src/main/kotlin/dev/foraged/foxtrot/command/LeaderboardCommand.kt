package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandCompletion
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.annotations.commands.customizer.CommandManagerCustomizer
import dev.foraged.commons.command.CommandManager
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.hologram.FoxtrotHologram
import dev.foraged.foxtrot.hologram.HologramService
import dev.foraged.foxtrot.hologram.impl.DeathsHologram
import dev.foraged.foxtrot.hologram.impl.KillsHologram
import dev.foraged.foxtrot.hologram.impl.KillstreakHologram
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

@AutoRegister
object LeaderboardCommand : GoodCommand() {

    @CommandManagerCustomizer
    fun customizer(manager: CommandManager) {
        manager.commandCompletions.registerCompletion(
            "leaderboards"
        ) {
            HologramService.types
        }
    }

    @CommandAlias("place-leaderboard")
    @CommandPermission("foxtrot.leaderboard.manage")
    @CommandCompletion("@leaderboards")
    fun onPlaceLeaderboard(sender: Player, type: String) {
        val hologram = when (type) {
            "kills" -> KillsHologram(sender.location)
            "deaths" -> DeathsHologram(sender.location)
            "killstreak" -> KillstreakHologram(sender.location)
            else -> null
        } ?: throw ConditionFailedException("There is no hologram with that name.")

        hologram.initialLoad()
        HologramService.registerHologram(hologram)

        sender.sendMessage("${CC.SEC}You have placed the hologram ${CC.PRI}$type${CC.SEC}.")
    }
}