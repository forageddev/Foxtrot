package dev.foraged.foxtrot.team.data

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*


class TeamMember(val uniqueId: UUID, val name: String, val role: TeamMemberRole)
{
    fun getBukkitPlayer(): Player?
    {
        return Bukkit.getPlayer(uniqueId)
    }
}