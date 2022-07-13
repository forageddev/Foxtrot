package dev.foraged.foxtrot.event.team

import dev.foraged.foxtrot.team.Team
import org.bukkit.event.Cancellable

class TeamCreateEvent(team: Team) : AbstractTeamEvent(team), Cancellable
{
    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean { return cancelled }
    override fun setCancelled(cancel: Boolean) { cancelled = cancel }
}