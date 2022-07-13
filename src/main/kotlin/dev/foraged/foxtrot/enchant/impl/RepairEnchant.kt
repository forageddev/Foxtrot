package dev.foraged.foxtrot.enchant.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.enchant.Enchant
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.util.bukkit.ItemUtils
import net.evilblock.cubed.util.math.Chance
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack

@Listeners
object RepairEnchant : Enchant("repair", "Repair", ChatColor.YELLOW, 4)
{
    override fun canEnchant(item: ItemStack): Boolean {
        return ItemUtils.isArmorEquipment(item.type) || ItemUtils.isSword(item.type) ||
                item.type.name.contains("AXE") || item.type.name.contains("SHOVEL") ||
                item.type.name.contains("HOE") || item.type == Material.BOW
    }

    fun shouldRepair(level: Int) : Boolean {
        return Chance.percent(when (level) {
            1 -> 25
            2 -> 45
            3 -> 60
            4 -> 80
            else -> -1
        })
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (Chance.percent(50)) return
        if (!EventUtils.hasPlayerMoved(event)) return

        tick(event.player, 0)
    }

    override fun tick(player: Player, level: Int) {
        if (hasEnchant(player.itemInHand)) tick(player, player.itemInHand)
        player.inventory.armorContents.filterNotNull().forEach {
            if (hasEnchant(it)) tick(player, it)
        }
    }

    fun tick(player: Player, item: ItemStack) {
        if (shouldRepair(getEnchantLevel(item))) {
            if (item.durability.toInt() != 0) {
                item.durability = (item.durability - 1).toShort()
                player.itemInHand = item
                player.updateInventory()
            }
        }
    }
}