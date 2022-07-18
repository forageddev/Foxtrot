package dev.foraged.foxtrot.classes.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.classes.PvPClass
import dev.foraged.foxtrot.classes.PvPClassService
import dev.foraged.foxtrot.map.cooldown.nopersist.SpawnTagMap
import dev.foraged.foxtrot.map.cooldown.nopersist.pvpclass.archer.ArcherJumpMap
import dev.foraged.foxtrot.map.cooldown.nopersist.pvpclass.archer.ArcherSpeedMap
import dev.foraged.foxtrot.team.TeamService
import dev.foraged.foxtrot.team.enums.SystemFlag
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.PlayerInventory
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap

@Listeners
object ArcherClass : PvPClass("Archer", 15, listOf(Material.SUGAR, Material.FEATHER))
{
    const val MARK_SECONDS = 5

    private val markedPlayers: MutableMap<String, Long> = ConcurrentHashMap()
    private val markedBy: MutableMap<String, MutableSet<Pair<String, Long>>> = HashMap()

    override fun qualifies(armor: PlayerInventory): Boolean
    {
        return wearingAllArmor(armor) &&
                armor.helmet.type == Material.LEATHER_HELMET &&
                armor.chestplate.type == Material.LEATHER_CHESTPLATE &&
                armor.leggings.type == Material.LEATHER_LEGGINGS &&
                armor.boots.type == Material.LEATHER_BOOTS
    }

