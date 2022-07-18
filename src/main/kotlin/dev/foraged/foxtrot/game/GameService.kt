package dev.foraged.foxtrot.game

import dev.foraged.commons.persist.PluginService
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.game.koth.KothGame
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType

@Service
object GameService : PluginService {

    val games = mutableListOf<Game>()
    val kothStorage: DataStoreObjectController<KothGame> = DataStoreObjectControllerCache.create()

    @Configure
    override fun configure()
    {
        kothStorage.loadAll(DataStoreStorageType.MONGO).thenAccept {
            it.values.forEach(this::registerGame)
        }
    }

    fun registerGame(game: Game) {
        games.add(game)
        FoxtrotExtendedPlugin.instance.logger.info("[Game] Registered ${game::class.simpleName} with name ${game.name}.")
    }

    fun deleteGame(game: Game) {
        games.remove(game)
        if (game is KothGame) kothStorage.delete(game.identifier, DataStoreStorageType.MONGO)
    }

    fun findGameByName(name: String) : Game? {
        return games.firstOrNull {it.name.equals(name, true)}
    }
}