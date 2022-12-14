package dev.foraged.foxtrot.classes.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.classes.PvPClass
import dev.foraged.foxtrot.classes.PvPClassService
import dev.foraged.foxtrot.map.CobblestonePersistMap
import dev.foraged.foxtrot.map.ore.impl.DiamondPersistableMap
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.text.TextUtil
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.PlayerInventory
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Listeners
object MinerClass : PvPClass("Miner", ChatColor.AQUA,10, listOf())
{
    const val Y_HEIGHT = 20
    const val ULTIMATE_RADIUS = 1
    val noDamage = mutableMapOf<String, Int>()
    val invis = mutableMapOf<String, Int>()

    override fun getScoreboardLines(player: Player): List<String>
    {
        return super.getScoreboardLines(player).toMutableList().also {
            it.add("${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.WHITE}Diamonds: ${CC.AQUA}${DiamondPersistableMap[player.uniqueId] ?: 0}")
        }
    }

    override fun qualifies(armor: PlayerInventory) : Boolean
    {
        return wearingAllArmor(armor) &&
                armor.helmet.type == Material.IRON_HELMET &&
                armor.chestplate.type == Material.IRON_CHESTPLATE &&
                armor.leggings.type == Material.IRON_LEGGINGS &&
                armor.boots.type == Material.IRON_BOOTS
    }

    override fun apply(player: Player)
    {
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0), true)
        player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, Int.MAX_VALUE, 1), true)
        player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Int.MAX_VALUE, 0), true)
    }

    override fun tick(player: Player)
    {
        if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0), true)


        val diamonds = DiamondPersistableMap[player.uniqueId] ?: 0
        var level = if (diamonds > 125) 3 else if (diamonds > 50) 2 else 1

        if (shouldApplyPotion(player, PotionEffectType.FAST_DIGGING, level)) player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, Int.MAX_VALUE, level), true)

        level = if (diamonds > 400) 1 else if (diamonds > 100) 0 else -1
        if (level != -1 && shouldApplyPotion(player, PotionEffectType.SPEED, level)) player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, level), true)

        if (diamonds > 250) if (shouldApplyPotion(player, PotionEffectType.FIRE_RESISTANCE, 0)) player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Int.MAX_VALUE, 0), true)
        if (diamonds > 600) if (shouldApplyPotion(player, PotionEffectType.REGENERATION, 0)) player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, Int.MAX_VALUE, 0), true)
        if (diamonds >= 1000 && shouldApplyPotion(player, PotionEffectType.SATURATION, 0)) player.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, Int.MAX_VALUE, 0), true)

    }

    private fun shouldApplyPotion(player: Player, eff: PotionEffectType, level: Int): Boolean
    {
        var potionLevel = -1
        for (effect in player.activePotionEffects) {
            if (effect.type == eff) {
                potionLevel = effect.amplifier
                break
            }
        }
        return !player.hasPotionEffect(eff) || potionLevel < level
    }

    override fun remove(player: Player)
    {
        removeInfiniteEffects(player)
        noDamage.remove(player.name)
        invis.remove(player.name)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!PvPClassService.hasKitOn(event.player, this)) return
        if (event.action.name.contains("RIGHT") && isUltimateReady(event.player)) {
            activateUltimate(event.player)

            Tasks.delayed(10 * 20L) {
                deactivateUltimate(event.player)
            }
        }
    }

    @EventHandler
    fun onMine(event: BlockBreakEvent) {
        val player = event.player
        if (event.block.type != Material.DIAMOND_ORE) return
        if (!PvPClassService.hasKitOn(player, this)) return

        if (isUltimateActive(player)) {
            val origin = event.block.location

            for (x in origin.blockX - ULTIMATE_RADIUS..origin.blockX + ULTIMATE_RADIUS) {
                for (y in origin.blockY - ULTIMATE_RADIUS..origin.blockY + ULTIMATE_RADIUS) {
                    for (z in origin.blockZ - ULTIMATE_RADIUS..origin.blockZ + ULTIMATE_RADIUS) {
                        val block = Location(origin.world, x.toDouble(), y.toDouble(), z.toDouble())
                        val team = LandBoard.getTeam(block)
                        if (team == null || team is PlayerTeam && team.isMember(player.uniqueId)) {
                            block.block.breakNaturally()
                        }
                    }
                }
            }
        } else increaseUltimate(player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return

        val player = event.entity as Player
        if (!PvPClassService.hasKitOn(player, this)) return

        noDamage[player.name] = 15
        if (invis.containsKey(player.name) && invis[player.name] != 0) {
            invis[player.name] = 10
            player.removePotionEffect(PotionEffectType.INVISIBILITY)
            player.sendMessage("${CC.BLUE}Miner Invisibility${CC.YELLOW} has been temporarily removed!")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player || event.entity !is Player) return

        val player = event.damager as Player
        if (!PvPClassService.hasKitOn(player, this)) return

        noDamage[player.name] = 15
        if (invis.containsKey(player.name) && invis[player.name] != 0) {
            invis[player.name] = 10
            player.removePotionEffect(PotionEffectType.INVISIBILITY)
            player.sendMessage("${CC.BLUE}Miner Invisibility${CC.YELLOW} has been temporarily removed!")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent)
    {
        if (event.from.blockY == event.to.blockY) return

        val player = event.player
        if (!PvPClassService.hasKitOn(player, this)) return

        if (event.to.blockY <= Y_HEIGHT) { // Going below 20
            if (!invis.containsKey(player.name)) {
                invis[player.name] = 10
                player.sendMessage("${CC.BLUE}Miner Invisibility${CC.YELLOW} will be activated in 10 seconds.")
            }
        } else if (event.to.blockY > Y_HEIGHT) { // Going above 20
            if (invis.containsKey(player.name)) {
                noDamage.remove(player.name)
                invis.remove(player.name)
                player.removePotionEffect(PotionEffectType.INVISIBILITY)
                player.sendMessage("${CC.BLUE}Miner Invisibility${CC.YELLOW} has been removed.")
            }
        }
    }
}