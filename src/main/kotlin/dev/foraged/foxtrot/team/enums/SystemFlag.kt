package dev.foraged.foxtrot.team.enums

import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.impl.SystemTeam
import org.bukkit.Location

enum class SystemFlag
{
    SAFE_ZONE,
    THIRTY_SECOND_PEARL,
    FORTY_FIVE_SECOND_SPAWN_TAG,
    FIVE_MINUTE_DEATHBAN,
    FIFTEEN_MINUTE_DEATHBAN,
    DENY_RE_ENTRY,
    DENY_ENDERPEARL,
    ROAD,
    DTC,
    FURY,
    CONQUEST,
    CITADEL,
    ALLOW_COBWEBS,
    CAVERN,
    GLOWSTONE,
    KING_OF_THE_HILL;

    open fun appliesAt(location: Location): Boolean
    {
        val ownerTo = LandBoard.getTeam(location)
        return ownerTo != null && ownerTo is SystemTeam && ownerTo.hasFlag(this)
    }
}