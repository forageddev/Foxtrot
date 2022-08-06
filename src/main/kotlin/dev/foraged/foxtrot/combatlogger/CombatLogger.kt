package dev.foraged.foxtrot.combatlogger

import dev.foraged.foxtrot.map.CombatLoggerTrackerPersistMap
import dev.foraged.foxtrot.map.stats.DeathsPersistMap
import dev.foraged.foxtrot.server.MapService
import dev.foraged.foxtrot.team.TeamService
import gg.scala.cache.uuid.ScalaStoreUuidCache
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.npc.NpcEntity
import net.evilblock.cubed.util.CC
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player

class CombatLogger(owner: Player) : NpcEntity(listOf(
    "{empty}",
    "${CC.B_YELLOW}Combat Logger",
    "${CC.RED}${owner.name}",
    "{empty}"
), owner.location)
{
    var uniqueId = owner.uniqueId
    var health = owner.health
    var armorContents = owner.inventory.armorContents
    var contents = owner.inventory.contents

    init {
        CombatLoggerService.loggers[this] = System.currentTimeMillis()
        helmet = owner.inventory.helmet
        chestplate = owner.inventory.chestplate
        leggings = owner.inventory.leggings
        boots = owner.inventory.boots
        itemInHand = if (owner.itemInHand.type == Material.AIR) null else owner.itemInHand

        initializeData()
        EntityHandler.trackEntity(this)

        val property = ((owner) as CraftPlayer).handle.profile.properties["textures"].first()
        updateTexture(property.value, property.signature)
    }

    override fun isDamageable(): Boolean {
        return true
    }

    override fun damage(amount: Double)
    {
        health -= if (amount == 0.0) 1.5 else amount

        if (health <= 0.0) {
            contents.filterNotNull().filterNot { it.type == Material.AIR }.forEach {
                location.world.dropItemNaturally(location, it)
            }
            armorContents.filterNotNull().filterNot { it.type == Material.AIR }.forEach {
                location.world.dropItemNaturally(location, it)
            }

            destroyForCurrentWatchers()
            EntityHandler.forgetEntity(this)
            CombatLoggerTrackerPersistMap[uniqueId] = true
            DeathsPersistMap[uniqueId]?.inc()
            location.world.strikeLightningEffect(location)
            if (!MapService.KIT_MAP) {
                // todo: do deathban logic
            }
            val team = TeamService.findTeamByPlayer(uniqueId) ?: return
            team.playerDeath(ScalaStoreUuidCache.username(uniqueId) ?: uniqueId.toString(), 1.0)
        }
        super.damage(amount)
    }
}