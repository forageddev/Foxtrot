package dev.foraged.foxtrot.combatlogger

import dev.foraged.commons.annotations.runnables.Repeating
import net.evilblock.cubed.entity.EntityHandler

@Repeating(20L)
class CombatLoggerTask : Runnable
{
    override fun run()
    {
        CombatLoggerService.loggers.forEach {
            val logger = it.key
            val time = it.value

            if (System.currentTimeMillis() - time > 30 * 1000) {
                EntityHandler.forgetEntity(logger)
                logger.destroyForCurrentWatchers()
            }
        }
    }
}