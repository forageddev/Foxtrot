package dev.foraged.foxtrot.team.claim

import com.google.common.collect.Maps
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.server.ServerHandler
import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamHandler
import dev.foraged.foxtrot.team.impl.PlayerTeam
import dev.foraged.foxtrot.team.impl.SystemTeam
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.util.bukkit.ItemUtils
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.random.Random

class VisualClaim(val player: Player, val type: VisualClaimType, val bypass: Boolean = false) : Listener
{
    val blockChanges: MutableList<Location> = ArrayList()
    var resizing: Claim? = null
    var corner1: Location? = null
    var corner2: Location? = null

    fun draw(silent: Boolean)
    {
        checkTaskSetup()
        // If they already have a map open and they're opening another
        if (currentMaps.containsKey(player.name) && (type == VisualClaimType.MAP || type == VisualClaimType.SURFACE_MAP))
        {
            currentMaps[player.name]!!.cancel()
            if (!silent)
            {
                if (type == VisualClaimType.MAP) player.sendMessage(ChatColor.YELLOW.toString() + "Claim pillars have been hidden!")
                else player.sendMessage(ChatColor.YELLOW.toString() + "The surface map has been hidden!")
            }
            return
        }

        // If they have a visual claim open and this isn't a map (or subclaim map, or surface map), cancel it.
        if (visualClaims.containsKey(player.name) && !(type == VisualClaimType.MAP || type == VisualClaimType.SURFACE_MAP))
        {
            visualClaims[player.name]!!.cancel()
        }
        when (type)
        {
            VisualClaimType.MAP, VisualClaimType.SURFACE_MAP -> currentMaps[player.name] = this
            else -> visualClaims[player.name] = this
        }
        FoxtrotExtendedPlugin.instance.server.pluginManager.registerEvents(this, FoxtrotExtendedPlugin.instance)
        when (type)
        {
            VisualClaimType.CREATE ->
            {
                // smt for disabling cliams eventually
/*                // Don't allow claiming during a kit map
                if (!bypass && FoxtrotExtendedPlugin.instance.getMapHandler().isKitMap())
                {
                    if (!silent)
                    {
                        player.sendMessage(ChatColor.RED.toString() + "Land claiming is disabled during kit maps")
                    }
                    cancel()
                    return
                }*/
                player.sendMessage(ChatColor.GOLD.toString() + "Team land claim started.")
                player.sendMessage(ChatColor.YELLOW.toString() + "Left click at a corner of the land you'd like to claim.")
                player.sendMessage(ChatColor.YELLOW.toString() + "Right click on the second corner of the land you'd like to claim.")
                player.sendMessage(ChatColor.YELLOW.toString() + "Crouch left click the air to purchase your claim.")
            }
            VisualClaimType.RESIZE ->
            {
                player.sendMessage(ChatColor.GOLD.toString() + "Team land resize started.")
                player.sendMessage(ChatColor.YELLOW.toString() + "Left click in the claim you'd like to resize.")
                player.sendMessage(ChatColor.YELLOW.toString() + "Right click on the corner you'd like to resize to.")
                player.sendMessage(ChatColor.YELLOW.toString() + "Crouch left click the air to confirm your resize.")
            }
            VisualClaimType.MAP ->
            {
                val sendMaps: MutableMap<Map.Entry<Claim, Team?>, Material> = HashMap()
                for ((claimIteration, regionData) in LandBoard.getRegionData(
                    player.location, MAP_RADIUS, 256, MAP_RADIUS
                ).withIndex())
                {
                    val mat = getMaterial(claimIteration)
                    drawClaim(regionData.key, mat)
                    sendMaps[regionData] = mat
                }
                if (sendMaps.isEmpty())
                {
                    if (!silent)
                    {
                        player.sendMessage(ChatColor.YELLOW.toString() + "There are no claims within " + MAP_RADIUS + " blocks of you!")
                    }
                    cancel()
                }
                if (!silent)
                {
                    for ((key, value) in sendMaps)
                    {
                        val team = key.value
                        val claim = key.key
                        if (team is PlayerTeam)
                        {
                            player.sendMessage(
                                ChatColor.YELLOW.toString() + "Land " + ChatColor.BLUE + team.getName(player) + ChatColor.GREEN + "(" + ChatColor.AQUA + ItemUtils.getName(
                                    ItemStack(
                                        value
                                    )
                                ) + ChatColor.GREEN + ") " + ChatColor.YELLOW + "is claimed by " + ChatColor.BLUE + team.getName(
                                    player
                                )
                            )
                        } else if (team is SystemTeam)
                        {
                            player.sendMessage(
                                ChatColor.YELLOW.toString() + "Land " + team.color + team.name + ChatColor.GREEN + "(" + ChatColor.AQUA + ItemUtils.getName(
                                    ItemStack(
                                        value
                                    )
                                ) + ChatColor.GREEN + ") " + ChatColor.YELLOW + "is claimed by " + ChatColor.BLUE + team.name
                            )
                        }
                    }
                }
            }
            VisualClaimType.SURFACE_MAP ->
            {
                for ((claim, claimOwner) in LandBoard.getRegionData(
                    player.location, MAP_RADIUS, 256, MAP_RADIUS
                ))
                {
                    val claimWorld: World = FoxtrotExtendedPlugin.instance.server.getWorld(claim.world)
                    for ((x, z) in claim)
                    {
                        var block = claimWorld.getBlockAt(x, 100, z)
                        var displayCarpet = false
                        for (blockFace in NESW_BLOCKS)
                        {
                            val relative = block.getRelative(blockFace).location
                            if (!claimOwner!!.ownsLocation(relative))
                            {
                                displayCarpet = true
                                break
                            }
                        }
                        if (!displayCarpet) continue

                        while (!block.type.isSolid || block.type == Material.LEAVES || block.type == Material.LOG || block.type == Material.LOG_2)
                        {
                            block = block.getRelative(BlockFace.DOWN)
                        }
                        val carpetColor: DyeColor = if (claimOwner is PlayerTeam)
                        {
                            if (claimOwner.isMember(player.uniqueId))
                            {
                                DyeColor.GREEN
                            } else if (claimOwner.isAlly(player.uniqueId))
                            {
                                DyeColor.BLUE
                            } else
                            {
                                DyeColor.RED
                            }
                        } else DyeColor.getByData(ColorUtil.toWoolData(claimOwner!!.color).toByte())

                        sendBlockChange(player, block.location, Material.WOOL, carpetColor.woolData)
                        sendBlockChange(
                            player,
                            block.getRelative(BlockFace.UP).location,
                            Material.CARPET,
                            carpetColor.woolData
                        )
                        blockChanges.add(block.location)
                        blockChanges.add(block.getRelative(BlockFace.UP).location)
                    }
                }
                if (blockChanges.size == 0)
                {
                    if (!silent)
                    {
                        player.sendMessage(ChatColor.YELLOW.toString() + "There are no claims near you!")
                    }
                    cancel()
                    return
                }
                if (!silent)
                {
                    player.sendMessage(ChatColor.YELLOW.toString() + "Claims have been shown.")
                }
            }
        }
    }

