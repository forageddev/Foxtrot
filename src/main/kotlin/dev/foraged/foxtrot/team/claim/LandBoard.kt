package dev.foraged.foxtrot.team.claim

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamHandler
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object LandBoard : Listener
{
    private val claimsEnabled = true
    private val buckets: MutableMap<String, Multimap<CoordinateSet, Map.Entry<Claim, Team?>>> =
        ConcurrentHashMap<String, Multimap<CoordinateSet, Map.Entry<Claim, Team?>>>()

    init
    {
        for (world in FoxtrotExtendedPlugin.instance.server.worlds) {
            buckets[world.name] = HashMultimap.create()
        }
        FoxtrotExtendedPlugin.instance.server.pluginManager.registerEvents(this, FoxtrotExtendedPlugin.instance)
    }

    fun loadFromTeams()
    {
        for (team in TeamHandler.teams)
        {
            for (claim in team.claims)
            {
                setTeamAt(claim, team)
            }
        }
    }

    fun getRegionData(center: Location, xDistance: Int, yDistance: Int, zDistance: Int): Set<Map.Entry<Claim, Team?>>
    {
        val loc1 = Location(
            center.world,
            (center.blockX - xDistance).toDouble(),
            (center.blockY - yDistance).toDouble(),
            (center.blockZ - zDistance).toDouble()
        )
        val loc2 = Location(
            center.world,
            (center.blockX + xDistance).toDouble(),
            (center.blockY + yDistance).toDouble(),
            (center.blockZ + zDistance).toDouble()
        )
        return getRegionData(loc1, loc2)
    }

    fun getRegionData(min: Location, max: Location): Set<Map.Entry<Claim, Team?>>
    {
        if (!claimsEnabled)
        {
            return HashSet()
        }
        val regions: MutableSet<Map.Entry<Claim, Team?>> = HashSet()
        val step = 1 shl CoordinateSet.BITS
        var x = min.blockX
        while (x < max.blockX + step)
        {
            var z = min.blockZ
            while (z < max.blockZ + step)
            {
                val coordinateSet = CoordinateSet(x, z)
                for (regionEntry in buckets[min.world.name]!![coordinateSet])
                {
                    if (!regions.contains(regionEntry))
                    {
                        if (max.blockX >= regionEntry.key.x1
                            && min.blockX <= regionEntry.key.x2
                            && max.blockZ >= regionEntry.key.z1
                            && min.blockZ <= regionEntry.key.z2
                            && max.blockY >= regionEntry.key.y1
                            && min.blockY <= regionEntry.key.y2
                        )
                        {
                            regions.add(regionEntry)
                        }
                    }
                }
                z += step
            }
            x += step
        }
        return regions
    }

    fun getRegionData(location: Location): Map.Entry<Claim, Team?>?
    {
        if (!claimsEnabled)
        {
            return null
        }
        for (data in buckets[location.world.name]!![CoordinateSet(location.blockX, location.blockZ)])
        {
            if (data.key.contains(location))
            {
                return data
            }
        }
        return null
    }

    fun getClaim(location: Location): Claim?
    {
        val regionData: Map.Entry<Claim, Team?>? = getRegionData(location)
        return regionData?.key
    }

    fun getTeam(location: Location): Team?
    {
        val regionData: Map.Entry<Claim, Team?>? = getRegionData(location)
        return regionData?.value
    }

    fun setTeamAt(claim: Claim, team: Team?)
    {
        val regionData: Map.Entry<Claim, Team?> = AbstractMap.SimpleEntry<Claim, Team?>(claim, team)
        val step = 1 shl CoordinateSet.BITS
        var x = regionData.key.x1
        while (x < regionData.key.x2 + step)
        {
            var z = regionData.key.z1
            while (z < regionData.key.z2 + step)
            {
                val worldMap: Multimap<CoordinateSet, Map.Entry<Claim, Team?>>? = buckets[regionData.key.world]
                if (worldMap == null) {
                    z += step
                    continue
                }
                if (regionData.value == null) {
                    val coordinateSet = CoordinateSet(x, z)
                    worldMap[coordinateSet].removeIf { (key): Map.Entry<Claim, Team?> -> key == regionData.key }
                } else {
                    worldMap.put(CoordinateSet(x, z), regionData)
                }
                z += step
            }
            x += step
        }
        updateClaim(claim)
    }

    fun updateClaim(modified: Claim)
    {
        val visualClaims: ArrayList<VisualClaim> = ArrayList<VisualClaim>(VisualClaim.currentMaps.values)
        for (visualClaim in visualClaims)
        {
            if (modified.isWithin(
                    visualClaim.player.location.blockX,
                    visualClaim.player.location.blockZ,
                    VisualClaim.MAP_RADIUS,
                    modified.world
                )
            )
            {
                visualClaim.draw(true)
                visualClaim.draw(true)
            }
        }
    }

    fun clear(team: Team)
    {
        for (claim in team.claims) setTeamAt(claim, null)
    }

    @EventHandler
    fun onWorldLoadEvent(event: WorldLoadEvent)
    {
        buckets[event.world.name] = HashMultimap.create()
    }
}