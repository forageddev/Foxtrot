package dev.foraged.foxtrot.game.supply

import dev.foraged.commons.annotations.runnables.Repeating

@Repeating(30 * 60 * 20)
class SupplyDropTask : Runnable
{
    override fun run() {
        SupplyDropService.summonSupplyDrop(true)
    }
}