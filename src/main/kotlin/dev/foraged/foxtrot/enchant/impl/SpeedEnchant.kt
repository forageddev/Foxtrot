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
object SpeedEnchant : Enchant("speed", "Speed", ChatColor.AQUA, 2)
{
    override fun canEnchant(item: ItemStack): Boolean {
        return item.type.name.contains("BOOTS")
    }

    fun getPotionEffect(level: Int) : PotionEffect {
        return PotionEffect(PotionEffectType.SPEED,
        40,
        level - 1)
    }

    override fun tick(player: Player, level: Int) {
        player.addPotionEffect(getPotionEffect(level), true)
    }
}