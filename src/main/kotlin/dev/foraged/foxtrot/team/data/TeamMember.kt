package dev.foraged.foxtrot.team.data

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*


class TeamMember(val uniqueId: UUID, val name: String, var role: TeamMemberRole)
{
    fun hasPermission(permission: TeamMemberPermission) : Boolean {
        return permission.hasPermission(uniqueId)
    }

    fun getBukkitPlayer(): Player?
    {
        return Bukkit.getPlayer(uniqueId)
    }
}