    fun containsOtherClaim(claim: Claim): Boolean
    {
        val maxPoint = claim.maximumPoint
        val minPoint = claim.minimumPoint
        val maxTeam = LandBoard.getTeam(maxPoint)
        if (maxTeam != null && (type != VisualClaimType.RESIZE || maxTeam is PlayerTeam && !maxTeam.isMember(player.uniqueId)))
        {
            return true
        }
        val minTeam = LandBoard.getTeam(minPoint)
        if (minTeam != null && (type != VisualClaimType.RESIZE || minTeam is PlayerTeam && !minTeam.isMember(player.uniqueId)))
        {
            return true
        }

        // A Claim doesn't like being iterated when either its X or Z is 0.
        if (abs(claim.x1 - claim.x2) === 0 || abs(claim.z1 - claim.z2) === 0)
        {
            return false
        }
        for (x in minPoint.blockX..maxPoint.blockX)
        {
            for (z in minPoint.blockZ..maxPoint.blockZ)
            {
                val at = Location(
                    FoxtrotExtendedPlugin.instance.server.getWorld(claim.world),
                    x.toDouble(),
                    80.0,
                    z.toDouble()
                )
                val teamAt = LandBoard.getTeam(at)
                if (teamAt != null && (type != VisualClaimType.RESIZE || teamAt is PlayerTeam && !teamAt.isMember(player.uniqueId))) return true
            }
        }
        return false
    }

