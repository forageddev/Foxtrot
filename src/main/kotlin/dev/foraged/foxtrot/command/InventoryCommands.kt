package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.CommandPermission
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

@AutoRegister
object InventoryCommands : GoodCommand()
{
    @CommandAlias("cpto|cp2|copyto")
    @CommandPermission("foxtrot.inventory.management")
    fun copyTo(player: Player, target: Player) {
        if (player == target) throw ConditionFailedException("You cannot copy your inventory to yourself.")

        target.inventory.contents = player.inventory.contents
        target.inventory.armorContents = player.inventory.armorContents
        target.updateInventory()
        player.sendMessage("${CC.SEC}You have copied your inventory contents to ${CC.PRI}${target.name}${CC.SEC}.")
    }

    @CommandAlias("cpfrom|copyfrom")
    @CommandPermission("foxtrot.inventory.management")
    fun copyFrom(player: Player, target: Player) {
        if (player == target) throw ConditionFailedException("You cannot copy your own inventory.")

        player.inventory.contents = target.inventory.contents
        player.inventory.armorContents = target.inventory.armorContents
        player.updateInventory()
        player.sendMessage("${CC.SEC}You have copied the inventory contents of ${CC.PRI}${target.name}${CC.SEC}.")
    }
}