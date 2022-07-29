package dev.foraged.foxtrot.game

import gg.scala.store.storage.storable.IDataStoreObject
import org.bukkit.entity.Player
import java.util.UUID

abstract class Game(override val identifier: UUID = UUID.randomUUID(), val name: String) : IDataStoreObject
{
    abstract val active: Boolean
    abstract fun start()
    abstract fun stop(winner: Player?)
    open fun getScoreboardLines() : List<String> {return emptyList()}
}