package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.server.MapService
import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamService
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.enums.SystemFlag
import dev.foraged.foxtrot.team.impl.PlayerTeam
import dev.foraged.foxtrot.team.impl.SystemTeam
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.EventUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.*

@Listeners
object TeamListener : Listener
{
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent)
    {
        val team = TeamService.findTeamByPlayer(event.player.uniqueId)
        if (team != null) {
            for (player in Bukkit.getServer().onlinePlayers) {
                if (team.isMember(player.uniqueId)) {
                    player.sendMessage("${CC.GREEN}Member Online: " + ChatColor.WHITE + event.player.name)
                } else if (team.isAlly(player.uniqueId)) {
                    player.sendMessage(Team.ALLY_COLOR + "Ally Online: " + ChatColor.WHITE + event.player.name)
                }
            }
        } else {
            event.player.sendMessage("${CC.RED}You are not currently in a team. Please create a new team using /team create <name>")
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent)
    {
        val team = TeamService.findTeamByPlayer(event.player.uniqueId)
        if (team != null) {
            for (player in Bukkit.getServer().onlinePlayers) {
                if (player == event.player) continue

                if (team.isMember(player.uniqueId)) {
                    player.sendMessage("${CC.RED}Member Offline: ${CC.WHITE}${event.player.name}")
                } else if (team.isAlly(player.uniqueId)) {
                    player.sendMessage(Team.ALLY_COLOR + "Ally Offline: ${CC.WHITE}${event.player.name}")
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockIgnite(event: BlockIgniteEvent) {
        if (event.player == null) return
        if (MapService.isAdminOverride(event.player)) return
        if (MapService.isUnclaimedOrRaidable(event.block.location)) return

        val owner = LandBoard.getTeam(event.block.location)
        if (owner != null) {
            if (event.cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL && owner is PlayerTeam && owner.isMember(event.player.uniqueId)) return

            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent)
    {
        if (MapService.isAdminOverride(event.player) || MapService.isUnclaimedOrRaidable(event.block.location)) return

        val team = LandBoard.getTeam(event.block.location)
        if (team is SystemTeam || team is PlayerTeam && !team.isMember(event.player.uniqueId)) {
            event.player.sendMessage("${CC.YELLOW}You cannot place blocks in ${team.getName(event.player)}${CC.YELLOW}'s territory!")
            event.isCancelled = true
            return
        }
    }

    @EventHandler(ignoreCancelled = true) // normal priority
    fun onBlockBreak(event: BlockBreakEvent)
    {
        if (MapService.isAdminOverride(event.player) || MapService.isUnclaimedOrRaidable(event.block.location)) return

        val team = LandBoard.getTeam(event.block.location) ?: return

        if (team is SystemTeam) {
            if (event.block.type == Material.GLOWSTONE && team.hasFlag(SystemFlag.GLOWSTONE)) return  // don't concern ourselves with glowstone breaks in glowstone mountains
            if (team.hasFlag(SystemFlag.ROAD) && event.block.y <= 40) return  // allow players to mine under roads
        }

        if (team is SystemTeam || team is PlayerTeam && !team.isMember(event.player.uniqueId)) {
            event.player.sendMessage("${CC.YELLOW}You cannot break blocks in ${team.getName(event.player)}${CC.YELLOW}'s territory!")
            event.isCancelled = true
            return
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockPistonRetract(event: BlockPistonRetractEvent)
    {
        if (!event.isSticky) return

        val retractBlock = event.retractLocation.block
        if (retractBlock.isEmpty || retractBlock.isLiquid) return

        event.isCancelled = LandBoard.getTeam(event.block.location) != LandBoard.getTeam(retractBlock.location)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockPistonExtend(event: BlockPistonExtendEvent)
    {
        val pistonTeam = LandBoard.getTeam(event.block.location)
        if (pistonTeam is PlayerTeam) {
            var i = 0
            for (block in event.blocks) {
                i++
                val targetBlock = event.block.getRelative(event.direction, i + 1)
                val targetTeam = LandBoard.getTeam(targetBlock.location)
                if (targetTeam === pistonTeam || targetTeam == null || targetTeam is PlayerTeam && targetTeam.raidable) continue

                if (targetBlock.isEmpty || targetBlock.isLiquid) event.isCancelled = true

            }
        } else event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onHangingPlace(event: HangingPlaceEvent)
    {
        if (MapService.isAdminOverride(event.player) || MapService.isUnclaimedOrRaidable(event.entity.location)) return

        val team = LandBoard.getTeam(event.entity.location)
        event.isCancelled = team is SystemTeam || team is PlayerTeam && !team.isMember(event.player.uniqueId)

    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onHangingBreakByEntity(event: HangingBreakByEntityEvent)
    {
        if (event.remover !is Player || MapService.isAdminOverride(event.remover as Player)) return
        if (MapService.isUnclaimedOrRaidable(event.entity.location)) return

        val team = LandBoard.getTeam(event.entity.location)
        event.isCancelled = team is SystemTeam || team is PlayerTeam && !team.isMember(event.remover.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent)
    {
        if (event.rightClicked.type != EntityType.ITEM_FRAME || MapService.isAdminOverride(event.player)) return
        if (MapService.isUnclaimedOrRaidable(event.rightClicked.location)) return
        val team = LandBoard.getTeam(event.rightClicked.location)
        event.isCancelled = team is SystemTeam || team is PlayerTeam && !team.isMember(event.player.uniqueId)
    }

    // Used for item frames
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent)
    {
        if (event.entity.type != EntityType.ITEM_FRAME) return

        val damager = EventUtils.getAttacker(event.damager)
        if (damager == null || MapService.isAdminOverride(damager) || MapService.isUnclaimedOrRaidable(event.entity.location)) return

        val team = LandBoard.getTeam(event.entity.location)
        event.isCancelled = team is SystemTeam || team is PlayerTeam && !team.isMember(event.damager.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamageByEntity2(event: EntityDamageByEntityEvent)
    {
        if (event.entity !is Player) return

        val damager = EventUtils.getAttacker(event.damager) // find the player damager if one exists
        if (damager != null) {
            val team = TeamService.findTeamByPlayer(damager.uniqueId)
            val victim = event.entity as Player
            if (team != null && event.cause != EntityDamageEvent.DamageCause.FALL) {
                if (team.isMember(victim.uniqueId))
                {
                    damager.sendMessage("${CC.YELLOW}You cannot hurt your teammate ${CC.GREEN}${victim.name}${CC.YELLOW}.")
                    event.isCancelled = true
                } else if (team.isAlly(victim.uniqueId)) {
                    damager.sendMessage("${CC.YELLOW}Be careful, that's your ally ${Team.ALLY_COLOR}${victim.name}${CC.YELLOW}.")
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityHorseDamage(event: EntityDamageByEntityEvent)
    {
        if (event.entity !is Horse) return

        val damager = EventUtils.getAttacker(event.damager) // find the player damager if one exists
        val victim = event.entity as Horse
        if (damager != null && victim.isTamed) {
            val damagerTeam = TeamService.findTeamByPlayer(damager.uniqueId)
            val horseOwner = victim.owner.uniqueId
            if (damager.uniqueId != horseOwner && damagerTeam != null && damagerTeam.isMember(horseOwner)) {
                event.isCancelled = true
                damager.sendMessage(
                    "${CC.YELLOW}This horse belongs to ${CC.GREEN}" + ScalaStoreUuidCache.username(
                        horseOwner
                    ) + "${CC.YELLOW} who is in your faction."
                )
            }
        }
    }

    @EventHandler
    fun onBucketEmpty(event: PlayerBucketEmptyEvent)
    {
        val checkLocation = event.blockClicked.getRelative(event.blockFace).location
        if (MapService.isAdminOverride(event.player) || MapService.isUnclaimedOrRaidable(checkLocation)) return

        val owner = LandBoard.getTeam(checkLocation)

        if (owner is SystemTeam || owner is PlayerTeam && !owner.isMember(event.player.uniqueId)) {
            event.isCancelled = true
            event.player.sendMessage("${CC.YELLOW}You cannot empty buckets in ${owner.getName(event.player)}${CC.YELLOW}'s territory!")
        }
    }

    @EventHandler
    fun onBucketFill(event: PlayerBucketFillEvent)
    {
        val checkLocation = event.blockClicked.getRelative(event.blockFace).location
        if (MapService.isAdminOverride(event.player) || MapService.isUnclaimedOrRaidable(checkLocation)) return

        val owner = LandBoard.getTeam(checkLocation)
        if (owner !is PlayerTeam) return
        if (!owner.isMember(event.player.uniqueId)) {
            event.isCancelled = true
            event.player.sendMessage("${CC.YELLOW}You cannot fill buckets in ${owner.getName(event.player)}${CC.YELLOW}'s territory!")
        }
    }
}