    fun getTouchingClaims(claim: Claim): MutableSet<Claim>
    {
        val touchingClaims: MutableSet<Claim> = HashSet()
        for ((x, z) in claim.outset(Claim.CuboidDirection.Horizontal, 1))
        {
            val loc = Location(FoxtrotExtendedPlugin.instance.server.getWorld(claim.world), x.toDouble(), 80.0, z.toDouble())
            val claimAtLocation = LandBoard.getRegionData(loc)
            if (claimAtLocation != null) touchingClaims.add(claimAtLocation.key)
        }
        return touchingClaims
    }

    fun setLoc(locationId: Int, clicked: Location)
    {
        val playerTeam = TeamHandler.findTeamByPlayer(player.uniqueId)
        if (playerTeam == null && !bypass) {
            player.sendMessage(
                ChatColor.RED.toString() + "You have to be on a team to " + type.name.lowercase(
                    Locale.getDefault()
                ) + " land!"
            )
            cancel()
            return
        }
        if (type == VisualClaimType.CREATE) {
            if (!bypass && !ServerHandler.isUnclaimed(clicked)) {
                player.sendMessage(ChatColor.RED.toString() + "You can only claim land in the Wilderness!")
                return
            }
            if (locationId == 1) {
                if (corner2 != null && isIllegalClaim(Claim(clicked, corner2!!), null)) return

                clearPillarAt(corner1)
                corner1 = clicked
            } else if (locationId == 2)
            {
                if (corner1 != null && isIllegalClaim(Claim(corner1!!, clicked), null)) return

                clearPillarAt(corner2)
                corner2 = clicked
            }

            Tasks.delayed(1L) {
                erectPillar(clicked, Material.EMERALD_BLOCK)
            }
            player.sendMessage(ChatColor.YELLOW.toString() + "Set claim's location " + ChatColor.LIGHT_PURPLE + locationId + ChatColor.YELLOW + " to " + ChatColor.GREEN + "(" + ChatColor.WHITE + clicked.blockX + ", " + clicked.blockY + ", " + clicked.blockZ + ChatColor.GREEN + ")" + ChatColor.YELLOW + ".")
            if (corner1 != null && corner2 != null)
            {
                val price = Claim.getPrice(Claim(corner1!!, corner2!!), playerTeam, true)
                val x = abs(corner1!!.blockX - corner2!!.blockX)
                val z = abs(corner1!!.blockZ - corner2!!.blockZ)
                if (!bypass && price > playerTeam!!.balance)
                {
                    player.sendMessage(ChatColor.YELLOW.toString() + "Claim cost: " + ChatColor.RED + "$" + price + ChatColor.YELLOW + ", Current size: (" + ChatColor.WHITE + x + ", " + z + ChatColor.YELLOW + "), " + ChatColor.WHITE + x * z + ChatColor.YELLOW + " blocks")
                } else
                {
                    player.sendMessage(ChatColor.YELLOW.toString() + "Claim cost: " + ChatColor.GREEN + "$" + price + ChatColor.YELLOW + ", Current size: (" + ChatColor.WHITE + x + ", " + z + ChatColor.YELLOW + "), " + ChatColor.WHITE + x * z + ChatColor.YELLOW + " blocks")
                }
            }
        } else if (type == VisualClaimType.RESIZE)
        {
            val teamAtLocation = LandBoard.getRegionData(clicked)
            if (locationId == 1)
            {
                if (teamAtLocation == null || teamAtLocation.value is PlayerTeam && !(teamAtLocation.value as PlayerTeam).isMember(player.uniqueId))
                {
                    player.sendMessage(ChatColor.YELLOW.toString() + "To resize your claim, please left click in the claim you'd like to resize.")
                    return
                }
                resizing = teamAtLocation.key
                drawClaim(resizing, Material.LAPIS_BLOCK)
            } else if (locationId == 2)
            {
                if (resizing == null)
                {
                    player.sendMessage(ChatColor.YELLOW.toString() + "Before you set the location you'd like to resize to, first left click in the claim you'd like to resize.")
                    return
                }
                val claimClone = Claim(resizing!!)
                applyResize(claimClone, clicked)
                if (isIllegalClaim(claimClone, listOf(resizing, claimClone)))
                {
                    return
                }
                corner2 = clicked
                object : BukkitRunnable()
                {
                    override fun run()
                    {
                        clearAllBlocks()
                        drawClaim(resizing, Material.LAPIS_BLOCK)
                        drawClaim(claimClone, Material.EMERALD_BLOCK)
                    }
                }.runTaskLater(FoxtrotExtendedPlugin.instance, 1L)
            }
            if (locationId == 1)
            {
                player.sendMessage(ChatColor.YELLOW.toString() + "Selected claim " + ChatColor.LIGHT_PURPLE + teamAtLocation!!.key.name + ChatColor.YELLOW + " to resize.")
            } else
            {
                player.sendMessage(ChatColor.YELLOW.toString() + "Set resize location to " + ChatColor.GREEN + "(" + ChatColor.WHITE + clicked.blockX + ", " + clicked.blockY + ", " + clicked.blockZ + ChatColor.GREEN + ")" + ChatColor.YELLOW + ".")
            }
            if (resizing != null && corner2 != null)
            {
                val oldPrice = Claim.getPrice(resizing!!, null, false)
                val preview = Claim(resizing!!)
                applyResize(preview, corner2!!)
                val newPrice = Claim.getPrice(preview, null, false)
                val cost = newPrice - oldPrice
                if (cost > playerTeam!!.balance && !bypass)
                {
                    player.sendMessage(ChatColor.YELLOW.toString() + "Resize cost: " + ChatColor.RED + "$" + cost)
                } else
                {
                    player.sendMessage(ChatColor.YELLOW.toString() + "Resize cost: " + ChatColor.GREEN + "$" + cost)
                }
            }
        }
    }

