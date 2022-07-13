package dev.foraged.foxtrot.enchant

import dev.foraged.commons.annotations.runnables.Repeating
import dev.foraged.foxtrot.enchant.impl.RepairEnchant
import org.bukkit.Bukkit
import org.bukkit.Material

@Repeating(20L)
class EnchantTask : Runnable
{
    override fun run()
    {
        Bukkit.getServer().onlinePlayers.forEach { player ->
            player.inventory.armorContents.filterNotNull().filter { it.type != Material.AIR }.forEach { item ->
                EnchantHandler.findEnchants(item).filterNot { it.key is RepairEnchant }.forEach {
                    it.key.tick(player, it.value)
                }
            }
        }
    }
}