package dev.foraged.foxtrot.enchant.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.enchant.Enchant
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Listeners
object FireResistanceEnchant : Enchant("fireresistance", "Fire Resistance", ChatColor.RED, 1)
{
    override fun canEnchant(item: ItemStack): Boolean {
        return ItemUtils.isArmorEquipment(item.type)
    }

    override fun tick(player: Player, level: Int) {
        player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0), true)
    }
}