    fun cancel()
    {
        if (type == VisualClaimType.CREATE || type == VisualClaimType.RESIZE)
        {
            player.inventory.remove(Team.SELECTION_WAND)
        }
        HandlerList.unregisterAll(this)
        when (type)
        {
            VisualClaimType.MAP, VisualClaimType.SURFACE_MAP -> currentMaps.remove(player.name)
            else -> visualClaims.remove(player.name)
        }
        clearAllBlocks()
        ClaimHandler.remove(player.uniqueId)
    }

    fun clearAllBlocks()
    {
        for (location in blockChanges)
        {
            sendBlockChange(player, location, location.block.type, location.block.data)
        }
    }

    fun purchaseClaim()
    {
        println("purchase")
        val playerTeam = TeamHandler.findTeamByPlayer(player.uniqueId)
        if (playerTeam == null && !bypass)
        {
            player.sendMessage(ChatColor.RED.toString() + "You have to be on a team to claim land!")
            cancel()
            return
        }
        println("purchase2")
        if (corner1 != null && corner2 != null)
        {
        println("purchase3")
            val price = Claim.getPrice(Claim(corner1!!, corner2!!), playerTeam, true)
            if (!bypass && playerTeam != null)
            {
                if (playerTeam.claims.size >= Team.MAX_CLAIMS)
                {
                    player.sendMessage(ChatColor.RED.toString() + "Your team has the maximum amount of claims, which is " + Team.MAX_CLAIMS + ".")
                    return
                }
                if (!playerTeam.isCaptain(player.uniqueId) && !playerTeam.isCoLeader(player.uniqueId) && !playerTeam.isOwner(
                        player.uniqueId
                    )
                )
                {
                    player.sendMessage(ChatColor.RED.toString() + "Only team captains can claim land.")
                    return
                }
                if (playerTeam.balance < price)
                {
                    player.sendMessage(ChatColor.RED.toString() + "Your team does not have enough money to do this!")
                    return
                }
                if (playerTeam.raidable)
                {
                    player.sendMessage(ChatColor.RED.toString() + "You cannot claim land while raidable.")
                    return
                }
            }
            val team = ClaimHandler[player.uniqueId] ?: playerTeam
        println("purchaseteam")
            val claim = Claim(corner1!!, corner2!!)
        println("claim")
            if (isIllegalClaim(claim, null) || team == null) return
        println("not illegal")


            claim.name = team.name + "_" + (100 + Random.nextInt(800))
            claim.y1 = 0
            claim.y2 = 256
            LandBoard.setTeamAt(claim, team)
            team.claims.add(claim)
            player.sendMessage(ChatColor.YELLOW.toString() + "You have claimed this land for your team!")
            if (!bypass && playerTeam != null)
            {
                playerTeam.balance = playerTeam.balance - price
                FoxtrotExtendedPlugin.instance.logger
                    .info("Economy Logger: Withdrawing " + price + " from " + playerTeam.balance + "'s account: Claimed land")
                player.sendMessage(ChatColor.YELLOW.toString() + "Your team's new balance is " + ChatColor.WHITE + "$" + playerTeam.balance.toInt() + ChatColor.LIGHT_PURPLE + " (Price: $" + price + ")")
            }
        println("purchase4")
            val minLoc = claim.minimumPoint
            val maxLoc = claim.maximumPoint

            cancel()
            object : BukkitRunnable()
            {
                override fun run()
                {
                    if (currentMaps.containsKey(player.name)) currentMaps[player.name]!!.cancel()
                    VisualClaim(player, VisualClaimType.MAP, false).draw(true)
                }
            }.runTaskLater(FoxtrotExtendedPlugin.instance, 1L)
        println("finished")
        } else
        {
            player.sendMessage(ChatColor.RED.toString() + "You have not selected both corners of your claim yet!")
        }
    }

