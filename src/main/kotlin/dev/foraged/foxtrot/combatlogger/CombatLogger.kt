package dev.foraged.foxtrot.combatlogger

import dev.foraged.foxtrot.map.CombatLoggerTrackerPersistMap
import dev.foraged.foxtrot.server.ServerHandler
import dev.foraged.foxtrot.team.TeamHandler
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.npc.NpcEntity
import net.evilblock.cubed.util.CC
import org.bukkit.Material
import org.bukkit.entity.Player

class CombatLogger(val owner: Player) : NpcEntity(listOf(
    "",
    "${CC.B_YELLOW}Combat Logger",
    "${CC.RED}${owner.name}",
    ""
), owner.location)
{
    var uniqueId = owner.uniqueId
    var health = owner.health
    var armorContents = owner.inventory.armorContents
    var contents = owner.inventory.contents

    init {
        CombatLoggerHandler.loggers[this] = System.currentTimeMillis()
        helmet = owner.inventory.helmet
        chestplate = owner.inventory.chestplate
        leggings = owner.inventory.leggings
        boots = owner.inventory.boots
        itemInHand = if (owner.itemInHand.type == Material.AIR) null else owner.itemInHand
        updateTextureByUsername(owner.name) { _, _ ->

        }

        initializeData()
        EntityHandler.trackEntity(this)
    }

    override fun isDamageable(): Boolean {
        return true
    }

    override fun damage(amount: Double)
    {
        health = (health - amount).coerceAtMost(0.0)

        if (health == 0.0) {
            contents.filterNotNull().forEach {
                location.world.dropItemNaturally(location, it)
            }
            armorContents.filterNotNull().forEach {
                location.world.dropItemNaturally(location, it)
            }

            CombatLoggerTrackerPersistMap[uniqueId] = true
            location.world.strikeLightningEffect(location)
            if (!ServerHandler.KIT_MAP) {
                // todo: do deathban logic
            }
            val team = TeamHandler.findTeamByPlayer(uniqueId) ?: return
            team.playerDeath(ScalaStoreUuidCache.username(uniqueId) ?: uniqueId.toString(), 1.0)
        }
        super.damage(amount)
    }
}