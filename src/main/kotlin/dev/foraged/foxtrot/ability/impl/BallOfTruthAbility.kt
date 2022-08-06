package dev.foraged.foxtrot.ability.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.ability.Ability
import dev.foraged.foxtrot.ability.AbilityService
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
object BallOfTruthAbility : Ability("balloftruth", "Ball Of Truth", ChatColor.GOLD, Material.INK_SACK, listOf(
    "${CC.YELLOW}MONKEY",
    "${CC.YELLOW}MONKEY."
), 30) {
    override fun getItem(amount: Int): ItemStack {
        return ItemBuilder.copyOf(super.getItem(amount)).data(DyeColor.ORANGE.dyeData.toShort()).build()
    }

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
                var partnerItems = 0
                var potions = 0
                for (content in (event.entity as Player).inventory.contents) {
                    if ((content.type == Material.POTION) && content.durability.toInt() == 16421) potions += content.amount
                    if (AbilityService.findAbility(content) != null) partnerItems += content.amount
                }

                player.sendMessage("${CC.YELLOW}Ball fo truht")
                player.sendMessage("${CC.YELLOW}potion: ${CC.RED}$potions")
                player.sendMessage("${CC.YELLOW}pp: ${CC.RED}$partnerItems")

                startCooldown(player.uniqueId)
                event.entity.removeMetadata("$id${player.name}", FoxtrotExtendedPlugin.instance)
            }
        }
    }
}