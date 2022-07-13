package dev.foraged.foxtrot.enchant.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.enchant.Enchant
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemUtils
import net.evilblock.cubed.util.math.Chance
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack

@Listeners
object LifeStealerEnchant : Enchant("lifestealer", "Life Stealer", ChatColor.RED, 3)
{
    override fun canEnchant(item: ItemStack): Boolean {
        return ItemUtils.isSword(item.type)
    }

    fun shouldSteal(level: Int) : Boolean {
        return Chance.percent(when (level) {
            1 -> 21
            2 -> 45
            3 -> 65
            else -> -1
        })
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (event.entity.killer == null) return
        if (event.entity.killer !is Player) return

        val player = event.entity.killer as Player
        if (player.itemInHand == null) return
        if (!canEnchant(player.itemInHand)) return
        if (getEnchantLevel(player.itemInHand) == -1) return

        if (shouldSteal(getEnchantLevel(player.itemInHand))) {

            player.sendMessage("${CC.B_RED}LIFESTEALER! ${CC.YELLOW}You have stolen ${CC.RED}1${CC.YELLOW} life from ${event.entity.displayName}")
        }
    }
}