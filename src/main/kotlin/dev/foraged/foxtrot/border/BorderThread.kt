package dev.foraged.foxtrot.border

import dev.foraged.commons.persist.PluginService
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.map.cooldown.nopersist.SpawnTagMap
import dev.foraged.foxtrot.team.claim.Claim
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.enums.SystemFlag
import dev.foraged.foxtrot.team.impl.SystemTeam
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

@Service
object BorderThread : Thread("Foxtrot - Border Thread"), PluginService {

    val REGION_DISTANCE = 8
    val REGION_DISTANCE_SQUARED = REGION_DISTANCE * REGION_DISTANCE

    private val sentBlockChanges: MutableMap<String, MutableMap<Location, Long>> = HashMap()

    @Configure
    override fun configure() {
        start()
    }

    override fun run() {
        while (true) {
            for (player in Bukkit.getServer().onlinePlayers) {
                kotlin.runCatching {
                    checkPlayer(player)
                }.onFailure { exception -> exception.printStackTrace()  }
            }
            try {
                sleep(250L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun checkPlayer(player: Player) {
        try {
            val claims: MutableList<Claim> = LinkedList<Claim>()
            if (player.gameMode == GameMode.CREATIVE) {
                return
            }
            val tagged = SpawnTagMap.isOnCooldown(player.uniqueId)
            val hasPvPTimer = PvPTimerPersistableMap.isOnCooldown(player.uniqueId)
            if (!tagged && !hasPvPTimer) {
                clearPlayer(player)
                return
            }
            for ((claim, team) in LandBoard
                .getRegionData(player.location, REGION_DISTANCE, REGION_DISTANCE, REGION_DISTANCE)) {

            // Ignore claims if the player is in them.
            // There might become a time where we need to remove this
            // and make it a per-claim-type check, however for now
            // all checks work fine with this here.
            if (claim.contains(player)) {
                continue
            }
            if (team is SystemTeam) {
                if (team.hasFlag(SystemFlag.SAFE_ZONE) && tagged) {
                    // If the team is a SAFE_ZONE (IE spawn), they're not inside of it, and they're spawn tagged
                    claims.add(claim)
                } else if ((team.hasFlag(SystemFlag.KING_OF_THE_HILL) || team.hasFlag(SystemFlag.CITADEL)) && hasPvPTimer) {
                    // If it's an event zone (KOTH or Citadel) and they have a PvP Timer
                    claims.add(claim)
                }
            } else {
                if (PvPTimerPersistableMap.isOnCooldown(player.uniqueId)) {
                    // If it's an actual claim and the player has a PvP Timer
                    claims.add(claim)
                }
            }
        }
            if (claims.size == 0) {
                clearPlayer(player)
            } else {
                if (!sentBlockChanges.containsKey(player.name)) {
                    sentBlockChanges[player.name] = HashMap()
                }
                val bordersIterator: MutableIterator<Map.Entry<Location, Long>> =
                    sentBlockChanges[player.name]!!.entries.iterator()

                // Remove borders after they 'expire' -- This is used to get rid of block changes the player has walked away from,
                // whose value in the map hasn't been updated recently.
                while (bordersIterator.hasNext()) {
                    val (loc, value) = bordersIterator.next()
                    if (System.currentTimeMillis() >= value) {
                        if (!loc.world.isChunkLoaded(loc.blockX shr 4, loc.blockZ shr 4)) {
                            continue
                        }
                        val block = loc.block
                        player.sendBlockChange(loc, block.type, block.data)
                        bordersIterator.remove()
                    }
                }
                for (claim in claims) {
                    sendClaimToPlayer(player, claim)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendClaimToPlayer(player: Player, claim: Claim) {
        // start lunar impl
       /* if (LunarClientAPI.getInstance().isRunningLunarClient(player)) {
            val claims = sentWorldBorders[player.name] ?: mutableListOf()

            if (!claims.contains(claim)) {
                claims.add(claim)
                sentWorldBorders[player.name] = claims

                println("sendding worldborder packet")
                LunarClientAPI.getInstance().sendPacket(player, LCPacketWorldBorderCreateNew(
                    claim.name,
                    claim.world,
                    true,
                    true,
                    false,
                    -13421569,
                    claim.minimumPoint.x,
                    claim.minimumPoint.z,
                    claim.maximumPoint.x,
                    claim.maximumPoint.z
                ))
            }

            return
        }*/
        // enmd lunar impl

        // This gets us all the coordinates on the outside of the claim.
        // Probably could be made better
        for (coordinate in claim) {
            val onPlayerY = Location(player.world, coordinate.x.toDouble(), player.location.y, coordinate.z.toDouble())

            // Ignore an entire pillar if the block closest to the player is further than the max distance (none of the others will be close enough, either)
            if (onPlayerY.distanceSquared(player.location) > REGION_DISTANCE_SQUARED) {
                continue
            }
            for (i in -4..4) {
                val check = onPlayerY.clone().add(0.0, i.toDouble(), 0.0)
                if (check.world.isChunkLoaded(
                        check.blockX shr 4,
                        check.blockZ shr 4
                    ) && check.block.type.isTransparent && check.distanceSquared(onPlayerY) < REGION_DISTANCE_SQUARED
                ) {
                    player.sendBlockChange(check, Material.STAINED_GLASS, 14.toByte()) // Red stained glass
                    sentBlockChanges[player.name]!![check] =
                        System.currentTimeMillis() + 4000L // The time the glass will stay for if the player walks away
                }
            }
        }
    }

    private fun clearPlayer(player: Player) {
        /*if (sentWorldBorders.containsKey(player.name)) {
            sentWorldBorders[player.name]!!.forEach {
                LunarClientAPI.getInstance().sendPacket(player, LCPacketWorldBorderRemove(it.name))
            }


            return
        }*/

        if (sentBlockChanges.containsKey(player.name)) {
            for (changedLoc in sentBlockChanges[player.name]!!.keys) {
                if (!changedLoc.world.isChunkLoaded(changedLoc.blockX shr 4, changedLoc.blockZ shr 4)) {
                    continue
                }
                val block = changedLoc.block
                player.sendBlockChange(changedLoc, block.type, block.data)
            }
            sentBlockChanges.remove(player.name)
        }
    }
}