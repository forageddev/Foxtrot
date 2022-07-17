package dev.foraged.foxtrot.combatlogger

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.map.CombatLoggerTrackerPersistMap
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.server.MapService
import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamService
import dev.foraged.foxtrot.team.enums.SystemFlag
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.flavor.service.Service
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.event.PlayerDamageEntityEvent
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@Service
@Listeners
object CombatLoggerService : Listener
{
    val loggers = mutableMapOf<CombatLogger, Long>()

    fun getByEntity(id: Int): CombatLogger? {
        return loggers.keys.firstOrNull {
            it.id == id
        }
    }

    fun getByPlayer(player: Player): CombatLogger? {
        return loggers.keys.firstOrNull {
            it.uniqueId == player.uniqueId
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val player = event.player
        if (player.hasMetadata("logoutSafe")) return
        if (player.gameMode == GameMode.CREATIVE) return
        if (MapService.SOTW_ACTIVE) return
        if (PvPTimerPersistableMap.isOnCooldown(player.uniqueId)) return

        if (SystemFlag.SAFE_ZONE.appliesAt(player.location)) return

        CombatLogger(player)
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val player = event.player
        val logger = getByPlayer(player)
        if (logger != null) {
            if (CombatLoggerTrackerPersistMap[player.uniqueId] == false) {
                event.player.health = logger.health
            }

            event.player.teleport(logger.location)
            logger.destroyForCurrentWatchers()
            EntityHandler.forgetEntity(logger)
            loggers.remove(logger)
        }
        if (CombatLoggerTrackerPersistMap[player.uniqueId] == true)
        {
            player.inventory.clear()
            player.inventory.armorContents = null
            player.exp = 0f
            player.health = 0.0
        }
        CombatLoggerTrackerPersistMap[player.uniqueId] = false
    }

   /* @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityDeathEvent(event: EntityDeathEvent)
    {
        val entity = event.entity
        val logger = getByEntity(entity)
        if (logger != null)
        {
            logger.contents.filterNotNull().forEach {
                entity.world.dropItemNaturally(entity.location, it)
            }
            logger.armorContents.filterNotNull().forEach {
                entity.world.dropItemNaturally(entity.location, it)
            }

            CombatLoggerTrackerPersistMap[logger.uniqueId] = true
            event.drops.clear()
            entity.world.strikeLightningEffect(entity.location)
            if (!ServerHandler.KIT_MAP) {
                // todo: do deathban logic
            }

            val team = TeamHandler.findTeamByPlayer(logger.uniqueId) ?: return

            team.playerDeath(ScalaStoreUuidCache.username(logger.uniqueId) ?: logger.uniqueId.toString(), 1.0)
        }
    }
*/
    @EventHandler
    fun onDamage(event: PlayerDamageEntityEvent) {
        val damager = event.player
        val logger = getByEntity(event.entity.id)
        if (logger is CombatLogger) {
            val team = TeamService.findTeamByPlayer(logger.uniqueId)

            if (team != null)
            {
                if (team.isMember(damager.uniqueId))
                {
                    damager.sendMessage(
                        "${CC.YELLOW}You cannot hurt your teammate ${CC.GREEN}${
                            ScalaStoreUuidCache.username(
                                logger.uniqueId
                            )
                        }${CC.YELLOW}."
                    )
                    event.isCancelled = true
                }
                if (team.isAlly(damager.uniqueId))
                {
                    damager.sendMessage(
                        "${CC.YELLOW}Be careful, that's your ally ${Team.ALLY_COLOR}${
                            ScalaStoreUuidCache.username(
                                logger.uniqueId
                            )
                        }${CC.YELLOW}."
                    )
                }
            }
            if (event.isCancelled) return
            Tasks.sync {
                logger.damage(event.damageData.damage)
            }
        }
    }
}