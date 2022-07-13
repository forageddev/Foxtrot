package dev.foraged.foxtrot.server

import dev.foraged.foxtrot.region.RegionData
import dev.foraged.foxtrot.region.RegionType
import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.enums.SystemFlag
import dev.foraged.foxtrot.team.impl.PlayerTeam
import dev.foraged.foxtrot.team.impl.SystemTeam
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs

object ServerHandler
{
    const val NORMAL_BUFFER = 300
    const val NETHER_BUFFER = 300
    const val WARZONE_RADIUS = 1000
    const val WARZONE_BORDER = 3000

    val KIT_MAP = false

    var SOTW_EXPIRES: Long = -1
    var SOTW_ENABLED = mutableSetOf<UUID>()
    val SOTW_ACTIVE: Boolean
        get() {
            return SOTW_EXPIRES > System.currentTimeMillis()
        }

    fun getRegion(ownerTo: Team?, location: Location): RegionData
    {
        if (ownerTo != null && ownerTo is SystemTeam)
        {
            if (ownerTo.hasFlag(SystemFlag.SAFE_ZONE)) return RegionData(RegionType.SPAWN, ownerTo)
             else if (ownerTo.hasFlag(SystemFlag.KING_OF_THE_HILL)) return RegionData(RegionType.KOTH, ownerTo)
             else if (ownerTo.hasFlag(SystemFlag.CITADEL)) return RegionData(RegionType.CITADEL, ownerTo)
             else if (ownerTo.hasFlag(SystemFlag.ROAD)) return RegionData(RegionType.ROAD, ownerTo)

        }
        if (ownerTo != null) return RegionData(RegionType.CLAIMED_LAND, ownerTo)
        else if (isWarzone(location)) return RegionData(RegionType.WARZONE, null)
        return RegionData(RegionType.WILDNERNESS, null)
    }

    fun isAdminOverride(player: Player): Boolean {
        return player.gameMode == GameMode.CREATIVE
    }

    fun getSpawnLocation(): Location {
        return Bukkit.getServer().getWorld("world").spawnLocation.add(Vector(0.5, 1.0, 0.5))
    }

    fun isUnclaimedOrRaidable(loc: Location): Boolean
    {
        val owner = LandBoard.getTeam(loc)
        return owner == null || owner is PlayerTeam && owner.raidable
    }

    fun isUnclaimed(loc: Location): Boolean {
        return LandBoard.getClaim(loc) == null && !isWarzone(loc)
    }

    fun isWarzone(loc: Location): Boolean
    {
        return if (loc.world.environment != World.Environment.NORMAL) false
        else abs(loc.blockX) <= WARZONE_RADIUS && abs(loc.blockZ) <= WARZONE_RADIUS || abs(loc.blockX) > WARZONE_BORDER || abs(loc.blockZ) > WARZONE_BORDER
    }

    fun isSpawnBufferZone(loc: Location): Boolean
    {
        if (loc.world.environment != World.Environment.NORMAL) return false

        val radius = NORMAL_BUFFER
        val x = loc.blockX
        val z = loc.blockZ
        return x < radius && x > -radius && z < radius && z > -radius
    }

    fun isNetherBufferZone(loc: Location): Boolean
    {
        if (loc.world.environment != World.Environment.NETHER) return false

        val radius = NETHER_BUFFER
        val x = loc.blockX
        val z = loc.blockZ
        return x < radius && x > -radius && z < radius && z > -radius
    }
}