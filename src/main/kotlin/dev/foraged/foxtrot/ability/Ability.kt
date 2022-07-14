package dev.foraged.foxtrot.ability

import dev.foraged.commons.persist.CooldownMap
import dev.foraged.foxtrot.map.cooldown.nopersist.AbilityCooldownMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.*
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class Ability(
    val id: String,
    val name: String,
    val color: ChatColor,
    val type: Material,
    val lore: List<String>,
    val cooldown: Int
) : CooldownMap(cooldown), Listener
{
    companion object {
        val CHAT_PREFIX = "${CC.B_PRI}[Ability] "
    }

    val displayName: String get() = "$color${CC.BOLD}$name"

    fun getItem(amount: Int = 1) : ItemStack {
        return ItemBuilder.of(type).amount(amount).name(displayName).setLore(lore).glow().build()
    }

    fun isAbilityItem(itemStack: ItemStack) : Boolean {
        return itemStack.type == type && itemStack.itemMeta != null
                && itemStack.itemMeta.hasDisplayName()
                && itemStack.itemMeta.displayName == displayName && itemStack.itemMeta.hasLore()
                && itemStack.itemMeta.lore.equals(lore)
    }

    override fun startCooldown(uuid: UUID, seconds: Int)
    {
        AbilityCooldownMap.startCooldown(uuid)
        super.startCooldown(uuid, seconds)
    }
}