package dev.foraged.foxtrot.event.team

import dev.foraged.foxtrot.team.Team
import net.evilblock.cubed.event.PluginEvent

abstract class AbstractTeamEvent(val team: Team) : PluginEvent()