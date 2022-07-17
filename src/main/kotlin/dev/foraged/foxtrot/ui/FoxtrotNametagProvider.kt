package dev.foraged.foxtrot.ui

import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamService
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
        if (nametagInfo == null)
        {
            /*if (RangerClass.getMarkedPlayers().containsKey(toRefresh.uniqueId) && RangerClass.getMarkedPlayers()
                    .get(toRefresh.uniqueId) > System.currentTimeMillis()
            )
            {
                nametagInfo =
                    createNametag(Foxtrot.getInstance().getServerHandler().getStunTagColor().toString(), "")
            } else if (ArcherClass.getMarkedPlayers().containsKey(toRefresh.name) && ArcherClass.getMarkedPlayers()
                    .get(toRefresh.name) > System.currentTimeMillis()
            ) {
                nametagInfo = createNametag(CC.GOLD, "")
            } else if (viewerTeam != null && viewerTeam.getFocused() != null && viewerTeam.getFocused()
                    .equals(toRefresh.uniqueId)
            )
            {
                nametagInfo = createNametag(CC.LIGHT_PURPLE, "")
            }*/
        }

        // You always see yourself as green.

        // You always see yourself as green.
        if (refreshFor === toRefresh) nametagInfo = createNametag(CC.GREEN, "")

        return nametagInfo ?: createNametag(CC.YELLOW, "")

    }

}