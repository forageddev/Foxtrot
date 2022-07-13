package dev.foraged.foxtrot.combatlogger

import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.npc.NpcEntity
import net.evilblock.cubed.util.CC
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
        itemInHand = owner.itemInHand
        updateTextureByUsername(owner.name) { _, _ ->

        }

        initializeData()
        EntityHandler.trackEntity(this)
    }

    override fun isDamageable(): Boolean {
        return true
    }
}