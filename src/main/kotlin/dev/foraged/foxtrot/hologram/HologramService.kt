package dev.foraged.foxtrot.hologram

import dev.foraged.commons.annotations.Listeners
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import org.bukkit.event.Listener

@Listeners
object HologramService : Listener {

    val types = listOf("kills", "deaths", "killstreak")
    val controller: DataStoreObjectController<FoxtrotHologram> = DataStoreObjectControllerCache.create()

    fun registerHologram(hologram: FoxtrotHologram) {
        controller.save(hologram, DataStoreStorageType.CACHE)
        controller.save(hologram, DataStoreStorageType.MONGO)
    }

    fun unregisterHologram(hologram: FoxtrotHologram) {
        hologram.destroyForCurrentWatchers()
        controller.delete(hologram.identifier, DataStoreStorageType.MONGO)
        controller.delete(hologram.identifier, DataStoreStorageType.CACHE)
    }
}