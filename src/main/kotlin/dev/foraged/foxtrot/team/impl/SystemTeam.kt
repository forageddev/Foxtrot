package dev.foraged.foxtrot.team.impl

import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamHandler
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
        TeamHandler.systemTeamController.save(this)
    }

    override fun deleteEntry()
    {
        TeamHandler.systemTeamController.delete(identifier, DataStoreStorageType.ALL)
    }

    override fun getName(player: Player): String
    {
        return color.toString() + name + if (flags.contains(SystemFlag.KING_OF_THE_HILL)) {
            " ${CC.GOLD}KOTH"
        } else ""
    }
}