package dev.foraged.foxtrot.region

import dev.foraged.foxtrot.team.Team
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class RegionData(val regionType: RegionType, val data: Team?)
{
    override fun equals(obj: Any?): Boolean
    {
        if (obj == null || obj !is RegionData)
        {
            return false
        }
        val other = obj
        return other.regionType == regionType && (data == null || other.data!! == data)
    }

    fun getName(player: Player): String
    {
        return if (data == null)
        {
            when (regionType)
            {
                RegionType.WARZONE -> ChatColor.RED.toString() + "Warzone"
                RegionType.WILDNERNESS -> ChatColor.GRAY.toString() + "The Wilderness"
                else -> ChatColor.DARK_RED.toString() + "N/A"
            }
        } else data.getName(player)
    }

    override fun hashCode(): Int
    {
        var result = regionType.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }
}