    fun resizeClaim()
    {
        val playerTeam = TeamHandler.findTeamByPlayer(player.uniqueId)
        if (playerTeam == null)
        {
            player.sendMessage(ChatColor.RED.toString() + "You have to be on a team to resize land!")
            cancel()
            return
        }
        if (resizing != null && corner2 != null)
        {
            val newClaim = Claim(resizing!!)
            applyResize(newClaim, corner2!!)
            val oldPrice = Claim.getPrice(resizing!!, null, false)
            val newPrice = Claim.getPrice(newClaim, null, false)
            val cost = newPrice - oldPrice
            if (!bypass)
            {
                if (!playerTeam.isCaptain(player.uniqueId) && !playerTeam.isCoLeader(player.uniqueId) && !playerTeam.isOwner(
                        player.uniqueId
                    )
                )
                {
                    player.sendMessage(ChatColor.RED.toString() + "Only team captains can resize land.")
                    return
                }
                if (playerTeam.balance < cost)
                {
                    player.sendMessage(ChatColor.RED.toString() + "Your team does not have enough money to do this!")
                    return
                }
                if (playerTeam.raidable)
                {
                    player.sendMessage(ChatColor.RED.toString() + "You cannot resize land while raidable.")
                    return
                }
            }
            if (isIllegalClaim(newClaim, null))
            {
                return
            }
            LandBoard.setTeamAt(resizing!!, null)
            LandBoard.setTeamAt(newClaim, playerTeam)
            playerTeam.claims.remove(resizing)
            playerTeam.claims.add(newClaim)
            player.sendMessage(ChatColor.YELLOW.toString() + "You have resized this land!")
            if (!bypass)
            {
                playerTeam.balance = playerTeam.balance - cost
                player.sendMessage(ChatColor.YELLOW.toString() + "Your team's new balance is " + ChatColor.WHITE + "$" + playerTeam.balance as Int + ChatColor.LIGHT_PURPLE + " (Price: $" + cost + ")")
            }
            val minLoc = resizing!!.minimumPoint
            val maxLoc = resizing!!.maximumPoint

            cancel()
            object : BukkitRunnable()
            {
                override fun run()
                {
                    if (currentMaps.containsKey(player.name))
                    {
                        currentMaps[player.name]!!.cancel()
                    }
                    VisualClaim(player, VisualClaimType.MAP, false).draw(true)
                }
            }.runTaskLater(FoxtrotExtendedPlugin.instance, 1L)
        } else
        {
            player.sendMessage(ChatColor.RED.toString() + "You have not selected both corners of your claim yet!")
        }
    }

    private fun drawClaim(claim: Claim?, material: Material)
    {
        for (loc in claim!!.cornerLocations)
        {
            erectPillar(loc, material)
        }
    }