    override fun apply(player: Player)
    {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true)
        player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0), true)
    }

    override fun tick(player: Player)
    {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true)
        if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0), true)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityArrowHit(event: EntityDamageByEntityEvent) {
        if (event.entity is Player && event.damager is Arrow) {
            val arrow = event.damager as Arrow
            val victim = event.entity as Player
            if (arrow.shooter !is Player) return

            val shooter = arrow.shooter as Player
            val pullback = arrow.getMetadata("Pullback")[0].asFloat()
            if (!PvPClassService.hasKitOn(shooter, this)) return


            // 2 hearts for a marked shot
            // 1.5 hearts for a marking / unmarked shot.
            var damage = if (isMarked(victim)) 4 else 3 // Ternary for getting damage!

            // If the bow isn't 100% pulled back we do 1 heart no matter what.
            if (pullback < 0.5f) damage = 2 // 1 heart

            if (victim.health - damage <= 0.0) event.isCancelled = true
            else event.damage = 0.0

            // The 'ShotFromDistance' metadata is applied in the deathmessage module.
            val shotFrom = arrow.getMetadata("ShotFromDistance")[0].value() as Location
            val distance = shotFrom.distance(victim.location)
            /*DeathMessageHandler.addDamage(
                victim,
                ArrowDamageByPlayer(victim.name, damage, (arrow.shooter as Player).name, shotFrom, distance)
            )*/
            victim.health = Math.max(0.0, victim.health - damage)
            if (PvPClassService.hasKitOn(victim, this)) {
                shooter.sendMessage(
                    "${CC.YELLOW}[${CC.BLUE}Arrow Range${CC.YELLOW} (${CC.RED}${distance.toInt()}${CC.YELLOW})] ${CC.RED}Cannot mark other Archers. " +
                            "${CC.B_BLUE}(" + damage / 2 + " ${CC.B_RED}${Constants.HEART_SYMBOL}${CC.B_BLUE}" + (if (damage / 2 == 1) "" else "s") + ")"
                )
            } else if (pullback >= 0.5f) {
                shooter.sendMessage(
                    ("${CC.YELLOW}[${CC.BLUE}Arrow Range${CC.YELLOW} (${CC.RED}${distance.toInt()}${CC.YELLOW})] ${CC.GOLD}Marked player for $MARK_SECONDS seconds. " +
                            "${CC.B_BLUE}(" + (damage / 2) + " ${CC.B_RED}${Constants.HEART_SYMBOL}${CC.B_BLUE}" + (if ((damage / 2 == 1)) "" else "s") + ")")
                )

                // Only send the message if they're not already marked.
                if (!isMarked(victim)) victim.sendMessage("${CC.B_RED}Marked! ${CC.YELLOW}An archer has shot you and marked you (+25% damage) for $MARK_SECONDS seconds.")

                var invis: PotionEffect? = null
                for (potionEffect: PotionEffect in victim.activePotionEffects)
                {
                    if ((potionEffect.type == PotionEffectType.INVISIBILITY))
                    {
                        invis = potionEffect
                        break
                    }
                }
                if (invis != null)
                {
                    val playerClass = PvPClassService.getPvPClass(victim)
                    victim.removePotionEffect(invis.type)
                    val invisFinal: PotionEffect = invis

                    /* Handle returning their invisibility after the archer tag is done */
                if (playerClass is MinerClass) {
                    /* Queue player to have invis returned. (MinerClass takes care of this) */
                    playerClass.invis[victim.name] = MARK_SECONDS
                } else {
                    /* player has no class but had invisibility, return it after their tag expires */
                    object : BukkitRunnable() {
                        override fun run() {
                            if (invisFinal.duration > 1000000) return  // Ensure we never apply an infinite invis to a non miner

                            victim.addPotionEffect(invisFinal)
                        }
                    }.runTaskLater(FoxtrotExtendedPlugin.instance, ((MARK_SECONDS * 20) + 5).toLong())
                }
                }
                markedPlayers[victim.name] = System.currentTimeMillis() + (MARK_SECONDS * 1000)
                markedBy.putIfAbsent(shooter.name, HashSet())
                markedBy[shooter.name]!!.add(victim.name to (System.currentTimeMillis() + (MARK_SECONDS * 1000)))
                NametagHandler.reloadPlayer(victim)

                object : BukkitRunnable() {
                    override fun run() {
                        NametagHandler.reloadPlayer(victim)
                    }
                }.runTaskLater(FoxtrotExtendedPlugin.instance, ((MARK_SECONDS * 20) + 5).toLong())
            } else {
                shooter.sendMessage(
                    ("${CC.YELLOW}[${CC.BLUE}Arrow Range${CC.YELLOW} (${CC.RED}${distance.toInt()}${CC.YELLOW})] ${CC.GOLD}Marked player for $MARK_SECONDS seconds. " +
                            "${CC.B_BLUE}(" + (damage / 2) + " ${CC.B_RED}${Constants.HEART_SYMBOL}${CC.B_BLUE}" + (if ((damage / 2 == 1)) "" else "s") + ")")
                )
            }
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent)
    {
        if (event.entity is Player)
        {
            val player = event.entity as Player
            if (isMarked(player))
            {
                var damager: Player? = null
                if (event.damager is Player)
                {
                    damager = event.damager as Player
                } else if (event.damager is Projectile && (event.damager as Projectile).shooter is Player)
                {
                    damager = (event.damager as Projectile).shooter as Player
                }
                if (damager != null && !canUseMark(damager, player))
                {
                    return
                }

                // Apply 125% damage if they're 'marked'
                event.damage = event.damage * 1.25
            }
        }
    }

    @EventHandler
    fun onEntityShootBow(event: EntityShootBowEvent) {
        event.projectile.setMetadata("Pullback", FixedMetadataValue(FoxtrotExtendedPlugin.instance, event.force))
    }

    override fun itemConsumed(player: Player, material: Material): Boolean {
        if (material == Material.SUGAR) {
            if (ArcherSpeedMap.isOnCooldown(player.uniqueId)) {
                val millisLeft = ArcherSpeedMap.getCooldown(player.uniqueId) - System.currentTimeMillis()
                val msg: String = TimeUtil.formatIntoDetailedString(millisLeft.toInt() / 1000)
                player.sendMessage("${CC.RED}You cannot use this for another ${CC.BOLD}$msg${CC.RED}.")
                return false
            }
            ArcherSpeedMap.startCooldown(player.uniqueId)
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 200, 3), true)
            return (true)
        } else {
            if (SystemFlag.SAFE_ZONE.appliesAt(player.location))
            {
                player.sendMessage(ChatColor.RED.toString() + "You can't use this in spawn!")
                return (false)
            }
            if (ArcherJumpMap.isOnCooldown(player.uniqueId))
            {
                val millisLeft = ArcherJumpMap.getCooldown(player.uniqueId) - System.currentTimeMillis()
                val msg: String = TimeUtil.formatIntoDetailedString(millisLeft.toInt() / 1000)
                player.sendMessage(ChatColor.RED.toString() + "You cannot use this for another §c§l" + msg + "§c.")
                return (false)
            }

            ArcherJumpMap.startCooldown(player.uniqueId)
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 20 * 5, 6))
            SpawnTagMap.startCooldown(player.uniqueId)
            return (false)
        }
    }

    fun isMarked(player: Player): Boolean {
        return (markedPlayers.containsKey(player.name) && markedPlayers[player.name]!! > System.currentTimeMillis())
    }

    fun getMarkedTime(player: Player) : Long {
        return markedPlayers[player.name] ?: 0
    }

    private fun canUseMark(player: Player, victim: Player): Boolean {
        if (TeamService.findTeamByPlayer(player.uniqueId) != null) {
            val team = TeamService.findTeamByPlayer(player.uniqueId)!!
            var amount = 0
            for (member: Player in team.onlineMembers) {
                if (PvPClassService.hasKitOn(member, this)) {
                    amount++
                    if (amount > 3) break
                }
            }
            if (amount > 3) {
                player.sendMessage(ChatColor.RED.toString() + "Your team has too many archers. Archer mark was not applied.")
                return false
            }
        }
        if (markedBy.containsKey(player.name)) {
            for (pair: Pair<String, Long> in markedBy[player.name]!!)
            {
                if ((victim.name == pair.first) && pair.second > System.currentTimeMillis()) return false
            }
            return true
        } else return true
    }
}