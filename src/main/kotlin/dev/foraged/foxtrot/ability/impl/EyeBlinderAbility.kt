package dev.foraged.foxtrot.ability.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.ability.Ability
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.map.cooldown.nopersist.AbilityCooldownMap
import dev.foraged.foxtrot.team.enums.SystemFlag
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Chance
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Listeners
object EyeBlinderAbility : Ability("eyeblinder", "Eye Blinder", ChatColor.DARK_GREEN, Material.SPIDER_EYE, listOf(
    "${CC.YELLOW}MONKEY",
    "${CC.YELLOW}MONKEY."
), 30) {
    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        if (event.damager !is Player) return

        val player = event.damager as Player
        if (event.isCancelled) return
        if (isAbilityItem(player.itemInHand)) {
            event.isCancelled = true
            if (!checkCooldown(player)) return

            val amount = kotlin.runCatching { event.entity.getMetadata("$id${player.name}")[0].asInt() ?: 1 }.getOrDefault(1)
            event.entity.setMetadata("$id${player.name}", FixedMetadataValue(FoxtrotExtendedPlugin.instance, amount))

            if (amount == 3) {
                if (Chance.percent(50)) {
                    (event.entity as Player).addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 0))
                    player.sendMessage("${CC.SEC}You have blinded ${CC.PRI}${event.entity.name}${CC.SEC}.")
                } else {
                    player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 7, 0))
                    player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 7, 0))

                    player.sendMessage("${CC.SEC}You have been blinded.")
                }
                startCooldown(player.uniqueId)
                event.entity.removeMetadata("$id${player.name}", FoxtrotExtendedPlugin.instance)
            }
        }
    }
}