package dev.foraged.foxtrot.team.impl

import dev.foraged.foxtrot.team.Team
import dev.foraged.foxtrot.team.TeamHandler
import dev.foraged.foxtrot.team.data.TeamMember
import dev.foraged.foxtrot.team.data.TeamMemberRole
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*


class PlayerTeam(identifier: UUID, name: String, val leader: TeamMember) : Team(identifier, name)
{
    var balance: Double = 0.0
    var deathsUntilRaidable: Double = 1.01
    val members = mutableSetOf<TeamMember>()
    val raidable: Boolean
        get() = deathsUntilRaidable < 0

    val officers: List<TeamMember>
        get() = members.filter { it.role == TeamMemberRole.OFFICER }

    val leaders: List<TeamMember>
        get() = members.filter { it.role == TeamMemberRole.CO_LEADER }

    val onlineMemberCount: Int
        get() {
            var amt = 0
            for (member in members)
            {
                val exactPlayer = member.getBukkitPlayer()
                if (exactPlayer != null && !exactPlayer.hasMetadata("invisible")) amt++
            }
            return amt
        }

    val onlineMembers: Collection<Player>
        get() {
            val players: MutableList<Player> = ArrayList()
            for (member in members)
            {
                val exactPlayer = member.getBukkitPlayer()
                if (exactPlayer != null && !exactPlayer.hasMetadata("invisible")) players.add(exactPlayer)
            }
            return players
        }

    val offlineMembers: Collection<UUID>
        get() {
            val players: MutableList<UUID> = ArrayList()
            for (member in members)
            {
                val exactPlayer = member.getBukkitPlayer()
                if (exactPlayer == null || exactPlayer.hasMetadata("invisible")) players.add(member.uniqueId)
            }
            return players
        }

    val invites = mutableSetOf<UUID>()

    fun broadcast(message: String) {
        onlineMembers.forEach {
            it.sendMessage(CC.translate(message))
        }
    }

    fun getMember(uniqueId: UUID): TeamMember?
    {
        return allMembers.find { it.uniqueId == uniqueId }
    }

    fun isMember(uniqueId: UUID) : Boolean {
        return getMember(uniqueId) != null
    }

    fun isCaptain(uniqueId: UUID) : Boolean {
        return isMember(uniqueId) && getMember(uniqueId)!!.role == TeamMemberRole.OFFICER
    }

    fun isCoLeader(uniqueId: UUID) : Boolean {
        return isMember(uniqueId) && getMember(uniqueId)!!.role == TeamMemberRole.CO_LEADER
    }

    fun isOwner(uniqueId: UUID) : Boolean {
        return leader.uniqueId == uniqueId
    }

    fun isAlly(uniqueId: UUID) : Boolean {
        // todo: implement allies to teamns.
        return false
    }

    val allMembers: Collection<TeamMember>
        get() {
            val members: MutableSet<TeamMember> = HashSet(members)
            members.add(this.leader)
            return members
        }

    override fun getName(player: Player): String
    {
        return if (isMember(player.uniqueId)) CC.GREEN + name
         else if (isAlly(player.uniqueId)) CC.LIGHT_PURPLE + name
        else CC.RED + name

    }

    override fun saveEntry()
    {
        TeamHandler.playerTeamController.save(this, DataStoreStorageType.ALL)
    }

    override fun deleteEntry()
    {
        TeamHandler.playerTeamController.delete(identifier, DataStoreStorageType.ALL)
    }
}