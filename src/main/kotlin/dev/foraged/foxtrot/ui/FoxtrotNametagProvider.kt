package dev.foraged.foxtrot.ui

import dev.foraged.foxtrot.classes.impl.ArcherClass
import dev.foraged.foxtrot.server.MapService
import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamService
import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

object FoxtrotNametagProvider : NametagProvider("Foxtrot Provider", 5)
{
    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo?
    {
        val viewerTeam = TeamService.findTeamByPlayer(refreshFor.uniqueId)
        var nametagInfo: NametagInfo? = null

        if (viewerTeam != null)
        {
            if (viewerTeam.isMember(toRefresh.uniqueId))
            {
                nametagInfo = createNametag(CC.GREEN, "")
            } else if (viewerTeam.isAlly(toRefresh.uniqueId))
            {
                nametagInfo = createNametag(Team.ALLY_COLOR, "")
            }
        }

        // If we already found something above they override these, otherwise we can do these checks.

        // If we already found something above they override these, otherwise we can do these checks.
        if (nametagInfo == null) {
            if (ArcherClass.isMarked(toRefresh) && ArcherClass.getMarkedTime(toRefresh) > System.currentTimeMillis()) {
                nametagInfo = createNametag(CC.GOLD, "")
            } else if (viewerTeam?.focused != null && (viewerTeam.focused == toRefresh.uniqueId || (TeamService.findTeam(
                    viewerTeam.focused!!
                ) as PlayerTeam?)?.isMember(toRefresh.uniqueId) == true)) {
                nametagInfo = createNametag(CC.LIGHT_PURPLE, "")
            }
        }

        // You always see yourself as green.

        // You always see yourself as green.
        if (refreshFor === toRefresh) nametagInfo = createNametag(CC.GREEN, "")

        return nametagInfo ?: createNametag(
            if (MapService.SOTW_ACTIVE && toRefresh.uniqueId !in MapService.SOTW_ENABLED)
                CC.B_BLUE
            else
                CC.YELLOW
            , "")

    }

}