package dev.foraged.foxtrot.classes

import dev.foraged.commons.annotations.runnables.Repeating
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.ChatColor

@Repeating(2L)
class PvPClassTask : Runnable
{
    override fun run()
    {
        Bukkit.getServer().onlinePlayers.forEach { player ->
            if (PvPClassService.equippedKits.contains(player.name))
            {
                val pvpClass = PvPClassService.equippedKits[player.name]!!

                if (!pvpClass.qualifies(player.inventory))
                {
                    PvPClassService.equippedKits.remove(player.name)
                    player.sendMessage(ChatColor.AQUA.toString() + "Class: " + ChatColor.BOLD + pvpClass.name + ChatColor.GRAY + " --> " + ChatColor.RED + "Disabled!")
                    pvpClass.remove(player)
                } else if (!player.hasMetadata("frozen")) pvpClass.tick(player)
            } else {
                PvPClassService.classes.forEach { pvpClass ->
                    if (pvpClass.qualifies(player.inventory) && pvpClass.canApply(player) && !player.hasMetadata("frozen")) {
                        pvpClass.apply(player)
                        PvPClassService.equippedKits[player.name] = pvpClass
                        player.sendMessage("${CC.AQUA}Class: ${CC.BOLD}${pvpClass.name}${CC.GRAY} --> ${CC.GREEN}Enabled!")
                        player.sendMessage("${CC.AQUA}Class Info: ${CC.GREEN}${pvpClass.siteLink}")
                    }
                }
            }
        }

        PvPClassService.checkSavedPotions()
    }
}