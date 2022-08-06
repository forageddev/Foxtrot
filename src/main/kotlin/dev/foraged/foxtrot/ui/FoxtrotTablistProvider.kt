package dev.foraged.foxtrot.ui

import dev.foraged.foxtrot.map.BalancePersistMap
import dev.foraged.foxtrot.map.LivesPersistMap
import dev.foraged.foxtrot.map.stats.DeathsPersistMap
import dev.foraged.foxtrot.map.stats.KillsPersistMap
import dev.foraged.foxtrot.map.stats.KillstreakPersistMap
import dev.foraged.tablist.adapter.TablistAdapter
import dev.foraged.tablist.entry.TabEntry
import dev.foraged.tablist.skin.Skin
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object FoxtrotTablistProvider : TablistAdapter {
    override fun getHeader(player: Player) = listOf(
        "",
        "${CC.B_PRI}Nyte ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT}${CC.WHITE}${Bukkit.getServerName()}",
        ""
    ).joinToString("\n")

    override fun getFooter(player: Player) = listOf(
        "",
        "${CC.PRI}${10} ${CC.WHITE}players online",
        "${CC.GRAY}(${Bukkit.getServer().onlinePlayers.filterNot { it.hasMetadata("invisible") }.size} on this server)",
        "",
        "${CC.I_GRAY}Buy ranks, perks and more at:",
        "${CC.PRI + CC.I}${Constants.STORE_LINK}",
        ""
    ).joinToString("\n")

    override fun getLines(player: Player): MutableList<TabEntry>
    {
        return mutableListOf<TabEntry>().also {
            it.add(TabEntry().setColumn(3).setText("${CC.B_PRI}${player.name}"))
            it.add(TabEntry().setColumn(4).setText("${CC.WHITE}Kills: ${CC.PRI}${KillsPersistMap[player.uniqueId] ?: 0}"))
            it.add(TabEntry().setColumn(5).setText("${CC.WHITE}Deaths: ${CC.PRI}${DeathsPersistMap[player.uniqueId] ?: 0}"))
            it.add(TabEntry().setColumn(5).setText("${CC.WHITE}Streak: ${CC.PRI}${KillstreakPersistMap[player.uniqueId] ?: 0}"))
            it.add(TabEntry().setColumn(6).setText("${CC.WHITE}Balance: ${CC.GREEN}$${BalancePersistMap[player.uniqueId] ?: 0}"))
            it.add(TabEntry().setColumn(6).setText("${CC.WHITE}Lives: ${CC.RED}${LivesPersistMap[player.uniqueId] ?: 0}${Constants.HEART_SYMBOL}"))
        }
    }
}