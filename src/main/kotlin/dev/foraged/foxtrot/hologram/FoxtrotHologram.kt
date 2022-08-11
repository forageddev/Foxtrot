package dev.foraged.foxtrot.hologram

import dev.foraged.commons.persist.impl.IntegerPersistMap
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.HologramEntity
import net.evilblock.cubed.entity.hologram.updating.FormatUpdatingHologramEntity
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*
import java.util.concurrent.TimeUnit

open class FoxtrotHologram(val hologramMap: IntegerPersistMap, var title: String, location: Location) :
    FormatUpdatingHologramEntity(title, location), IDataStoreObject {

    override val identifier = UUID.randomUUID()

    override fun getNewLines(): List<String>
    {
        var index = 0
        return mutableListOf<String>().also { lines ->
            lines.add(CC.GRAY)
            lines.add("${CC.B_PRI}${Bukkit.getServerName()}")
            lines.add("${CC.WHITE}Top 10 ${title}")
            lines.add(CC.GRAY)

            hologramMap.wrappedMap.entries.sortedBy {
                it.value
            }.stream().limit(10).forEach {
                lines.add(" ${CC.B_PRI}${index++} ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}${ScalaStoreUuidCache.username(it.key)}${CC.GRAY} - ${CC.GREEN}${it.value}")
            }
        }
    }

    override fun getTickInterval() = TimeUnit.SECONDS.toMillis(90)

    fun initialLoad() {
        initializeData()
        EntityHandler.trackEntity(this)
    }

    override fun onDeletion() {
        HologramService.unregisterHologram(this)
    }
}