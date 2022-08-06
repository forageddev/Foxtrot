package dev.foraged.foxtrot.hologram

import dev.foraged.commons.annotations.runnables.Repeating

@Repeating(120 * 20)
class HologramUpdateTask : Runnable {

    override fun run() {
        HologramService.controller.localCache().values.forEach(FoxtrotHologram::updateForCurrentWatchers)
    }
}