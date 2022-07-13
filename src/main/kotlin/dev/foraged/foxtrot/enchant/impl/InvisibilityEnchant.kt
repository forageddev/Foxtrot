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
object InvisibilityEnchant : Enchant("invisiblity", "Invisiblity", ChatColor.AQUA, 3)
{
    override fun canEnchant(item: ItemStack): Boolean {
        return ItemUtils.isArmorEquipment(item.type)
    }

    fun getPotionEffect(level: Int) : PotionEffect {
        return PotionEffect(PotionEffectType.INVISIBILITY,
        when (level) {
            1 -> 10 * 20
            2 -> 30 * 20
            3 -> 60 * 20
            else -> 0
        },0)
    }

    override fun tick(player: Player, level: Int) {
        player.addPotionEffect(getPotionEffect(level), true)
    }
}