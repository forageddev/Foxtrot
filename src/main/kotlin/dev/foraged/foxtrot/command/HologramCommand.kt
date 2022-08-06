package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.hologram.HologramService
import dev.foraged.foxtrot.hologram.impl.DeathsHologram
import dev.foraged.foxtrot.hologram.impl.KillsHologram
import dev.foraged.foxtrot.hologram.impl.KillstreakHologram
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

@CommandAlias("foxtrot-hologram|foxtrot-holo")
@CommandPermission("foxtrot.hologram.manage")
@AutoRegister
object HologramCommand : GoodCommand() {

    @Subcommand("create-kills")
    fun kills(sender: Player, name: String) {
        HologramService.registerHologram(KillsHologram(sender.location))
        sender.sendMessage("${CC.SEC}You have created a hologram ${CC.PRI}$name${CC.SEC}.")
    }

    @Subcommand("create-deaths")
    fun deaths(sender: Player, name: String) {
        HologramService.registerHologram(DeathsHologram(sender.location))
        sender.sendMessage("${CC.SEC}You have created a hologram ${CC.PRI}$name${CC.SEC}.")
    }

    @Subcommand("create-killstreak")
    fun create(sender: Player, name: String) {
        HologramService.registerHologram(KillstreakHologram(sender.location))
        sender.sendMessage("${CC.SEC}You have created a hologram ${CC.PRI}$name${CC.SEC}.")
    }
}