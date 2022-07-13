package dev.foraged.foxtrot.team.claim

import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamHandler
import java.util.*

object ClaimHandler
{
    private val claimingMap = mutableMapOf<UUID, UUID>()

    fun remove(uuid: UUID) {
        claimingMap.remove(uuid)
    }

    operator fun set(uuid: UUID, team: Team) {
        claimingMap[uuid] = team.identifier
    }

    operator fun get(uuid: UUID) : Team? {
        return TeamHandler.teams.find { it.identifier == claimingMap[uuid] }
    }
}