package dev.foraged.foxtrot.team.impl

import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamService
import dev.foraged.foxtrot.team.enums.SystemFlag
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*

class SystemTeam(identifier: UUID, name: String) : Team(identifier, name)
{
    val flags = mutableListOf<SystemFlag>()

    fun hasFlag(flag: SystemFlag) : Boolean {
        return flags.contains(flag)
    }

    override fun saveEntry()
    {
        TeamService.systemTeamController.save(this)
    }

    override fun deleteEntry()
    {
        TeamService.systemTeamController.delete(identifier, DataStoreStorageType.ALL)
    }

    override fun getName(player: Player): String
    {
        var final = "$color$name"
        if (flags.contains(SystemFlag.KING_OF_THE_HILL)) final += " ${CC.GOLD}KOTH"
        if (flags.contains(SystemFlag.ROAD)) final += " Road"

        return final
    }
}