package dev.foraged.foxtrot.ability.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.CooldownMap
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.ability.Ability
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Chance
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.metadata.FixedMetadataValue

@Listeners
object AntiBuildStickAbility : Ability("antibuildstick", "Anti Build Stick", ChatColor.LIGHT_PURPLE, Material.STICK, listOf(
    "${CC.YELLOW}MONKEY",
    "${CC.YELLOW}MONKEY."
), 30) {

    val antiBuildMap: CooldownMap = object : CooldownMap(15) {}


    @EventHandler
    fun onBuild(event: BlockPlaceEvent) {
        if (antiBuildMap.isOnCooldown(event.player.uniqueId)) {
            event.isCancelled = true
            event.player.sendMessage("${CC.RED}You cannot place blocks for another ${TimeUtil.formatIntoDetailedString(antiBuildMap.getCooldown(event.player.uniqueId).toInt() / 1000)}.")
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        if (event.damager !is Player) return

        val player = event.damager as Player
        if (event.isCancelled) return
        if (isAbilityItem(player.itemInHand)) {
            event.isCancelled = true
            if (!checkCooldown(player, false)) return

            val amount = kotlin.runCatching { event.entity.getMetadata("$id${player.name}")[0].asInt() ?: 1 }.getOrDefault(1)
            event.entity.setMetadata("$id${player.name}", FixedMetadataValue(FoxtrotExtendedPlugin.instance, amount))

            if (amount == 3) {
                checkCooldown(player, true)
                antiBuildMap.startCooldown(event.entity.uniqueId)
                startCooldown(player.uniqueId)
                event.entity.removeMetadata("$id${player.name}", FoxtrotExtendedPlugin.instance)
            }
        }
    }
}