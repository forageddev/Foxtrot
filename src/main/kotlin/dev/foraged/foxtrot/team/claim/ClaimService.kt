package dev.foraged.foxtrot.team.claim

import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamService
import gg.scala.flavor.service.Service
import java.util.*

@Service
object ClaimService
{
    private val claimingMap = mutableMapOf<UUID, UUID>()

    fun remove(uuid: UUID) {
        claimingMap.remove(uuid)
    }

    operator fun set(uuid: UUID, team: Team) {
        claimingMap[uuid] = team.identifier
    }

    operator fun get(uuid: UUID) : Team? {
        return TeamService.teams.find { it.identifier == claimingMap[uuid] }
    }
}