package dev.foraged.foxtrot.enchant.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.enchant.Enchant
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Listeners
object RecoverEnchant : Enchant("recover", "Recover", ChatColor.GOLD, 1)
{
    override fun canEnchant(item: ItemStack): Boolean {
        return item.type.name.contains("CHESTPLATE")
    }

    @EventHandler
    fun onKill(event: PlayerDeathEvent) {
        if (event.entity.killer != null) {
            val player = event.entity.killer!!
            if (player.inventory.chestplate != null && hasEnchant(player.inventory.chestplate)) tick(player, 1)
        }
    }

    override fun tick(player: Player, level: Int) {
        player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 10 * 60, 3), true)
    }
}