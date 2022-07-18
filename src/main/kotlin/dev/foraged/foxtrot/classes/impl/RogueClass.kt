package dev.foraged.foxtrot.classes.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.classes.PvPClass
import dev.foraged.foxtrot.classes.PvPClassService
import dev.foraged.foxtrot.event.BackstabKillEvent
import dev.foraged.foxtrot.map.cooldown.nopersist.SpawnTagMap
import dev.foraged.foxtrot.map.cooldown.nopersist.pvpclass.rogue.RogueBackstabMap
import dev.foraged.foxtrot.map.cooldown.nopersist.pvpclass.rogue.RogueJumpMap
import dev.foraged.foxtrot.map.cooldown.nopersist.pvpclass.rogue.RogueSpeedMap
import dev.foraged.foxtrot.team.enums.SystemFlag
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.abs

@Listeners
object RogueClass : PvPClass("Rogue", 10, listOf(Material.SUGAR, Material.FEATHER))
{
    override fun qualifies(armor: PlayerInventory): Boolean
    {
        return wearingAllArmor(armor) &&
                armor.helmet.type == Material.CHAINMAIL_HELMET &&
                armor.chestplate.type == Material.CHAINMAIL_CHESTPLATE &&
                armor.leggings.type == Material.CHAINMAIL_LEGGINGS &&
                armor.boots.type == Material.CHAINMAIL_BOOTS
    }

    override fun apply(player: Player)
    {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true)
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1), true)
    }

    override fun tick(player: Player)
    {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true)
        if (!player.hasPotionEffect(PotionEffectType.JUMP)) player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1), true)
        if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0), true)
    }

    override fun itemConsumed(player: Player, material: Material): Boolean {
        if (material == Material.SUGAR) {
            if (RogueSpeedMap.isOnCooldown(player.uniqueId)) {
                val millisLeft = RogueSpeedMap.getCooldown(player.uniqueId) - System.currentTimeMillis()
                val msg: String = TimeUtil.formatIntoDetailedString(millisLeft.toInt() / 1000)
                player.sendMessage("${CC.RED}You cannot use this for another ${CC.BOLD}$msg${CC.RED}.")
                return false
            }
            RogueSpeedMap.startCooldown(player.uniqueId)
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 200, 4), true)
            return (true)
        } else {
            if (SystemFlag.SAFE_ZONE.appliesAt(player.location))
            {
                player.sendMessage(ChatColor.RED.toString() + "You can't use this in spawn!")
                return (false)
            }
            if (RogueJumpMap.isOnCooldown(player.uniqueId))
            {
                val millisLeft = RogueJumpMap.getCooldown(player.uniqueId) - System.currentTimeMillis()
                val msg: String = TimeUtil.formatIntoDetailedString(millisLeft.toInt() / 1000)
                player.sendMessage(ChatColor.RED.toString() + "You cannot use this for another §c§l" + msg + "§c.")
                return (false)
            }

            RogueJumpMap.startCooldown(player.uniqueId)
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 20 * 5, 7))
            SpawnTagMap.startCooldown(player.uniqueId)
            return (false)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEntityArrowHit(event: EntityDamageByEntityEvent)
    {
        if (event.isCancelled)
        {
            return
        }
        if (event.damager is Player && event.entity is Player)
        {
            val damager = event.damager as Player
            val victim = event.entity as Player
            if (damager.itemInHand != null && damager.itemInHand.type == Material.GOLD_SWORD && PvPClassService.hasKitOn(damager, this)) {
                if (RogueBackstabMap.isOnCooldown(damager.uniqueId)) return

                RogueBackstabMap.startCooldown(damager.uniqueId)
                val playerVector = damager.location.direction
                val entityVector = victim.location.direction
                playerVector.setY(0f)
                entityVector.setY(0f)
                val degrees = playerVector.angle(entityVector).toDouble()
                if (abs(degrees) < 1.4) {
                    damager.itemInHand = ItemStack(Material.AIR)
                    damager.playSound(damager.location, Sound.ITEM_BREAK, 1f, 1f)
                    damager.world.playEffect(victim.eyeLocation, Effect.STEP_SOUND, Material.REDSTONE_BLOCK)
                    if (victim.health - 7.0 <= 0) event.isCancelled = true
                    else event.damage = 0.0


                    victim.health = Math.max(0.0, victim.health - 7.0)
                    if (victim.health <= 0.0) Bukkit.getPluginManager().callEvent(BackstabKillEvent(damager, victim))

                    damager.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 2 * 20, 2))
                } else {
                    damager.sendMessage("${CC.RED}Backstab failed!")
                }
            }
        }
    }
}