    private fun erectPillar(loc: Location, mat: Material)
    {
        val set = loc.clone()
        for (y in 0..255)
        {
            set.y = y.toDouble()
            if (set.block.type == Material.AIR || set.block.type.isTransparent || set.block.type == Material.WATER || set.block.type == Material.STATIONARY_WATER)
            {
                if (y % 5 == 0)
                {
                    sendBlockChange(player, set, mat, 0.toByte())
                } else
                {
                    sendBlockChange(player, set, Material.GLASS, 0.toByte())
                }
                blockChanges.add(set.clone())
            }
        }
    }

    private fun clearPillarAt(location: Location?)
    {
        if (location == null)
        {
            return
        }
        val blockChangeIterator = blockChanges.iterator()
        while (blockChangeIterator.hasNext())
        {
            val blockChange = blockChangeIterator.next()
            if (blockChange.blockX == location.blockX && blockChange.blockZ == location.blockZ)
            {
                sendBlockChange(player, blockChange, blockChange.block.type, blockChange.block.data)
                blockChangeIterator.remove()
            }
        }
    }

    fun isIllegalClaim(claim: Claim, ignoreNearby: List<Claim?>?): Boolean
    {
        if (bypass) return false

        val playerTeam = TeamHandler.findTeamByPlayer(player.uniqueId)!!
        if (containsOtherClaim(claim))
        {
            player.sendMessage(ChatColor.RED.toString() + "This claim contains unclaimable land!")
            return true
        }
        if (player.world.environment != World.Environment.NORMAL)
        {
            player.sendMessage(ChatColor.RED.toString() + "Land can only be claimed in the overworld.")
            return true
        }
        val touchingClaims = getTouchingClaims(claim)
        val teamClaims = touchingClaims.iterator()
        var removedSelfClaims = false
        while (teamClaims.hasNext())
        {
            val possibleClaim = teamClaims.next()
            if (ignoreNearby != null && ignoreNearby.contains(possibleClaim))
            {
                removedSelfClaims = true
                teamClaims.remove()
            } else if (playerTeam.ownsClaim(possibleClaim))
            {
                removedSelfClaims = true
                teamClaims.remove()
            }
        }
        if (playerTeam.claims.size !== (if (type == VisualClaimType.RESIZE) 1 else 0) && !removedSelfClaims)
        {
            player.sendMessage(ChatColor.RED.toString() + "All of your claims must be touching each other!")
            return true
        }
        if (touchingClaims.size > 1 || touchingClaims.size == 1 && !removedSelfClaims)
        {
            player.sendMessage(ChatColor.RED.toString() + "Your claim must be at least 1 block away from enemy claims!")
            return true
        }
        val x: Int = Math.abs(claim.x1 - claim.x2)
        val z: Int = Math.abs(claim.z1 - claim.z2)
        if (x < 5 || z < 5)
        {
            player.sendMessage(ChatColor.RED.toString() + "Your claim is too small! The claim has to be at least 5 x 5!")
            return true
        }
        if (x > 3 * z || z > 3 * x)
        {
            player.sendMessage(ChatColor.RED.toString() + "One side of your claim cannot be more than 3 times larger than the other!")
            return true
        }
        return false
    }

