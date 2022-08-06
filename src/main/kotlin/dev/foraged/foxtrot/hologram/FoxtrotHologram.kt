package dev.foraged.foxtrot.hologram

import dev.foraged.commons.persist.impl.IntegerPersistMap
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.HologramEntity
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.Location
import java.util.*

open class FoxtrotHologram(val hologramMap: IntegerPersistMap, var title: String, location: Location) : HologramEntity("", location), IDataStoreObject {

    override val identifier = UUID.randomUUID()

    override fun getLines(): List<String> {
        var index = 0
        return mutableListOf<String>().also { lines ->
            lines.add(title)

            hologramMap.wrappedMap.entries.sortedBy {
                it.value
            }.stream().limit(10).forEach {
                lines.add(" ${CC.B_PRI}${index++} ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}${ScalaStoreUuidCache.username(it.key)}${CC.GRAY}: ${CC.RED}${it.value}")
            }
        }
    }

    override fun onDeletion() {
        HologramService.unregisterHologram(this)
    }

    init {
        initializeData()
        EntityHandler.trackEntity(this)
    }
}