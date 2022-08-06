package dev.foraged.foxtrot.ability

import dev.foraged.commons.persist.CooldownMap
import dev.foraged.foxtrot.map.cooldown.nopersist.AbilityCooldownMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
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

    open fun checkCooldown(player: Player, takeItem: Boolean = true) : Boolean {
        if (AbilityCooldownMap.isOnCooldown(player.uniqueId)) {
            player.sendMessage("${CC.RED}You cannot use ability items for another ${TimeUtil.formatIntoDetailedString(((AbilityCooldownMap.getCooldown(player.uniqueId) - System.currentTimeMillis()) / 1000).toInt())}.")
            player.updateInventory()
            return false
        }

        if (isOnCooldown(player.uniqueId)) {
            player.sendMessage("${CC.RED}You cannot use a $name for another ${TimeUtil.formatIntoDetailedString(((getCooldown(player.uniqueId) - System.currentTimeMillis()) / 1000).toInt())}.")
            player.updateInventory()
            return false
        }

        if (!takeItem) return true
        if (player.itemInHand.amount == 1) player.itemInHand = null
        else player.itemInHand.amount--
        return true
    }

    open fun getItem(amount: Int = 1) : ItemStack {
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