    fun applyResize(claim: Claim, location: Location)
    {
        var furthestDistance = 0.0
        var furthestCorner: Location? = null
        for (corner in claim.cornerLocations)
        {
            val distance = location.distanceSquared(corner)
            if (furthestCorner == null || distance > furthestDistance)
            {
                furthestDistance = distance
                furthestCorner = corner
            }
        }
        claim.setLocations(location, furthestCorner!!)
        claim.y1 = 0
        claim.y2 = 256
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent)
    {
        if (event.player === player && player.itemInHand != null)
        {
            if (player.itemInHand.type == Team.SELECTION_WAND.type && type == VisualClaimType.CREATE)
            {
                when (event.action)
                {
                    Action.RIGHT_CLICK_BLOCK -> {
                        player.removeMetadata("ClaimAirStop", FoxtrotExtendedPlugin.instance)
                        setLoc(2, event.clickedBlock.location)
                    }
                    Action.RIGHT_CLICK_AIR ->
                    {
                        if (player.hasMetadata("ClaimAirStop")) {
                            cancel()
                            player.sendMessage(ChatColor.RED.toString() + "You have cancelled the claiming process.")
                        } else {
                            player.setMetadata("ClaimAirStop", FixedMetadataValue(FoxtrotExtendedPlugin.instance, true))
                        }
                    }
                    Action.LEFT_CLICK_BLOCK -> if (player.isSneaking)
                    {
                        player.removeMetadata("ClaimAirStop", FoxtrotExtendedPlugin.instance)
                        purchaseClaim()
                    } else
                    {
                        setLoc(1, event.clickedBlock.location)
                    }
                    Action.LEFT_CLICK_AIR -> if (player.isSneaking)
                    {
                        purchaseClaim()
                    }
                }
                event.isCancelled = true
            } else if (player.itemInHand.type == Team.SELECTION_WAND.type && type == VisualClaimType.RESIZE)
            {
                when (event.action)
                {
                    Action.RIGHT_CLICK_BLOCK -> setLoc(2, event.clickedBlock.location)
                    Action.RIGHT_CLICK_AIR ->
                    {
                        cancel()
                        player.sendMessage(ChatColor.RED.toString() + "You have cancelled the resizing process.")
                    }
                    Action.LEFT_CLICK_BLOCK -> if (player.isSneaking)
                    {
                        resizeClaim()
                    } else
                    {
                        setLoc(1, event.clickedBlock.location)
                    }
                    Action.LEFT_CLICK_AIR -> if (player.isSneaking)
                    {
                        resizeClaim()
                    }
                }
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent)
    {
        if (player === event.player) cancel()
    }

    fun getMaterial(iteration: Int): Material
    {
        var iteration = iteration
        if (iteration == -1) return Material.IRON_BLOCK
        while (iteration >= MAP_MATERIALS.size) iteration -= MAP_MATERIALS.size
        return MAP_MATERIALS[iteration]
    }

    class QueuedBlockChange(val location: Location, val type: Material, val data: Byte = 0)

    companion object
    {
        const val MAP_RADIUS = 50
        val MAP_MATERIALS = arrayOf(
            Material.DIAMOND_BLOCK,
            Material.GOLD_BLOCK, Material.LOG, Material.BRICK, Material.WOOD,
            Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK, Material.CHEST,
            Material.MELON_BLOCK, Material.STONE, Material.COBBLESTONE,
            Material.COAL_BLOCK, Material.DIAMOND_ORE, Material.COAL_ORE,
            Material.GOLD_ORE, Material.REDSTONE_ORE, Material.FURNACE
        )
        val NESW_BLOCKS = arrayOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

        val currentMaps: MutableMap<String, VisualClaim> = HashMap()

        val visualClaims: MutableMap<String, VisualClaim> = HashMap()
        var taskSetup = false
        val queuedBlockChanges: MutableMap<UUID, Queue<QueuedBlockChange>> = Maps.newHashMap()

        private fun checkTaskSetup()
        {
            if (taskSetup)
            {
                return
            }
            taskSetup = true
            Bukkit.getScheduler().runTaskTimer(FoxtrotExtendedPlugin.instance, {
                val entryIterator: MutableIterator<Map.Entry<UUID, Queue<QueuedBlockChange>>> =
                    queuedBlockChanges.entries.iterator()
                while (entryIterator.hasNext())
                {
                    val (key, queue) = entryIterator.next()
                    val bukkitPlayer: Player? = Bukkit.getPlayer(key)

                    if (bukkitPlayer == null) {
                        entryIterator.remove()
                        continue
                    }
                    val queuedBlockChange = queue.poll()
                    if (queuedBlockChange == null)
                    {
                        entryIterator.remove()
                        continue
                    }
                    bukkitPlayer.sendBlockChange(
                        queuedBlockChange.location,
                        queuedBlockChange.type,
                        queuedBlockChange.data
                    )
                }
            }, 1L, 1L)
        }

        private fun sendBlockChange(player: Player, location: Location, type: Material, data: Byte)
        {
            if (true) {
                player.sendBlockChange(location, type, data)
                return
            }
            if (!queuedBlockChanges.containsKey(player.uniqueId)) queuedBlockChanges[player.uniqueId] = ConcurrentLinkedQueue()

            queuedBlockChanges[player.uniqueId]!!.add(QueuedBlockChange(location, type, data))
        }

        fun getVisualClaim(name: String): VisualClaim?
        {
            return visualClaims[name]
        }
    }
}