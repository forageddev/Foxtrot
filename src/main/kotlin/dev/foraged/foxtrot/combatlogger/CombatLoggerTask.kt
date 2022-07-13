package dev.foraged.foxtrot.combatlogger

import dev.foraged.commons.annotations.runnables.Repeating
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.npc.util.NpcUtils
import org.bukkit.Bukkit
import kotlin.math.log

@Repeating(20L)
class CombatLoggerTask : Runnable
{
    override fun run()
    {
        CombatLoggerHandler.loggers.forEach {
            val logger = it.key
            val time = it.value

            if (System.currentTimeMillis() - time > 30 * 1000) {
                EntityHandler.forgetEntity(logger)
                logger.destroyForCurrentWatchers()
            }
        }
    }
}