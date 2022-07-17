package dev.foraged.foxtrot.team

import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.impl.PlayerTeam
import dev.foraged.foxtrot.team.impl.SystemTeam
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*

@Service
object TeamService
{
    val teams = mutableSetOf<Team>()
    val playerTeamController: DataStoreObjectController<PlayerTeam> = DataStoreObjectControllerCache.create()
    val systemTeamController: DataStoreObjectController<SystemTeam> = DataStoreObjectControllerCache.create()

    fun registerTeam(team: Team)
    {
        teams.add(team)
        FoxtrotExtendedPlugin.instance.logger.info("[Team] Registered new team with class ${team::class.simpleName} and name ${team.name}")
    }

    fun unregisterTeam(team: Team) {
        teams.remove(team)
        LandBoard.clear(team)
        if (team is PlayerTeam) playerTeamController.delete(team.identifier, DataStoreStorageType.MONGO)
        if (team is SystemTeam) systemTeamController.delete(team.identifier, DataStoreStorageType.MONGO)
        FoxtrotExtendedPlugin.instance.logger.info("[Team] Unregistered team with name ${team.name}")
    }

    fun findTeamByName(name: String?): Team?
    {
        for (team in teams)
        {
            if (team.name.equals(name, ignoreCase = true)) return team
        }
        return null
    }

    fun findTeamByPlayer(uniqueId: UUID): PlayerTeam?
    {
        for (team in teams) if (team is PlayerTeam && team.getMember(uniqueId) != null) return team
        return null
    }

    @Configure
    fun configure()
    {
        FoxtrotExtendedPlugin.instance.logger.info("[Team] Loading all teams from MongoDB")
        systemTeamController.loadAll(DataStoreStorageType.MONGO).thenAccept {
            teams.addAll(it.values)

            playerTeamController.loadAll(DataStoreStorageType.MONGO).thenAccept {
                teams.addAll(it.values)

                FoxtrotExtendedPlugin.instance.logger.info("[Team] Loaded ${teams.size} from MongoDB")
                LandBoard.loadFromTeams()
                FoxtrotExtendedPlugin.instance.logger.info("[Team] Setup LandBoard for all claims")
            }
        }
    }

    @Close
    fun close()
    {
        FoxtrotExtendedPlugin.instance.logger.info("[Team] Saving ${teams.size} to MongoDB")
        teams.filterIsInstance<SystemTeam>().forEach {
            systemTeamController.save(it, DataStoreStorageType.MONGO)
        }
        teams.filterIsInstance<PlayerTeam>().forEach {
            playerTeamController.save(it, DataStoreStorageType.MONGO)
        }
        FoxtrotExtendedPlugin.instance.logger.info("[Team] Completed save of ${teams.size} to MongoDB")
    }
}