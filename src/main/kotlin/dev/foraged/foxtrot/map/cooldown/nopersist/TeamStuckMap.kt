package dev.foraged.foxtrot.map.cooldown.nopersist

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.CooldownMap
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.claim.LandBoard
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*

@RegisterMap
@Listeners
object TeamStuckMap : CooldownMap(120), Listener {

    const val MAX_DISTANCE = 5
    val locations = mutableMapOf<UUID, Location>()
    val resetMap = mutableSetOf<UUID>()

    override fun startCooldown(uuid: UUID, seconds: Int)
    {
        super.startCooldown(uuid, seconds)

        val origin = Bukkit.getPlayer(uuid).location
        locations[uuid] = origin

        var safeLocation: Location? = origin
        Tasks.asyncDelayed((seconds - 5) * 20L) {
            safeLocation = nearestSafeLocation(origin)
        }

        Tasks.delayed(seconds * 20L) {
            if (resetMap.contains(uuid)) {
                resetMap.remove(uuid)
                return@delayed
            }

            val player = Bukkit.getPlayer(uuid) ?: return@delayed

            locations.remove(player.uniqueId)
            player.teleport(safeLocation ?: Bukkit.getWorld("world").spawnLocation)
            player.sendMessage("${CC.GREEN}You have been teleported to a safe location.")
        }
    }

    override fun resetCooldown(uuid: UUID)
    {
        resetMap.add(uuid)
        locations.remove(uuid)
        super.resetCooldown(uuid)
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return

        resetCooldown(event.entity.uniqueId)
        event.entity.sendMessage("${Team.CHAT_PREFIX} ${CC.RED}Your stuck teleport has been cancelled.")
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (EventUtils.hasPlayerMoved(event)) {
            val origin = locations[event.player.uniqueId] ?: return
            if (event.to.distance(origin) > MAX_DISTANCE) {
                resetCooldown(event.player.uniqueId)
                event.player.sendMessage("${Team.CHAT_PREFIX} ${CC.RED}Your stuck teleport has been cancelled as you moved more than ${MAX_DISTANCE} blocks.")
            }
        }
    }

    fun nearestSafeLocation(origin: Location): Location?
    {

        if (LandBoard.getClaim(origin) == null) {
            return getActualHighestBlock(origin.block)
                .location.add(0.0, 1.0, 0.0)
        }

        // Start iterating outward on both positive and negative X & Z.
        var xPos = 2
        var xNeg = -2
        while (xPos < 250) {
            var zPos = 2
            var zNeg = -2
            while (zPos < 250) {
                val atPos = origin.clone().add(xPos.toDouble(), 0.0, zPos.toDouble())

                // Try to find a unclaimed location with no claims adjacent
                if (LandBoard.getClaim(atPos) == null && !isAdjacentClaimed(atPos)) {
                    return getActualHighestBlock(atPos.block).location.add(0.0, 1.0, 0.0)
                }
                val atNeg = origin.clone().add(xNeg.toDouble(), 0.0, zNeg.toDouble())

                // Try again to find a unclaimed location with no claims adjacent
                if (LandBoard.getClaim(atNeg) == null && !isAdjacentClaimed(atNeg)) {
                    return getActualHighestBlock(atNeg.block).location.add(0.0, 1.0, 0.0)
                }
                zPos += 2
                zNeg -= 2
            }
            xPos += 2
            xNeg -= 2
        }
        return null
    }

    private fun getActualHighestBlock(block: Block): Block {
        var block = block
        block = block.world.getHighestBlockAt(block.location)
        while (block.type == Material.AIR && block.y > 0) {
            block = block.getRelative(BlockFace.DOWN)
        }
        return block
    }

    /**
     * @param base center block
     * @return list of all adjacent locations
     */
    private fun getAdjacent(base: Location): List<Location>
    {
        val adjacent: MutableList<Location> = ArrayList()
        // Add all relevant locations surrounding the base block
        for (face in BlockFace.values())
        {
            if (face != BlockFace.DOWN && face != BlockFace.UP) adjacent.add(base.block.getRelative(face).location)

        }
        return adjacent
    }

    /**
     *
     * @param location location to check for
     * @return if any of it's blockfaces are claimed
     */
    private fun isAdjacentClaimed(location: Location): Boolean
    {
        for (adjacent in getAdjacent(location)) {
            if (LandBoard.getClaim(adjacent) != null) return true // we found a claim on an adjacent block!
        }
        return false
    }
}
