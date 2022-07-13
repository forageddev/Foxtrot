package dev.foraged.foxtrot.map.cooldown.nopersist

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.CooldownMap
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.enums.SystemFlag
import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.metadata.FixedMetadataValue
import kotlin.math.roundToInt

@Listeners
object EnderpearlMap : CooldownMap(16), Listener
{
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onProjectileLaunch(event: ProjectileLaunchEvent)
    {
        if (event.entity.shooter !is Player) return

        val shooter = event.entity.shooter as Player
        if (event.entity is EnderPearl) {
            // Store the player's enderpearl in-case we need to remove it prematurely
            shooter.setMetadata("LastEnderPearl", FixedMetadataValue(FoxtrotExtendedPlugin.instance, event.entity))

            // Get the default time to apply (in MS)
         /*   val timeToApply =
                if (DTRBitmask.THIRTY_SECOND_ENDERPEARL_COOLDOWN.appliesAt(event.entity.location)) 30000L else if (Foxtrot.getInstance()
                        .getMapHandler().getScoreboardTitle().contains("Staging")
                ) 1000L else 16000L
*/
            // Call our custom event (time to apply needs to be modifiable)
            //val appliedEvent = EnderpearlCooldownAppliedEvent(shooter, timeToApply)
            //appliedEvent.call()

            // Put the player into the cooldown map
            startCooldown(shooter.uniqueId)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerInteract(event: ProjectileLaunchEvent)
    {
        if (event.entity !is EnderPearl) return
        if (event.entity.shooter !is Player) return

        val thrower = event.entity.shooter as Player
        if (isOnCooldown(thrower.uniqueId)) {
            val millisLeft = getCooldown(thrower.uniqueId) - System.currentTimeMillis()
            val value = millisLeft / 1000.0
            val sec = if (value > 0.1) (10.0 * value).roundToInt() / 10.0 else 0.1 // don't tell user 0.0
            event.isCancelled = true
            thrower.itemInHand = ItemBuilder.copyOf(thrower.itemInHand).amount(thrower.itemInHand.amount + 1).build()
            thrower.sendMessage(ChatColor.RED.toString() + "You cannot use this for another " + ChatColor.BOLD + sec + ChatColor.RED + " seconds!")
            thrower.updateInventory()
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.cause != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return
        } else if (!isOnCooldown(event.player.uniqueId)) {
            event.isCancelled = true // only reason for this would be player died before pearl landed, so cancel it!
            return
        }
        val target = event.to
        val from = event.from
        if (SystemFlag.SAFE_ZONE.appliesAt(target)) {
            if (!SystemFlag.SAFE_ZONE.appliesAt(from)) {
                event.isCancelled = true
                event.player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Invalid Pearl! " + ChatColor.YELLOW + "You cannot Enderpearl into spawn!")
                return
            }
        }
        if (SystemFlag.DENY_ENDERPEARL.appliesAt(target))
        {
            event.isCancelled = true
            event.player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Invalid Pearl! " + ChatColor.YELLOW + "You cannot Enderpearl into this region!")
            return
        }
        val ownerTo = LandBoard.getTeam(event.to)
        if (PvPTimerPersistableMap.isOnCooldown(event.player.uniqueId) && ownerTo != null)
        {
            if (ownerTo is PlayerTeam && ownerTo.isMember(event.player.uniqueId))
            {
                PvPTimerPersistableMap.resetCooldown(event.player.uniqueId)
            } else if (ownerTo is PlayerTeam || SystemFlag.KING_OF_THE_HILL.appliesAt(event.to) || SystemFlag.CITADEL.appliesAt(event.to)) {
                event.isCancelled = true
                event.player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Invalid Pearl! " + ChatColor.YELLOW + "You cannot Enderpearl into claims while having a PvP Timer!")
            }
        }
    }
}